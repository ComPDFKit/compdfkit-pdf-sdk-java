#!/usr/bin/env python3
"""Prepare local, ignored fixtures from the Android demo's existing test PKI.

Edit these constants if your test PKI is elsewhere. No keys are generated and
the CA private key is never copied. Existing different fixtures are not replaced.
"""

from pathlib import Path
import re
import subprocess
import sys

SOURCE_DIR = Path.home() / "pki"
OUTPUT_DIR = Path(__file__).resolve().parent.parent / "TestFiles" / "SignatureLtvAndroid"
PASSWORD = "1234"
SYSTEM_CA_BUNDLES = [
    Path("/etc/ssl/cert.pem"),
    Path("/etc/ssl/certs/ca-certificates.crt"),
    Path("/opt/homebrew/etc/openssl@3/cert.pem"),
]


def openssl(*args, data=None):
    result = subprocess.run(
        ["openssl", *args], input=data, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=20
    )
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout


def main():
    names = ["signer.p12", "signer.crt", "root.crt", "trusted_roots.pem", "test.crl"]
    materials = {name: (SOURCE_DIR / name).read_bytes() for name in names}
    openssl(
        "verify", "-CAfile", str(SOURCE_DIR / "trusted_roots.pem"), str(SOURCE_DIR / "signer.crt")
    )
    openssl(
        "pkcs12",
        "-in",
        str(SOURCE_DIR / "signer.p12"),
        "-noout",
        "-passin",
        "stdin",
        data=(PASSWORD + "\n").encode(),
    )
    openssl(
        "crl",
        "-inform",
        "DER",
        "-in",
        str(SOURCE_DIR / "test.crl"),
        "-verify",
        "-CAfile",
        str(SOURCE_DIR / "trusted_roots.pem"),
        "-noout",
    )

    # The Android guide supplies the signer CA only. Timestamp validation also
    # needs trust anchors for its public DigiCert TSA. Reuse installed OS roots;
    # never trust a certificate merely because it came back in a TSA response.
    digicert_roots = set()
    for bundle in SYSTEM_CA_BUNDLES:
        if not bundle.is_file():
            continue
        for cert in re.findall(
            rb"-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----", bundle.read_bytes(), re.S
        ):
            subject = openssl("x509", "-noout", "-subject", data=cert)
            if b"DigiCert" in subject:
                digicert_roots.add(cert + b"\n")
    if not digicert_roots:
        raise RuntimeError(
            "No DigiCert roots in SYSTEM_CA_BUNDLES; configure a trusted system CA bundle first."
        )
    materials["trusted_roots.pem"] = (
        materials["trusted_roots.pem"].rstrip() + b"\n" + b"".join(sorted(digicert_roots))
    )

    for name, data in materials.items():
        target = OUTPUT_DIR / name
        if target.exists() and target.read_bytes() != data:
            raise RuntimeError(
                "Refusing to replace different existing test material: " + str(target)
            )
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    ignore = OUTPUT_DIR / ".gitignore"
    if not ignore.exists():
        ignore.write_text("# Generated local test PKI; includes a test private key.\n*\n")
    elif "*" not in ignore.read_text().splitlines():
        raise RuntimeError(
            "Test PKI directory must be ignored before copying private material: " + str(ignore)
        )
    for name, data in materials.items():
        target = OUTPUT_DIR / name
        if not target.exists():
            with target.open("xb") as stream:
                stream.write(data)
        if name.endswith(".p12"):
            target.chmod(0o600)
    print("Prepared Android demo test PKI: " + str(OUTPUT_DIR))
    print(
        "Signer CA is unchanged; trust bundle also includes %d installed DigiCert roots."
        % len(digicert_roots)
    )


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("Prepare Android test PKI: " + str(error), file=sys.stderr, flush=True)
        sys.exit(1)
