#!/usr/bin/env bash
# Turns Maven Surefire XML reports into a markdown table on the GitHub Actions run page.
# Usage: scripts/test-summary.sh "Title"
shopt -s nullglob
title="${1:-Test results}"
out="${GITHUB_STEP_SUMMARY:-/dev/stdout}"

total=0; failed=0; skipped=0
rows=""

for f in target/surefire-reports/TEST-*.xml; do
  line="$(grep -m1 '<testsuite ' "$f")"
  name="$(echo "$line" | sed -E 's/.* name="([^"]+)".*/\1/' | awk -F. '{print $NF}')"
  t="$(echo "$line" | sed -E 's/.* tests="([0-9]+)".*/\1/')"
  e="$(echo "$line" | sed -E 's/.* errors="([0-9]+)".*/\1/')"
  s="$(echo "$line" | sed -E 's/.* skipped="([0-9]+)".*/\1/')"
  x="$(echo "$line" | sed -E 's/.* failures="([0-9]+)".*/\1/')"
  bad=$((e + x))
  total=$((total + t)); failed=$((failed + bad)); skipped=$((skipped + s))
  icon=":white_check_mark:"; [[ $bad -gt 0 ]] && icon=":x:"; [[ $bad -eq 0 && $s -gt 0 && $s -eq $t ]] && icon=":fast_forward:"
  rows+="| ${icon} ${name} | ${t} | $((t - bad - s)) | ${bad} | ${s} |"$'\n'
done

{
  echo "### ${title}"
  echo ""
  echo "| Test class | Tests | Passed | Failed | Skipped |"
  echo "|---|---:|---:|---:|---:|"
  printf '%s' "$rows"
  echo ""
  echo "**Total: ${total} tests - ${failed} failed - ${skipped} skipped**"
} >> "$out"
