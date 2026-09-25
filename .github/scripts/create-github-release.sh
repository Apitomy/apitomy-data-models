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
#  - "Latest" goes to a final release only when it is newer than the current
#    Latest (is-latest-release.sh), whatever branch it is cut from. A patch to a
#    maintenance line is Latest while main is in a beta, and is not once main
#    has a newer final release.
EXTRA_ARGS=()
if [[ "$PRERELEASE" == "true" ]]; then
  EXTRA_ARGS+=(--prerelease --latest=false)
else
  # "release not found" means there is no Latest yet; any other failure is fatal,
  # so an unreadable Latest is never taken for "none".
  if CURRENT_LATEST=$(gh release view --json tagName --jq .tagName 2>"${RUNNER_TEMP:-/tmp}/latest-release.err"); then
    :
  elif grep -q "release not found" "${RUNNER_TEMP:-/tmp}/latest-release.err"; then
    CURRENT_LATEST=""
  else
    cat "${RUNNER_TEMP:-/tmp}/latest-release.err" >&2
    exit 1
  fi
  IS_LATEST=$("$(dirname "$0")/is-latest-release.sh" "$RELEASE_VERSION" false "$CURRENT_LATEST")
  echo "Current Latest release: '${CURRENT_LATEST:-none}'; $RELEASE_VERSION becomes Latest: $IS_LATEST"
  EXTRA_ARGS+=(--latest="$IS_LATEST")
fi

echo "Creating github release '$RELEASE_VERSION' for Repo '$GITHUB_REPOSITORY' and Branch: '$BRANCH' (prerelease: $PRERELEASE)"
gh release create "$RELEASE_VERSION" \
  --target "$BRANCH" \
  --title "$RELEASE_VERSION" \
  --generate-notes \
  "${EXTRA_ARGS[@]}"
echo "Github Release Created Successfully"
