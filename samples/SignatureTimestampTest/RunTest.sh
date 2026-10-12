#!/usr/bin/env bash
set -euo pipefail

SAMPLE_DIR=$(cd "$(dirname "$0")" && pwd)
SAMPLES_DIR=$(cd "$SAMPLE_DIR/.." && pwd)
SDK_JAR="$SAMPLES_DIR/lib/compdfkit-3.2.0.jar"
CLASSES_DIR=$(mktemp -d)
trap 'rm -rf "$CLASSES_DIR"' EXIT

javac -encoding UTF-8 -cp "$SDK_JAR" -d "$CLASSES_DIR" \
    "$SAMPLES_DIR/common/CPDFLicense.java" \
    "$SAMPLES_DIR/common/SignatureSessionSupport.java" \
    "$SAMPLE_DIR/SignatureTimestampTest.java"
cd "$SAMPLE_DIR"
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 \
    -cp "$SDK_JAR:$CLASSES_DIR" SignatureTimestampTest
