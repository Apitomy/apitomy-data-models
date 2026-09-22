# Migration

Guides for moving between major versions of Apitomy Data Models. Each guide lists the breaking
changes for that upgrade and shows the replacement for every API that was removed or changed.

## Guides

### [3.x → 4.0](3.x-to-4.0.md)

The JSON Schema rewrite. Modern draft support (2019-09 and 2020-12) with compound schema
conversion, a rebuilt compatibility checker, a new reference dereferencer, and the whole JSON
Schema API available in TypeScript for the first time.

Also covers the model-level changes that reached OpenAPI, AsyncAPI and OpenRPC as a consequence
of that work.

---

## Version support

| Line | Status | Branch |
|---|---|---|
| 4.0.x | Active development | `main` |
| 3.1.x | Maintenance — fixes only | `3.1.x` |

Releases from the 3.1.x line continue on the `3.1.x` branch. If you are not ready to upgrade,
pin to the latest 3.1.x release; it is not a dead end.
