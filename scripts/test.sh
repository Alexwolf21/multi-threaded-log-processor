#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

rm -rf out
mkdir -p out/main out/test

javac -d out/main $(find src/main/java -name '*.java')
javac -cp out/main -d out/test $(find src/test/java -name '*.java')

java -cp out/main:out/test com.example.logproc.TestRunner
