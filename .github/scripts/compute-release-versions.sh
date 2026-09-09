#!/bin/bash
#
# Decides which version gets released and which version the branch moves to
# afterwards.
#
# Usage: compute-release-versions.sh <current-version> [next-snapshot-version]
#
# Writes `key=value` lines to stdout, in the form the release workflow appends
# to $GITHUB_OUTPUT:
#
#   release-version=...    the version to publish
#   snapshot-version=...   the version the branch is set to afterwards
#   prerelease=true|false  whether the release is a pre-release
#
# Diagnostics go to stderr, so stdout stays safe to redirect into $GITHUB_OUTPUT.
#
# The POM is the single source of truth for what gets released: the release
# version is the current version with -SNAPSHOT stripped. Only the next
# snapshot needs a rule, and it depends on whether the release is a
# pre-release:
#
#   4.0.0-beta.1-SNAPSHOT -> release 4.0.0-beta.1, next 4.0.0-beta.2-SNAPSHOT
#   4.0.0-SNAPSHOT        -> release 4.0.0,        next 4.0.1-SNAPSHOT
#   3.1.4-SNAPSHOT        -> release 3.1.4,        next 3.1.5-SNAPSHOT
#
# That always continues the current line, which is right for routine releases
# but never moves between lines. Ending a beta line, or opening a new minor or
# major, is exactly that move -- hence the optional override.
#
set -euo pipefail

CURRENT_VERSION="${1:-}"
NEXT_SNAPSHOT_INPUT="${2:-}"

if [[ -z "$CURRENT_VERSION" ]]; then
  echo "::error::No current version given (expected the project version as \$1)." >&2
  exit 1
fi

RELEASE_VERSION="${CURRENT_VERSION%-SNAPSHOT}"

if [[ "$RELEASE_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)-(alpha|beta|rc)\.([0-9]+)$ ]]; then
  SNAPSHOT_VERSION="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.${BASH_REMATCH[3]}-${BASH_REMATCH[4]}.$((BASH_REMATCH[5] + 1))-SNAPSHOT"
  PRERELEASE=true
elif [[ "$RELEASE_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  SNAPSHOT_VERSION="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.$((BASH_REMATCH[3] + 1))-SNAPSHOT"
  PRERELEASE=false
else
  echo "::error::Cannot compute a release from project version '$CURRENT_VERSION'." >&2
  echo "::error::Expected MAJOR.MINOR.PATCH[-(alpha|beta|rc).N]-SNAPSHOT." >&2
  exit 1
fi

# An explicit next development version wins over the computed one. It is
# validated to the same shape the computation produces, because the value is
# committed straight back to the release branch. Whitespace is stripped first,
# since it is typed into a web form.
NEXT_SNAPSHOT_INPUT="${NEXT_SNAPSHOT_INPUT//[[:space:]]/}"
if [[ -n "$NEXT_SNAPSHOT_INPUT" ]]; then
  if [[ ! "$NEXT_SNAPSHOT_INPUT" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-(alpha|beta|rc)\.[0-9]+)?-SNAPSHOT$ ]]; then
    echo "::error::Invalid next-snapshot-version '$NEXT_SNAPSHOT_INPUT'." >&2
    echo "::error::Expected MAJOR.MINOR.PATCH[-(alpha|beta|rc).N]-SNAPSHOT." >&2
    exit 1
  fi
  if [[ "$NEXT_SNAPSHOT_INPUT" == "$CURRENT_VERSION" ]]; then
    echo "::error::next-snapshot-version '$NEXT_SNAPSHOT_INPUT' is the version being released from." >&2
    echo "::error::The branch would stay on it and the next release would repeat $RELEASE_VERSION." >&2
    exit 1
  fi
  echo "Overriding computed next snapshot $SNAPSHOT_VERSION with $NEXT_SNAPSHOT_INPUT" >&2
  SNAPSHOT_VERSION="$NEXT_SNAPSHOT_INPUT"
fi

echo "Release version: $RELEASE_VERSION (prerelease: $PRERELEASE)" >&2
echo "Next snapshot version: $SNAPSHOT_VERSION" >&2

echo "release-version=$RELEASE_VERSION"
echo "snapshot-version=$SNAPSHOT_VERSION"
echo "prerelease=$PRERELEASE"
