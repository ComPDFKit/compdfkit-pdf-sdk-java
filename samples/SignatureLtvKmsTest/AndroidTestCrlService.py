#!/usr/bin/env python3
"""Serve the Android test PKI's existing DER CRL without changing its CA or keys."""

import http.server
from pathlib import Path
import sys
import threading
from urllib.parse import urlsplit


def main():
    directory = Path(sys.argv[1]).resolve()
    url = urlsplit(sys.argv[2])
    if (
        url.scheme != "http"
        or url.hostname != "127.0.0.1"
        or url.path != "/test.crl"
        or url.query
        or url.fragment
        or url.username
    ):
        raise ValueError("Test CRL service requires http://127.0.0.1:<port>/test.crl")
    crl = (directory / "test.crl").read_bytes()

    class Handler(http.server.BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass

        def do_GET(self):
            if self.path != "/test.crl":
                self.send_error(404)
                return
            self.send_response(200)
            self.send_header("Content-Type", "application/pkix-crl")
            self.send_header("Content-Length", str(len(crl)))
            self.end_headers()
            self.wfile.write(crl)

    # Never stop another process if the port is already occupied.
    server = http.server.HTTPServer(("127.0.0.1", url.port or 80), Handler)
    try:
        threading.Thread(target=server.serve_forever, daemon=True).start()
        print("LOCAL_SIGNATURE_SERVICES_READY", flush=True)
        sys.stdin.buffer.read()
    finally:
        server.shutdown()
        server.server_close()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("Android test CRL service: " + str(error), file=sys.stderr, flush=True)
        sys.exit(1)
