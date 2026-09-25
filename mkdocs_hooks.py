"""MkDocs hooks for the Data Models documentation.

Replaces the release placeholders in every page while the site is built, so that
the install instructions name the version of this release line without the
Markdown naming it:

    {{ release_version }}   the released version, e.g. 4.0.0-beta.1
    {{ npm_package }}       what to pass to `npm install`: the package name, plus
                            the "beta" dist-tag for a pre-release, which is where
                            npm-publish.yaml puts pre-releases

The version is `extra.release_version` in mkdocs.yml, which the release
workflow sets to the version it releases. Only the built HTML contains the
values; the Markdown sources keep the placeholders.
"""

import re

from mkdocs.exceptions import PluginError

_PLACEHOLDER = re.compile(r"\{\{\s*(release_version|npm_package)\s*\}\}")


def on_config(config, **kwargs):
    version = config["extra"].get("release_version")
    if not version:
        raise PluginError("extra.release_version is not set in mkdocs.yml")
    return config


def on_page_markdown(markdown, page, config, files, **kwargs):
    version = config["extra"]["release_version"]
    values = {
        "release_version": version,
        # A pre-release version has a suffix (4.0.0-beta.1); a final one does not.
        "npm_package": "@apitomy/data-models" + ("@beta" if "-" in version else ""),
    }
    return _PLACEHOLDER.sub(lambda match: values[match.group(1)], markdown)
