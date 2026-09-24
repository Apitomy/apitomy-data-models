# Schema Compatibility

The library includes a JSON Schema compatibility checker for detecting breaking changes
between schema versions. This is useful when evolving APIs to ensure that updated schemas
remain compatible with existing data.

!!! warning "Experimental"
    This API is experimental and subject to change in future versions.

## Compatibility Types

| Type | Meaning |
|------|---------|
| **Backward compatible** | Data written with the *original* schema can be read using the *updated* schema |
| **Forward compatible** | Data written with the *updated* schema can be read using the *original* schema |
| **Fully compatible** | Both backward and forward compatible |

---

## Creating a Checker

Build a checker with `JsonSchemaCompatibilityChecker.builder()`. Instances are immutable and
safe to reuse across any number of checks, so build one and keep it.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.compat.JsonSchemaCompatibilityChecker;

    JsonSchemaCompatibilityChecker checker = JsonSchemaCompatibilityChecker.builder().build();
    ```

=== "TypeScript"

    ```typescript
    import { JsonSchemaCompatibilityChecker } from '@apitomy/data-models';

    const checker = JsonSchemaCompatibilityChecker.builder().build();
    ```

The checker parses its own copies of the input JSON strings — the schemas you pass in are never
modified.

---

## Quick Checks

Each check returns a result object; `isCompatible()` is the pass/fail answer.

=== "Java"

    ```java
    String original = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string" },
            "age": { "type": "integer" }
          },
          "required": ["name"]
        }
        """;

    String updated = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string" },
            "age": { "type": "integer" },
            "email": { "type": "string" }
          },
          "required": ["name"]
        }
        """;

    // Adding an optional property is backward compatible
    boolean backwardOk = checker.checkBackward(original, updated).isCompatible();
    System.out.println("Backward compatible: " + backwardOk); // true

    // Check forward compatibility
    boolean forwardOk = checker.checkForward(original, updated).isCompatible();

    // Check full compatibility (both directions)
    boolean fullyOk = checker.checkFull(original, updated).isFullyCompatible();
    ```

=== "TypeScript"

    ```typescript
    const original = JSON.stringify({
        type: 'object',
        properties: {
            name: { type: 'string' },
            age: { type: 'integer' },
        },
        required: ['name'],
    });

    const updated = JSON.stringify({
        type: 'object',
        properties: {
            name: { type: 'string' },
            age: { type: 'integer' },
            email: { type: 'string' },
        },
        required: ['name'],
    });

    // Adding an optional property is backward compatible
    const backwardOk = checker.checkBackward(original, updated).isCompatible();
    console.log('Backward compatible:', backwardOk); // true

    // Check forward compatibility
    const forwardOk = checker.checkForward(original, updated).isCompatible();

    // Check full compatibility (both directions)
    const fullyOk = checker.checkFull(original, updated).isFullyCompatible();
    ```

`checkFull` returns a `FullCompatibilityCheckResult`, which also exposes
`isBackwardCompatible()`, `isForwardCompatible()`, and the two underlying results via
`getBackwardResult()` and `getForwardResult()`.

---

## Detailed Diff

The result carries every difference found, and separately those that break compatibility.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.compat.CompatibilityCheckResult;
    import io.apitomy.datamodels.jsonschema.compat.Difference;

    CompatibilityCheckResult result = checker.checkBackward(original, updated);

    if (result.isCompatible()) {
        System.out.println("All changes are backward compatible.");
    } else {
        System.out.println("Incompatible changes found:");
        for (Difference diff : result.getIncompatibleDifferences()) {
            System.out.println("  " + diff.getDiffType() + " at " + diff.getPathUpdated());
        }
    }
    ```

=== "TypeScript"

    ```typescript
    const result = checker.checkBackward(original, updated);

    if (result.isCompatible()) {
        console.log('All changes are backward compatible.');
    } else {
        console.log('Incompatible changes found:');
        result.getIncompatibleDifferences().forEach(diff => {
            console.log(`  ${diff.getDiffType()} at ${diff.getPathUpdated()}`);
        });
    }
    ```

