#!/usr/bin/env bash
set -euo pipefail

TEST_NAME=SignatureContainerTest
SAMPLE_DIR=$(cd "$(dirname "$0")" && pwd)
ROOT_DIR=$(cd "$SAMPLE_DIR/.." && pwd)
LIB_DIR="$ROOT_DIR/lib"
COMMON_DIR="$ROOT_DIR/common"

# Use the same versioned SDK JAR directory as the other samples.
JAR_NAME=$(ls "$LIB_DIR" | grep -E '^compdfkit-[0-9][0-9.]*\.jar$' | sort -V | tail -n 1) || true
if [[ -z "$JAR_NAME" ]]; then
    echo "Error: no compdfkit-*.jar found in $LIB_DIR" >&2
    exit 1
fi
if ! jar tf "$LIB_DIR/$JAR_NAME" | grep -Fx 'com/compdfkit/core/signature/CPDFExternalSignatureSigner.class' > /dev/null; then
    echo "Error: $LIB_DIR/$JAR_NAME does not contain the external container APIs. Build the current SDK and update the JAR in samples/lib." >&2
    exit 1
fi

CLASSES_DIR=$(mktemp -d)
trap 'rm -rf "$CLASSES_DIR"' EXIT
cd "$SAMPLE_DIR"
javac -encoding UTF-8 -cp "$LIB_DIR/$JAR_NAME" -d "$CLASSES_DIR" \
    "$COMMON_DIR/CPDFLicense.java" "$TEST_NAME.java" "HttpContainerProvider.java"
java -Djava.awt.headless=true -Djava.library.path="$LIB_DIR" \
    -cp "$CLASSES_DIR:$LIB_DIR/$JAR_NAME" "$TEST_NAME"
