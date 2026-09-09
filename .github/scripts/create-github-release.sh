#!/bin/bash
set -euxo pipefail

RELEASE_VERSION="$1"
BRANCH="$2"
PRERELEASE="${3:-false}"

# Two things hang off these flags:
#
#  - A pre-release must be marked as one. Otherwise GitHub fires the "released"
#    event, which update-website.yaml listens for, and a beta would bump the
#    public website. Prereleases fire "prereleased" instead.
#  - "Latest" is only meaningful for a GA release cut from the default branch.
#    A patch released from a maintenance branch is newer by date but older by
#    version, so it must not steal the Latest badge from the main line.
EXTRA_ARGS=()
if [[ "$PRERELEASE" == "true" ]]; then
  EXTRA_ARGS+=(--prerelease --latest=false)
elif [[ "$BRANCH" != "main" ]]; then
  EXTRA_ARGS+=(--latest=false)
fi

echo "Creating github release '$RELEASE_VERSION' for Repo '$GITHUB_REPOSITORY' and Branch: '$BRANCH' (prerelease: $PRERELEASE)"
gh release create "$RELEASE_VERSION" \
  --target "$BRANCH" \
  --title "$RELEASE_VERSION" \
  --generate-notes \
  "${EXTRA_ARGS[@]}"
echo "Github Release Created Successfully"
