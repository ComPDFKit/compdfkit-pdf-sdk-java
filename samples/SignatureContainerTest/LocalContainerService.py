#!/usr/bin/env python3
"""Loopback-only CMS service for the Java sample and integration tests.

Only synthetic test keys are used. The small DER builder below demonstrates the
messageDigest / signed-attributes contract, not a production signing service.
"""

import http.server
from pathlib import Path
import subprocess
import sys
import threading


def der(tag, content):
    size = len(content)
    if size < 128:
        length = bytes([size])
    else:
        encoded = size.to_bytes((size.bit_length() + 7) // 8, "big")
        length = bytes([0x80 | len(encoded)]) + encoded
    return bytes([tag]) + length + content


def children(encoded):
    """Read children of a definite-length DER sequence from an OpenSSL certificate."""
    def bounds(offset):
        first = encoded[offset + 1]
        if first < 128:
            return offset + 2, offset + 2 + first
        count = first & 0x7f
        if count == 0 or count > 4:
            raise ValueError("Unsupported test certificate DER length")
        start = offset + 2 + count
        return start, start + int.from_bytes(encoded[offset + 2:start], "big")

    position, end = bounds(0)
    result = []
    while position < end:
        _, following = bounds(position)
        if following <= position or following > end:
            raise ValueError("Invalid test certificate DER")
        result.append(encoded[position:following])
        position = following
    return result


def openssl(*args, data=None):
    result = subprocess.run(["openssl", *args], input=data, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, timeout=20)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout


def build_container(document_digest):
    """Build detached SHA-256/RSA SignedData using signer.key, signer.der and ca.der in cwd."""
    if len(document_digest) != 32:
        raise ValueError("Expected a 32-byte SHA-256 document digest")
    signer = Path("signer.der").read_bytes()
    issuer = Path("ca.der").read_bytes()
    tbs = children(children(signer)[0])
    offset = 1 if tbs[0][0] == 0xa0 else 0
    issuer_and_serial = der(0x30, tbs[offset + 2] + tbs[offset])
    data_oid = bytes.fromhex("06092a864886f70d010701")
    signed_data_oid = bytes.fromhex("06092a864886f70d010702")
    sha256 = bytes.fromhex("300d06096086480165030402010500")
    rsa = bytes.fromhex("300d06092a864886f70d0101010500")
    content_type = der(0x30, bytes.fromhex("06092a864886f70d010903") + der(0x31, data_oid))
    message_digest = der(0x30, bytes.fromhex("06092a864886f70d010904")
                         + der(0x31, der(0x04, document_digest)))
    attributes = b"".join(sorted([content_type, message_digest]))
    signature = openssl("dgst", "-sha256", "-sign", "signer.key", data=der(0x31, attributes))
    signer_info = der(0x30, b"\x02\x01\x01" + issuer_and_serial + sha256
                      + der(0xa0, attributes) + rsa + der(0x04, signature))
    signed_data = der(0x30, b"\x02\x01\x01" + der(0x31, sha256) + der(0x30, data_oid)
                      + der(0xa0, b"".join(sorted([signer, issuer]))) + der(0x31, signer_info))
    return der(0x30, signed_data_oid + der(0xa0, signed_data))


def main():
    # Paths/port are supplied internally by the Java sample's fixed constants.
    directory = Path(sys.argv[1]).resolve()
    port = int(sys.argv[2])
    sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "SignatureLtvKmsTest"))
    from LocalSignatureServices import prepare_materials

    class Handler(http.server.BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass

        def reply(self, code, data, content_type):
            self.send_response(code)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)

        def do_GET(self):
            if self.path == "/ca.crl":
                self.reply(200, Path("ca.crl").read_bytes(), "application/pkix-crl")
            else:
                self.send_error(404)

        def do_POST(self):
            if self.path != "/cms":
                self.send_error(404)
                return
            if (self.headers.get("Content-Length") != "32"
                    or self.headers.get("X-Signature-Digest-Algorithm") != "SHA256"):
                self.send_error(400)
                return
            try:
                container = build_container(self.rfile.read(32))
                self.reply(200, container, "application/pkcs7-signature")
            except Exception as error:
                print("Local container service failed: " + str(error), file=sys.stderr, flush=True)
                self.send_error(500)

    server = http.server.HTTPServer(("127.0.0.1", port), Handler)
    started = False
    try:
        prepare_materials(directory, "test-only", "http://127.0.0.1:18080/tsa",
                          "http://127.0.0.1:%d/ca.crl" % port)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        started = True
        print("LOCAL_CONTAINER_SERVICE_READY", flush=True)
        sys.stdin.buffer.read()
    finally:
        if started:
            server.shutdown()
        server.server_close()


if __name__ == "__main__":
    main()
