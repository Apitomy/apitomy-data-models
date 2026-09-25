#!/bin/bash
#
# Tests for update-doc-versions.sh.
#
# The release workflow runs that script on the real documentation, which is only
# exercised by cutting a release, so it is covered here on a copy. Run it
# directly:
#
#   ./.github/scripts/update-doc-versions.test.sh
#
set -uo pipefail

SCRIPT="$(cd "$(dirname "$0")" && pwd)/update-doc-versions.sh"
failures=0

# A documentation tree with every snippet form, plus text that must not change:
# another artifact's version and the npm package name in an import.
make_tree() {
  local root
  root=$(mktemp -d)
  mkdir -p "$root/docs/user-guide" "$root/data-models/src/main/ts"
  cat > "$root/docs/index.md" <<'EOF'
    <dependency>
        <groupId>io.apitomy</groupId>
        <artifactId>apitomy-data-models</artifactId>
        <version>3.1.1</version>
    </dependency>
    <dependency>
        <artifactId>jackson-databind</artifactId>
        <version>2.18.0</version>
    </dependency>
    implementation 'io.apitomy:apitomy-data-models:3.1.1'
    npm install @apitomy/data-models
EOF
  cat > "$root/docs/user-guide/usage.md" <<'EOF'
    npm install @apitomy/data-models@beta
    import { Library } from '@apitomy/data-models';
EOF
  echo 'Install with `npm install @apitomy/data-models`.' > "$root/data-models/src/main/ts/README.md"
  echo "$root"
}

check() { # <description> <file> <expected text>
  if grep -qF -- "$3" "$2"; then
    printf '  ok   %s\n' "$1"
  else
    printf '  FAIL %s\n       expected to find: %s\n' "$1" "$3"
    failures=$((failures + 1))
  fi
}

check_absent() { # <description> <file> <unexpected text>
  if grep -qF -- "$3" "$2"; then
    printf '  FAIL %s\n       did not expect: %s\n' "$1" "$3"
    failures=$((failures + 1))
  else
    printf '  ok   %s\n' "$1"
  fi
}

echo "Pre-release"
root=$(make_tree)
"$SCRIPT" 4.0.0-beta.1 true "$root" 2>/dev/null
check "Maven version" "$root/docs/index.md" "<version>4.0.0-beta.1</version>"
check "other artifacts untouched" "$root/docs/index.md" "<version>2.18.0</version>"
check "Gradle version" "$root/docs/index.md" "apitomy-data-models:4.0.0-beta.1'"
check "npm names the beta dist-tag" "$root/docs/index.md" "npm install @apitomy/data-models@beta"
check "npm in nested pages" "$root/docs/user-guide/usage.md" "npm install @apitomy/data-models@beta"
check "imports untouched" "$root/docs/user-guide/usage.md" "from '@apitomy/data-models';"
check "npm package README" "$root/data-models/src/main/ts/README.md" "npm install @apitomy/data-models@beta"
rm -rf "$root"

echo "Final release"
root=$(make_tree)
"$SCRIPT" 3.1.4 false "$root" 2>/dev/null
check "Maven version" "$root/docs/index.md" "<version>3.1.4</version>"
check "Gradle version" "$root/docs/index.md" "apitomy-data-models:3.1.4'"
check_absent "npm drops a dist-tag" "$root/docs/user-guide/usage.md" "@beta"
check "npm plain install" "$root/docs/user-guide/usage.md" "npm install @apitomy/data-models"
rm -rf "$root"

echo "Refusals"
root=$(make_tree)
for args in "4.0.0-SNAPSHOT false" "4.0.0 yes" "" ; do
  # shellcheck disable=SC2086
  if "$SCRIPT" $args "$root" >/dev/null 2>&1; then
    printf '  FAIL accepted [%s]\n' "$args"
    failures=$((failures + 1))
  else
    printf '  ok   rejects [%s]\n' "$args"
  fi
done
rm -rf "$root/docs/index.md"
if "$SCRIPT" 4.0.0 false "$root" >/dev/null 2>&1; then
  printf '  FAIL accepted documentation without install snippets\n'
  failures=$((failures + 1))
else
  printf '  ok   rejects documentation without install snippets\n'
fi
rm -rf "$root"

if [[ "$failures" -gt 0 ]]; then
  echo "$failures check(s) failed"
  exit 1
fi
echo "All checks passed"
