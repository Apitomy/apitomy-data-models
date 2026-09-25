#!/bin/bash
#
# Decides whether a release becomes the "latest" one: GitHub's Latest release,
# which the website shows, and npm's "latest" dist-tag, which a plain
# `npm install` resolves.
#
# Usage: is-latest-release.sh <release-version> <prerelease> [current-latest]
#
# Prints "true" or "false". current-latest is the version that is latest now,
# on GitHub or on npm; empty when nothing has been released yet.
#
# The answer depends on versions, not on the branch a release is cut from:
#
#   - A pre-release is never latest.
#   - A final release is latest when it is newer than the current latest.
#
# Deciding by branch ("only main is latest") is right once main carries the
# stable line, but wrong while main is in a beta and a maintenance branch is the
# current stable line: a 3.1.4 patch would stay behind 3.1.3. And once main is
# stable again, a patch to an older line must not take npm's "latest" from it.
# Comparing versions gets both cases right without anything to switch by hand.
#
set -euo pipefail

RELEASE_VERSION="${1:-}"
PRERELEASE="${2:-}"
CURRENT_LATEST="${3:-}"

FINAL='^[0-9]+\.[0-9]+\.[0-9]+$'
PRE='^[0-9]+\.[0-9]+\.[0-9]+-(alpha|beta|rc)\.[0-9]+$'

if [[ "$PRERELEASE" != "true" && "$PRERELEASE" != "false" ]]; then
  echo "::error::Invalid prerelease flag '$PRERELEASE' (expected true or false)." >&2
  exit 1
fi
if [[ "$PRERELEASE" == "true" ]]; then
  if [[ ! "$RELEASE_VERSION" =~ $PRE ]]; then
    echo "::error::Invalid pre-release version '$RELEASE_VERSION'." >&2
    exit 1
  fi
  echo false
  exit 0
fi
if [[ ! "$RELEASE_VERSION" =~ $FINAL ]]; then
  echo "::error::Invalid release version '$RELEASE_VERSION' (expected MAJOR.MINOR.PATCH)." >&2
  exit 1
fi
if [[ -z "$CURRENT_LATEST" ]]; then
  echo true
  exit 0
fi
if [[ ! "$CURRENT_LATEST" =~ $FINAL ]]; then
  # Only final releases are ever latest, so anything else means the lookup went
  # wrong; refusing is safer than guessing which line is current.
  echo "::error::Unexpected current latest version '$CURRENT_LATEST'." >&2
  exit 1
fi
if [[ "$RELEASE_VERSION" == "$CURRENT_LATEST" ]]; then
  echo false
  exit 0
fi

# sort -V orders dotted numbers numerically (3.10.0 after 3.9.0).
NEWEST=$(printf '%s\n%s\n' "$RELEASE_VERSION" "$CURRENT_LATEST" | sort -V | tail -n 1)
if [[ "$NEWEST" == "$RELEASE_VERSION" ]]; then
  echo true
else
  echo false
fi
