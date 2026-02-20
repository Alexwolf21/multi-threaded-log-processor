#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [[ $# -eq 0 ]]; then
  cat <<USAGE
Usage:
  ./scripts/run-local.sh --input <logs-dir> --output <summary.json> [--producers N] [--consumers N] [--queue-size N]

Example:
  ./scripts/run-local.sh --input ./logs --output ./summary.json --producers 2 --consumers 4 --queue-size 1000
USAGE
  exit 1
fi

"$ROOT_DIR/scripts/build.sh"
"$ROOT_DIR/scripts/run.sh" "$@"

OUTPUT_PATH=""
ARGS=("$@")
for ((i=0; i<${#ARGS[@]}; i++)); do
  if [[ "${ARGS[$i]}" == "--output" ]] && (( i + 1 < ${#ARGS[@]} )); then
    OUTPUT_PATH="${ARGS[$((i+1))]}"
    break
  fi
done

if [[ -n "$OUTPUT_PATH" ]]; then
  echo
  echo "Summary written to: $OUTPUT_PATH"
  if [[ -f "$OUTPUT_PATH" ]]; then
    echo "---- summary preview ----"
    cat "$OUTPUT_PATH"
    echo
    echo "-------------------------"
  fi
fi
