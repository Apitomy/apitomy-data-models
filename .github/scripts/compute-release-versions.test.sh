#!/bin/bash
#
# Tests for compute-release-versions.sh.
#
# This logic decides what gets published to Maven Central and NPM, and it
# cannot be exercised without cutting a real release -- so it is covered here
# instead. Run it directly:
#
#   ./.github/scripts/compute-release-versions.test.sh
#
set -uo pipefail

SCRIPT="$(dirname "$0")/compute-release-versions.sh"
failures=0

# Runs the real script and flattens its stdout to a single comparable line.
# A non-zero exit becomes "REJECTED", so the error cases assert that the
# release is refused rather than computed wrongly.
run() {
  local out
  if ! out=$("$SCRIPT" "$1" "${2:-}" 2>/dev/null); then
    echo "REJECTED"
    return
  fi
  # release-version=X\nsnapshot-version=Y\nprerelease=Z  ->  "X | Y | Z"
  echo "$out" | cut -d= -f2- | paste -sd'|' - | sed 's/|/ | /g'
}

check() { # <current-version> <next-snapshot-override> <expected>
  local got label
  got=$(run "$1" "$2")
  label="$1${2:+  +override=[$2]}"   # brackets make stray whitespace visible
  if [[ "$got" == "$3" ]]; then
    printf '  ok   %-52s -> %s\n' "$label" "$got"
  else
    printf '  FAIL %-52s -> %s\n       expected: %s\n' "$label" "$got" "$3"
    failures=$((failures + 1))
  fi
}

echo "The beta line advances itself:"
check "4.0.0-beta.1-SNAPSHOT" "" "4.0.0-beta.1 | 4.0.0-beta.2-SNAPSHOT | true"
check "4.0.0-beta.2-SNAPSHOT" "" "4.0.0-beta.2 | 4.0.0-beta.3-SNAPSHOT | true"
check "4.0.0-beta.9-SNAPSHOT" "" "4.0.0-beta.9 | 4.0.0-beta.10-SNAPSHOT | true"

echo "GA and the maintenance line bump the patch:"
check "4.0.0-SNAPSHOT" "" "4.0.0 | 4.0.1-SNAPSHOT | false"
check "3.1.4-SNAPSHOT" "" "3.1.4 | 3.1.5-SNAPSHOT | false"
check "3.1.9-SNAPSHOT" "" "3.1.9 | 3.1.10-SNAPSHOT | false"

echo "alpha and rc follow the same rule as beta:"
check "4.1.0-rc.1-SNAPSHOT" "" "4.1.0-rc.1 | 4.1.0-rc.2-SNAPSHOT | true"
check "5.0.0-alpha.3-SNAPSHOT" "" "5.0.0-alpha.3 | 5.0.0-alpha.4-SNAPSHOT | true"

echo "The override moves between lines -- ending the beta line:"
# The release itself is still a prerelease; the NEXT one is 4.0.0 GA.
check "4.0.0-beta.3-SNAPSHOT" "4.0.0-SNAPSHOT" "4.0.0-beta.3 | 4.0.0-SNAPSHOT | true"
check "4.0.0-beta.2-SNAPSHOT" "4.0.0-rc.1-SNAPSHOT" "4.0.0-beta.2 | 4.0.0-rc.1-SNAPSHOT | true"
check "4.0.0-SNAPSHOT" "4.1.0-SNAPSHOT" "4.0.0 | 4.1.0-SNAPSHOT | false"
check "3.1.4-SNAPSHOT" "3.2.0-SNAPSHOT" "3.1.4 | 3.2.0-SNAPSHOT | false"

echo "Whitespace from the web form is stripped, not rejected:"
TRAILING_SPACE="4.0.0-SNAPSHOT "   # deliberate trailing space
check "4.0.0-beta.1-SNAPSHOT" "$TRAILING_SPACE" "4.0.0-beta.1 | 4.0.0-SNAPSHOT | true"

echo "A malformed project version fails the release rather than mis-computing it:"
check "4.0.0-beta-SNAPSHOT" "" "REJECTED"
check "4.0.0.1-SNAPSHOT" "" "REJECTED"
check "4.0-SNAPSHOT" "" "REJECTED"
check "not-a-version" "" "REJECTED"
check "4.0.0-beta.x-SNAPSHOT" "" "REJECTED"

echo "So does a malformed override -- it is committed back to the branch:"
check "4.0.0-beta.1-SNAPSHOT" "4.0.0" "REJECTED"
check "4.0.0-beta.1-SNAPSHOT" "v4.0.0-SNAPSHOT" "REJECTED"
check "4.0.0-beta.1-SNAPSHOT" "4.0-SNAPSHOT" "REJECTED"
check "4.0.0-beta.1-SNAPSHOT" "4.0.0-beta-SNAPSHOT" "REJECTED"

echo "An override equal to the current version would repeat the release forever:"
check "4.0.0-beta.1-SNAPSHOT" "4.0.0-beta.1-SNAPSHOT" "REJECTED"

echo
if [[ $failures -eq 0 ]]; then
  echo "ALL PASS"
else
  echo "$failures FAILURE(S)"
  exit 1
fi
