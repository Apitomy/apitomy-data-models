#!/bin/bash
#
# Points the install instructions in the documentation at a release.
#
# Usage: update-doc-versions.sh <release-version> <prerelease> [root]
#
# Run by the release workflow after the POM version is set, so that the release
# commit carries documentation naming the version it publishes, and the
# documentation publish triggered by that commit shows it. Without it, the
# install snippets name whichever version someone last typed in.
#
# Rewrites, in every Markdown file under docs/ and in the npm package README:
#
#   <artifactId>apitomy-data-models</artifactId> <version>X</version>   (Maven)
#   io.apitomy:apitomy-data-models:X                                     (Gradle)
#   npm install @apitomy/data-models[@tag]                               (npm)
#
# A pre-release is published to npm under the "beta" dist-tag (see
# npm-publish.yaml), so its install line names the tag; a final release goes to
# "latest", which needs none.
#
# Fails when it finds nothing to rewrite, so that a reworded snippet is noticed
# at the next release instead of silently going stale.
#
set -euo pipefail

RELEASE_VERSION="${1:-}"
PRERELEASE="${2:-}"
ROOT="${3:-.}"

if [[ ! "$RELEASE_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-(alpha|beta|rc)\.[0-9]+)?$ ]]; then
  echo "::error::Invalid release version '$RELEASE_VERSION'." >&2
  echo "::error::Expected MAJOR.MINOR.PATCH[-(alpha|beta|rc).N]." >&2
  exit 1
fi
if [[ "$PRERELEASE" != "true" && "$PRERELEASE" != "false" ]]; then
  echo "::error::Invalid prerelease flag '$PRERELEASE' (expected true or false)." >&2
  exit 1
fi

NPM_SUFFIX=""
if [[ "$PRERELEASE" == "true" ]]; then
  NPM_SUFFIX="@beta"
fi

FILES=()
while IFS= read -r -d '' file; do
  FILES+=("$file")
done < <(find "$ROOT/docs" -name '*.md' -print0 2>/dev/null)
if [[ -f "$ROOT/data-models/src/main/ts/README.md" ]]; then
  FILES+=("$ROOT/data-models/src/main/ts/README.md")
fi

# The Maven and Gradle snippets, passed to perl through the environment so that
# their slashes and quotes need no escaping.
export MAVEN='<artifactId>apitomy-data-models</artifactId>\s*<version>[^<]*</version>'
export GRADLE='io\.apitomy:apitomy-data-models:[0-9A-Za-z.+-]+'

found=0
for file in "${FILES[@]}"; do
  count=$(perl -0ne '$n += () = /$ENV{MAVEN}|$ENV{GRADLE}/g; END { print $n + 0 }' "$file")
  if [[ "$count" -gt 0 ]]; then
    found=$((found + count))
    echo "Updating $count version reference(s) in $file" >&2
  fi
  VERSION="$RELEASE_VERSION" SUFFIX="$NPM_SUFFIX" perl -0pi -e '
    s{(<artifactId>apitomy-data-models</artifactId>\s*<version>)[^<]*(</version>)}{$1$ENV{VERSION}$2}g;
    s{io\.apitomy:apitomy-data-models:[0-9A-Za-z.+-]+}{io.apitomy:apitomy-data-models:$ENV{VERSION}}g;
    s{npm install \@apitomy/data-models(?:\@[0-9A-Za-z.-]+)?}{npm install \@apitomy/data-models$ENV{SUFFIX}}g;
  ' "$file"
done

if [[ "$found" -eq 0 ]]; then
  echo "::error::No Maven or Gradle install snippet found in the documentation under $ROOT." >&2
  echo "::error::If the snippets were reworded, update .github/scripts/update-doc-versions.sh." >&2
  exit 1
fi

echo "Documentation now names $RELEASE_VERSION (npm: @apitomy/data-models$NPM_SUFFIX)" >&2
