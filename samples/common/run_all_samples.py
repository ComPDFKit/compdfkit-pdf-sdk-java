#!/usr/bin/env python3
"""Run the fixed Java sample configurations, honoring the persistent skip list."""

import datetime
import hashlib
import json
import os
from pathlib import Path
import resource
import signal
import subprocess
import sys
import time


SAMPLES_DIR = Path(__file__).resolve().parents[1]
SKIP_FILE = SAMPLES_DIR / "batch-skip.txt"
TIMEOUT_SECONDS = 180


def read_skip_list():
    return {
        line.split("#", 1)[0].strip()
        for line in SKIP_FILE.read_text(encoding="utf-8").splitlines()
        if line.split("#", 1)[0].strip()
    }


def disable_core_dumps():
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))


def stop_process_group(process):
    for sig in (signal.SIGTERM, signal.SIGKILL):
        try:
            os.killpg(process.pid, sig)
        except ProcessLookupError:
            pass
        try:
            process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            continue
    process.wait()


def output_snapshot(run_dir):
    outputs = {}
    for path in (SAMPLES_DIR / "out").rglob("*"):
        if path.is_file() and run_dir not in path.parents:
            stat = path.stat()
            outputs[str(path.relative_to(SAMPLES_DIR))] = (
                stat.st_size, stat.st_mtime_ns
            )
    return outputs


def save_results(run_dir, report):
    temporary = run_dir / "results.json.tmp"
    temporary.write_text(
        json.dumps(report, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    temporary.replace(run_dir / "results.json")


def run_sample(script, run_dir, environment):
    name = script.parent.name
    log_path = run_dir / "logs" / (name + ".log")
    before = output_snapshot(run_dir)
    started = time.monotonic()
    status = "failed"
    exit_code = None
    print("START " + name, flush=True)
    with log_path.open("wb") as log:
        process = subprocess.Popen(
            ["bash", "-e", "./RunTest.sh"],
            cwd=str(script.parent),
            env=environment,
            stdout=log,
            stderr=subprocess.STDOUT,
            start_new_session=True,
            preexec_fn=disable_core_dumps,
        )
        try:
            exit_code = process.wait(timeout=TIMEOUT_SECONDS)
            status = "passed" if exit_code == 0 else "failed"
        except subprocess.TimeoutExpired:
            status = "timeout"
            stop_process_group(process)
            exit_code = process.returncode
        except BaseException:
            stop_process_group(process)
            raise
    after = output_snapshot(run_dir)
    result = {
        "name": name,
        "status": status,
        "exit_code": exit_code,
        "seconds": round(time.monotonic() - started, 2),
        "log": str(log_path.relative_to(run_dir)),
        "outputs": [
            {"path": path, "bytes": metadata[0]}
            for path, metadata in sorted(after.items())
            if before.get(path) != metadata
        ],
    }
    print("END {} {} exit={} seconds={}".format(
        name, status, exit_code, result["seconds"]
    ), flush=True)
    return result


def main():
    scripts = sorted(SAMPLES_DIR.glob("*/RunTest.sh"))
    skipped = read_skip_list()
    unknown = skipped - {script.parent.name for script in scripts}
    if not scripts or unknown:
        raise ValueError("No runnable samples or unknown skip entries: "
                         + ", ".join(sorted(unknown)))
    stamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    run_dir = SAMPLES_DIR / "out" / "batch-runs" / stamp
    (run_dir / "logs").mkdir(parents=True)
    environment = dict(os.environ)
    environment["JAVA_TOOL_OPTIONS"] = (
        environment.get("JAVA_TOOL_OPTIONS", "")
        + " -Dfile.encoding=UTF8 -Djava.awt.headless=true"
    ).strip()
    report = {
        "started_at": datetime.datetime.now().astimezone().isoformat(),
        "samples_dir": str(SAMPLES_DIR),
        "skip_file": str(SKIP_FILE),
        "timeout_seconds": TIMEOUT_SECONDS,
        "discovered": len(scripts),
        "jars": [
            {"name": jar.name, "sha256": hashlib.sha256(jar.read_bytes()).hexdigest()}
            for jar in sorted((SAMPLES_DIR / "lib").glob("compdfkit-*.jar"))
        ],
        "results": [],
    }
    save_results(run_dir, report)
    print("RESULTS " + str(run_dir / "results.json"), flush=True)
    for script in scripts:
        name = script.parent.name
        if name in skipped:
            result = {
                "name": name,
                "status": "skipped",
                "reason": "Excluded by batch-skip.txt at KDAN's request",
            }
            print("SKIP " + name + " (batch-skip.txt)", flush=True)
        else:
            result = run_sample(script, run_dir, environment)
        report["results"].append(result)
        save_results(run_dir, report)
    report["finished_at"] = datetime.datetime.now().astimezone().isoformat()
    report["counts"] = {
        status: sum(result["status"] == status for result in report["results"])
        for status in ("passed", "failed", "timeout", "skipped")
    }
    save_results(run_dir, report)
    print("FINISHED " + json.dumps(report["counts"]), flush=True)
    return 1 if report["counts"]["failed"] or report["counts"]["timeout"] else 0


if __name__ == "__main__":
    sys.exit(main())
