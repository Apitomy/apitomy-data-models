#!/bin/bash
set -euxo pipefail

RELEASE_VERSION="$1"
BRANCH="$2"

echo "Creating github release '$RELEASE_VERSION' for Repo '$GITHUB_REPOSITORY' and Branch: '$BRANCH'"
gh release create "$RELEASE_VERSION" \
  --target "$BRANCH" \
  --title "$RELEASE_VERSION" \
  --generate-notes
echo "Github Release Created Successfully"
