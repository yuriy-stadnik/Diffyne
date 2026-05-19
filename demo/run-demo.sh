#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_FILE="${SCRIPT_DIR}/latest-output.json"

docker compose -f "${SCRIPT_DIR}/docker-compose.yml" up -d --build

for _ in $(seq 1 60); do
  if curl -fsS "http://localhost:8081/api/direct-comparisons/rest-api" \
    -H "Content-Type: application/json" \
    -d @"${SCRIPT_DIR}/request.json" \
    -o "${OUTPUT_FILE}"; then
    printf 'Saved Diffyne comparison output to %s\n' "${OUTPUT_FILE}"
    exit 0
  fi
  sleep 2
done

printf 'Diffyne did not become ready on http://localhost:8081 within 120 seconds.\n' >&2
exit 1