`getDifferences()` returns everything detected, compatible or not — useful for change logs
rather than gates.

Each difference is reported where the change is. Its path is a `JsonPointer` into the schema,
ending in the keyword that changed; `toString()` gives the familiar string form. Tightening a
nested property, for example, reports the keyword inside it rather than the property as a whole:

```
STRING_TYPE_MAX_LENGTH_DECREASED at /properties/name/maxLength
```

A difference that concerns a subschema as a whole, such as a property schema replaced with
`false`, points at the subschema itself (`/properties/name`). A comparison that only matches
alternatives, such as the branches of an `anyOf`, is reported once, at the keyword
(`/properties/choice/anyOf`).

### The result as a tree

`getRoot()` returns the same differences grouped by where they are, for example to display them.
The root node is the whole schema; each child is a nested schema — a property's schema, the
schema of an array's items — that has a difference at or below it. `flatten()` on any node lists
every difference in its subtree, and `getDifferences()` on the result is `getRoot().flatten()`.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.compat.DifferenceNode;

    static void print(DifferenceNode node, String indent) {
        System.out.println(indent + node.getPathUpdated()
                + (node.isCompatible() ? "" : "  (incompatible)"));
        for (Difference diff : node.getDifferences()) {
            System.out.println(indent + "  " + diff.getShortDescription());
        }
        for (DifferenceNode child : node.getChildren()) {
            print(child, indent + "  ");
        }
    }

    print(result.getRoot(), "");
    ```

=== "TypeScript"

    ```typescript
    function print(node: DifferenceNode, indent: string): void {
        console.log(`${indent}${node.getPathUpdated()}${node.isCompatible() ? '' : '  (incompatible)'}`);
        node.getDifferences().forEach(diff => console.log(`${indent}  ${diff.getShortDescription()}`));
        node.getChildren().forEach(child => print(child, indent + '  '));
    }

    print(result.getRoot(), '');
    ```

### Explaining a difference

Each `Difference` can describe itself, which is handy when surfacing results to a user rather
than failing a build.

=== "Java"

    ```java
    for (Difference diff : result.getIncompatibleDifferences()) {
        System.out.println(diff.getShortDescription());
        diff.getHelp().ifPresent(help -> System.out.println("  " + help));

        // Worked examples of this kind of change
        diff.getExamples().forEach(example ->
                System.out.println("  e.g. " + example.getId()));
    }
    ```

=== "TypeScript"

    ```typescript
    result.getIncompatibleDifferences().forEach(diff => {
        console.log(diff.getShortDescription());

        const help = diff.getHelp();
        if (help) {
            console.log(`  ${help}`);
        }

        // Worked examples of this kind of change
        diff.getExamples().forEach(example => console.log(`  e.g. ${example.getId()}`));
    });
    ```

!!! note "`getHelp()` differs by language"
    In Java it returns `Optional<String>`; in TypeScript it returns the string directly, which
    may be absent. The examples above reflect that.

`getPathOriginal()` and `getPathUpdated()` locate the change in each schema, in the keywords that
schema uses: for a draft-7 tuple the path is `/items/0/…`, for 2020-12 `/prefixItems/0/…`, even
when the two schemas of a cross-version check differ. The path of a tree node resolves against
the schema it belongs to with `JsonPointer.evaluate`. A difference in one member of a list or
map points at that member on the side that has it: a name added to `required` is at
`/required/1` in the updated schema and at `/required` in the original. A difference does not
copy the values it refers to; the paths lead to them.

---

## Unsupported Features

`getUnsupportedFeatures()` lists the parts of the comparison that could not be fully carried
out. Today that means exactly one thing: **references the dereferencer could not resolve**.

It is therefore only ever populated when you have [configured a
dereferencer](#resolving-references). Without one, the list is always empty.

=== "Java"

    ```java
    CompatibilityCheckResult result = checker.checkBackward(original, updated);

    if (result.hasUnsupportedFeatures()) {
        System.out.println("Not fully compared: " + result.getUnsupportedFeatures());
    }
    ```

=== "TypeScript"

    ```typescript
    const result = checker.checkBackward(original, updated);

    if (result.hasUnsupportedFeatures()) {
        console.log('Not fully compared:', result.getUnsupportedFeatures());
    }
    ```

Each entry names the reference that failed to resolve:

```
Unresolvable $ref: http://schemas.example.com/address.json
```

Both schemas are dereferenced, so the same reference can appear twice — once per side — and the
entry does not say which side it came from.

!!! warning "Check this before trusting a pass"
    A result can be `isCompatible() == true` while `hasUnsupportedFeatures()` is also true. The
    sub-schemas behind an unresolved reference were never compared, so the verdict covers less
    than it appears to. Gate on both:

    ```java
    boolean safe = result.isCompatible() && !result.hasUnsupportedFeatures();
    ```

!!! note "Without a dereferencer, `$ref`s are compared as strings"
    The checker does not silently ignore them: two schemas whose `$ref` strings differ are
    reported as different. But two schemas sharing a `$ref` string are treated as equal at that
    point, even if the document it points to has changed underneath them. Configure a
    dereferencer whenever the referenced content itself might move.

---

## Resolving References

By default a `$ref` that the checker cannot resolve is reported as an unsupported feature.
Supplying a dereferencer inlines references first, so the comparison runs against complete
schemas.

=== "Java"

    ```java
    JsonSchemaCompatibilityChecker checker = JsonSchemaCompatibilityChecker.builder()
            .dereferencer(myDereferencer)
            .build();
    ```

=== "TypeScript"

    ```typescript
    const checker = JsonSchemaCompatibilityChecker.builder()
        .dereferencer(myDereferencer)
        .build();
    ```

See [Dereferencing](dereferencing.md) for building a `JsonSchemaRefDereferencer`.

---

## Common Breaking Changes

The checker detects a wide range of schema differences. Here are some common examples:

| Change | Backward Compatible? |
|--------|---------------------|
| Adding a property to `properties` | **No** — see note below |
| Adding a required property | **No** |
| Removing a required property | Yes |
| Widening a type (e.g., `integer` → `number`) | Yes |
| Narrowing a type (e.g., `number` → `integer`) | **No** |
| Increasing `maxLength` / `maximum` | Yes |
| Decreasing `maxLength` / `maximum` | **No** |
| Decreasing `minLength` / `minimum` | Yes |
| Increasing `minLength` / `minimum` | **No** |
| Adding an enum member | Yes |
| Removing an enum member | **No** |
| Setting `additionalProperties: false` | **No** |

!!! note "Adding a property is not a safe change in JSON Schema"
    This is the row that most often surprises people, because the equivalent change *is* safe in
    Avro and Protobuf. In JSON Schema, a name absent from `properties` is unconstrained, so
    adding `"m": {"type": "string"}` starts rejecting `{"m": 42}`, which the original schema
    accepted. The checker reports it as `OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED`.

    Adding the name to `required` as well is a second, separate incompatibility.

---

## Supported Versions

All supported drafts can be compared: **Draft 4, Draft 6, Draft 7, 2019-09 and 2020-12**. Each
schema is converted to a compound representation that merges the draft-specific properties, and
the comparison runs against that, so the draft a schema is written in does not change which
differences are detected.

By default both schemas must use the **same** draft; comparing across drafts throws
`IllegalArgumentException`. Opt in when that is what you want:

=== "Java"

    ```java
    JsonSchemaCompatibilityChecker checker = JsonSchemaCompatibilityChecker.builder()
            .allowCrossVersionChecking(true)
            .build();
    ```

=== "TypeScript"

    ```typescript
    const checker = JsonSchemaCompatibilityChecker.builder()
        .allowCrossVersionChecking(true)
        .build();
    ```

!!! info "Changed in 4.0"
    The 3.x checker supported only Draft 4, 6 and 7, and exposed static methods
    (`isBackwardCompatible`, `checkBackwardCompatibility`) that no longer exist. See the
    [3.x → 4.0 migration guide](../migration/3.x-to-4.0.md#json-schema-compatibility-checking)
    for the mapping.
