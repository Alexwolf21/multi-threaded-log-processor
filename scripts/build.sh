#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

rm -rf out
mkdir -p out/main

javac -d out/main $(find src/main/java -name '*.java')
jar --create --file logproc.jar --main-class com.example.logproc.LogProcessorApp -C out/main .

echo "Built logproc.jar"
