#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAR_PATH="$ROOT_DIR/logproc.jar"

if [[ ! -f "$JAR_PATH" ]]; then
  echo "logproc.jar not found at: $JAR_PATH"
  echo "Run ./scripts/build.sh first."
  exit 1
fi

exec java -jar "$JAR_PATH" "$@"
