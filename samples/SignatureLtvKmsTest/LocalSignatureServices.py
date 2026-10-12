#!/usr/bin/env python3
"""Local test CA, RFC 3161 TSA and CRL service, owned by the Java sample process.

Arguments are supplied internally by SignatureLtvKmsTest's constants. This helper
never contacts AWS or a public TSA and only binds the configured loopback port.
"""

import http.server
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import threading
from urllib.parse import urlsplit


def openssl(*args, data=None):
    result = subprocess.run(
        ["openssl", *args], input=data, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=20
    )
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout


def prepare_materials(directory, password, tsa_url, crl_url):
    marker = directory / "sample-materials.json"
    expected = {"type": "SignatureLtvKmsTest-local-ca-v1", "tsaUrl": tsa_url}
    if directory.exists():
        if not marker.is_file() or json.loads(marker.read_text()) != expected:
            raise RuntimeError(
                "Certificate directory is not managed by this sample, or its TSA URL changed: "
                + str(directory)
            )
        os.chdir(directory)
        for name in ["ca.pem", "signer.pem", "tsa.pem"]:
            openssl("x509", "-in", name, "-noout", "-checkend", "60")
        openssl(
            "pkcs12",
            "-in",
            "signer.p12",
            "-noout",
            "-passin",
            "stdin",
            data=(password + "\n").encode(),
        )
        # Refresh revocation evidence while preserving the CA for prolongation.
        openssl("ca", "-gencrl", "-config", "ca.cnf", "-out", "ca.crl.pem")
        openssl("crl", "-in", "ca.crl.pem", "-outform", "DER", "-out", "ca.crl")
        return

    directory.parent.mkdir(parents=True, exist_ok=True)
    staging = Path(tempfile.mkdtemp(prefix=".local-signature-materials-", dir=directory.parent))
    original_cwd = Path.cwd()
    try:
        os.chdir(staging)
        Path("index").touch()
        Path("serial").write_text("1000\n")
        Path("crlnumber").write_text("1000\n")
        Path("newcerts").mkdir()
        Path("root.cnf").write_text("""[req]
distinguished_name=dn
x509_extensions=ext
[dn]
[ext]
basicConstraints=critical,CA:TRUE
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
""")
        Path("ca.cnf").write_text(
            """[ca]
default_ca=local
[local]
database=index
new_certs_dir=newcerts
certificate=ca.pem
private_key=ca.key
serial=serial
crlnumber=crlnumber
default_md=sha256
default_days=30
default_crl_days=30
policy=policy
unique_subject=no
[policy]
commonName=supplied
[signer]
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature,nonRepudiation
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
crlDistributionPoints=URI:"""
            + crl_url
            + """
[tsa]
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature
extendedKeyUsage=critical,timeStamping
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
crlDistributionPoints=URI:"""
            + crl_url
            + "\n"
        )
        openssl(
            "req",
            "-config",
            "root.cnf",
            "-x509",
            "-newkey",
            "rsa:2048",
            "-nodes",
            "-keyout",
            "ca.key",
            "-out",
            "ca.pem",
            "-days",
            "30",
            "-subj",
            "/CN=SDK Sample Test CA",
        )
        for name in ["signer", "tsa"]:
            openssl(
                "req",
                "-new",
                "-newkey",
                "rsa:2048",
                "-nodes",
                "-keyout",
                name + ".key",
                "-out",
                name + ".csr",
                "-subj",
                "/CN=SDK Sample " + name,
            )
            openssl(
                "ca",
                "-batch",
                "-config",
                "ca.cnf",
                "-extensions",
                name,
                "-in",
                name + ".csr",
                "-out",
                name + ".pem",
                "-notext",
            )
            openssl("x509", "-in", name + ".pem", "-outform", "DER", "-out", name + ".der")
        openssl("x509", "-in", "ca.pem", "-outform", "DER", "-out", "ca.der")
        openssl("verify", "-CAfile", "ca.pem", "signer.pem", "tsa.pem")
        openssl("ca", "-gencrl", "-config", "ca.cnf", "-out", "ca.crl.pem")
        openssl("crl", "-in", "ca.crl.pem", "-outform", "DER", "-out", "ca.crl")
        openssl(
            "pkcs12",
            "-export",
            "-inkey",
            "signer.key",
            "-in",
            "signer.pem",
            "-certfile",
            "ca.pem",
            "-out",
            "signer.p12",
            "-passout",
            "stdin",
            data=(password + "\n").encode(),
        )
        Path("tsa.serial").write_text("01\n")
        Path("tsa.cnf").write_text("""[tsa]
default_tsa=local
[local]
serial=tsa.serial
signer_cert=tsa.pem
certs=ca.pem
signer_key=tsa.key
signer_digest=sha256
default_policy=1.2.3.4.1
other_policies=1.2.3.4.2
digests=sha256,sha384,sha512
accuracy=secs:1
ordering=no
tsa_name=yes
ess_cert_id_chain=yes
ess_cert_id_alg=sha256
""")
        Path("sample-materials.json").write_text(json.dumps(expected))
        os.chdir(original_cwd)
        staging.rename(directory)
    finally:
        os.chdir(original_cwd)
        if staging.exists():
            shutil.rmtree(staging)
    os.chdir(directory)


def main():
    directory = Path(sys.argv[1]).resolve()
    password, tsa_url = sys.argv[2:4]
    url = urlsplit(tsa_url)
    if (
        url.scheme != "http"
        or url.hostname != "127.0.0.1"
        or url.path != "/tsa"
        or url.query
        or url.fragment
        or url.username
    ):
        raise ValueError("Local test TSA requires http://127.0.0.1:<port>/tsa")
    crl_url = "http://" + url.netloc + "/ca.crl"

    class Handler(http.server.BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass

        def reply(self, data, content_type):
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)

        def do_GET(self):
            if self.path == "/ca.crl":
                self.reply(Path("ca.crl").read_bytes(), "application/pkix-crl")
            else:
                self.send_error(404)

        def do_POST(self):
            if self.path != "/tsa":
                self.send_error(404)
                return
            size = int(self.headers.get("Content-Length", "0"))
            if size <= 0 or size > 65536:
                self.send_error(400)
                return
            try:
                with tempfile.TemporaryDirectory(dir=directory) as temp:
                    query = Path(temp) / "query.der"
                    query.write_bytes(self.rfile.read(size))
                    reply = openssl("ts", "-reply", "-config", "tsa.cnf", "-queryfile", str(query))
                self.reply(reply, "application/timestamp-reply")
            except Exception as error:
                print("Local TSA failed: " + str(error), file=sys.stderr, flush=True)
                self.send_error(500)

    # Reserve the port before touching persistent material; never stop another service.
    server = http.server.HTTPServer(("127.0.0.1", url.port or 80), Handler)
    started = False
    try:
        prepare_materials(directory, password, tsa_url, crl_url)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        started = True
        print("LOCAL_SIGNATURE_SERVICES_READY", flush=True)
        # EOF stops the service if the owning JVM exits, including after a failure.
        sys.stdin.buffer.read()
    finally:
        if started:
            server.shutdown()
        server.server_close()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("Local signature services: " + str(error), file=sys.stderr, flush=True)
        sys.exit(1)
