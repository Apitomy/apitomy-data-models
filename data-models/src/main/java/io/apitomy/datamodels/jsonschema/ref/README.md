# JSON Schema Reference Resolution

## JsonRef — Parsed `$ref` Value

A `$ref` value in JSON Schema is a URI-reference ([RFC 3986](https://www.rfc-editor.org/rfc/rfc3986))
with an optional fragment. `JsonRef` parses the raw string into typed parts:

```
     resource        fragment
    ┌───┴────┐ ┌────────┴─────────┐
    other.json#/definitions/Address
              │└────────┬─────────┘
              #       pointer
```

### Structure

A `JsonRef` has three parts:

| Part | Method | Description |
|------|--------|-------------|
| **resource** | `resource()` | The part before `#`. Identifies the external document (URL, relative path, etc.). Null for same-document references. |
| **pointer** | `pointer()` | A parsed JSON Pointer ([RFC 6901](https://www.rfc-editor.org/rfc/rfc6901)). Non-null only when the fragment starts with `/`. |
| **anchor** | `anchor()` | An anchor name. Non-null only when the fragment is a plain string (not starting with `/`). |

The **fragment** is the raw string after `#`. It is interpreted as either a pointer or an anchor — never both.
The `fragment()` method returns the raw fragment string regardless of interpretation.

### Fragment Types

The fragment determines whether `pointer()` or `anchor()` is non-null:

| Fragment format | `isPointer()` | `isAnchor()` | Example |
|----------------|---------------|--------------|---------|
| Starts with `/` | true | false | `#/definitions/Address` → pointer with segments `["definitions", "Address"]` |
| Plain string | false | true | `#Address` → anchor name `"Address"` |
| Empty (`#` alone) | false | false | `#` → root reference |
| Absent (no `#`) | false | false | `other.json` → external ref, no fragment |

### Examples

| `$ref` value | `resource()` | `isPointer()` | `pointer()` segments | `isAnchor()` | `anchor()` | `isInternal()` |
|-------------|-------------|---------------|---------------------|-------------|-----------|----------------|
| `#/definitions/Foo` | null | true | `["definitions", "Foo"]` | false | null | true |
| `#/$defs/Bar` | null | true | `["$defs", "Bar"]` | false | null | true |
| `#Address` | null | false | null | true | `"Address"` | true |
| `#` | null | false | null | false | null | true |
| `other.json` | `"other.json"` | false | null | false | null | false |
| `other.json#/defs/X` | `"other.json"` | true | `["defs", "X"]` | false | null | false |
| `https://example.com/s.json` | `"https://example.com/s.json"` | false | null | false | null | false |

### Anchors Across Drafts

The anchor mechanism changed across JSON Schema drafts:

- **Draft-4:** `"id": "#Address"` — the `id` keyword with a fragment-only value acts as an anchor
- **Draft-6/7:** `"$id": "#Address"` — same but renamed to `$id`
- **Draft 2019-09+:** `"$anchor": "Address"` — dedicated keyword, fragment `#` prefix is not part of the value

In all cases, the reference uses `"$ref": "#Address"` and `JsonRef` parses it as `anchor() == "Address"`.

## JsonPointer — JSON Pointer (RFC 6901)

A JSON Pointer is a sequence of reference tokens separated by `/`. Each token identifies a
child of the current node:

- Object property: token is the property name
- Array element: token is the index as a decimal string

Tokens use `~` escaping: `~0` represents `~` and `~1` represents `/` (unescape order matters).

The `evaluate(Node root)` method walks the document tree following the pointer segments.

## Reference Resolution Architecture

```
JsonSchemaRefTraversal          — caching + cycle detection
    └── JsonSchemaRefResolverChain  — tries resolvers in order
            ├── LocalPointerRefResolver — #/definitions/... (JSON Pointer)
            ├── AnchorRefResolver       — #anchorName ($anchor, $id fragments)
            └── (future resolvers: URL, Registry, etc.)
```

Each resolver receives a parsed `JsonRef` and a `RefResolutionContext` (source node + base URI).
Returns `Optional<ResolvedRef>` — empty if it can't handle the reference.

`ResolvedRef` contains the resolved `Node` and whether it's `attached` (part of the document tree)
or `detached` (parsed separately, e.g., from an external source).
