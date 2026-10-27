#!/usr/bin/env bash
# Sends N requests through the gateway and decides whether the canary is healthy.
#
#   PASS  -> the canary answered at least one request and produced 0 errors
#   FAIL  -> any canary (or stable) error, or the canary received no traffic  => rollback
#
# Usage: deploy/verify-canary.sh [requests=50] [url=http://localhost:8080]
set -uo pipefail

REQUESTS="${1:-50}"
URL="${2:-http://localhost:8080}"

stable_ok=0; stable_err=0; canary_ok=0; canary_err=0

for _ in $(seq 1 "$REQUESTS"); do
  out="$(curl -s -o /dev/null -D - -w 'HTTPCODE:%{http_code}' "$URL/api/products" || true)"
  code="${out##*HTTPCODE:}"
  track="$(printf '%s' "$out" | tr -d '\r' | awk -F': ' 'tolower($1)=="x-app-track"{print $2}')"

  if [[ "$track" == "canary" ]]; then
    if [[ "$code" == "200" ]]; then canary_ok=$((canary_ok+1)); else canary_err=$((canary_err+1)); fi
  else
    if [[ "$code" == "200" ]]; then stable_ok=$((stable_ok+1)); else stable_err=$((stable_err+1)); fi
  fi
done

canary_total=$((canary_ok+canary_err))
stable_total=$((stable_ok+stable_err))
share=$(( canary_total * 100 / REQUESTS ))

if [[ "$canary_total" -gt 0 && "$canary_err" -eq 0 && "$stable_err" -eq 0 ]]; then
  verdict="PASS"
else
  verdict="FAIL"
fi

report="
| Track  | Requests | OK | Errors |
|--------|---------:|---:|-------:|
| stable | ${stable_total} | ${stable_ok} | ${stable_err} |
| canary | ${canary_total} | ${canary_ok} | ${canary_err} |

Canary traffic share: ${share} %  ->  verdict: **${verdict}**"

echo "$report"
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  { echo "### Canary verification"; echo "$report"; } >> "$GITHUB_STEP_SUMMARY"
fi

[[ "$verdict" == "PASS" ]]
