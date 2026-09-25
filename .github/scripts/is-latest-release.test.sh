#!/bin/bash
#
# Tests for is-latest-release.sh.
#
# It decides which release GitHub marks Latest and which one npm's "latest"
# dist-tag points to, and it cannot be exercised without cutting a real
# release -- so it is covered here instead. Run it directly:
#
#   ./.github/scripts/is-latest-release.test.sh
#
set -uo pipefail

SCRIPT="$(dirname "$0")/is-latest-release.sh"
failures=0

check() { # <release-version> <prerelease> <current-latest> <expected: true|false|REJECTED> <why>
  local got
  if ! got=$("$SCRIPT" "$1" "$2" "$3" 2>/dev/null); then
    got="REJECTED"
  fi
  if [[ "$got" == "$4" ]]; then
    printf '  ok   %-38s -> %-8s %s\n' "$1 (pre=$2, now=${3:-none})" "$got" "$5"
  else
    printf '  FAIL %-38s -> %-8s expected %s: %s\n' "$1 (pre=$2, now=${3:-none})" "$got" "$4" "$5"
    failures=$((failures + 1))
  fi
}

check 4.0.0-beta.1 true  3.1.3 false    "a pre-release is never latest"
check 3.1.4        false 3.1.3 true     "a 3.x patch during the 4.x beta"
check 4.0.0        false 3.1.4 true     "the 4.0.0 GA"
check 3.1.5        false 4.0.0 false    "a 3.x patch after the 4.x GA"
check 4.0.1        false 4.0.0 true     "a routine patch"
check 3.10.0       false 3.9.0 true     "versions compare numerically"
check 3.1.3        false 3.1.3 false    "already latest"
check 1.0.0        false ""    true     "the first release"
check 4.0.0-SNAPSHOT false 3.1.3 REJECTED "a snapshot is not a release"
check 4.0.0        maybe 3.1.3 REJECTED "the flag must be true or false"
check 4.0.0-beta.1 false 3.1.3 REJECTED "a pre-release version needs the flag"
check 4.0.0        false 4.0.0-beta.1 REJECTED "a pre-release cannot be the current latest"

if [[ "$failures" -gt 0 ]]; then
  echo "$failures check(s) failed"
  exit 1
fi
echo "All checks passed"
