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

## Quick Checks

Use the boolean convenience methods for simple pass/fail checks.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.compat.JsonSchemaCompatibilityChecker;

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
    boolean backwardOk = JsonSchemaCompatibilityChecker.isBackwardCompatible(original, updated);
    System.out.println("Backward compatible: " + backwardOk); // true

    // Check forward compatibility
    boolean forwardOk = JsonSchemaCompatibilityChecker.isForwardCompatible(original, updated);

    // Check full compatibility (both directions)
    boolean fullyOk = JsonSchemaCompatibilityChecker.isFullyCompatible(original, updated);
    ```

=== "TypeScript"

    ```typescript
    import { JsonSchemaCompatibilityChecker } from '@apitomy/data-models';

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
    const backwardOk = JsonSchemaCompatibilityChecker.isBackwardCompatible(original, updated);
    console.log('Backward compatible:', backwardOk); // true

    // Check forward compatibility
    const forwardOk = JsonSchemaCompatibilityChecker.isForwardCompatible(original, updated);

    // Check full compatibility (both directions)
    const fullyOk = JsonSchemaCompatibilityChecker.isFullyCompatible(original, updated);
    ```

---

## Detailed Diff

Use `checkBackwardCompatibility()` to get a `DiffContext` with all detected differences,
including which are compatible and which are not.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.compat.DiffContext;
    import io.apitomy.datamodels.jsonschema.compat.Difference;

    DiffContext result = JsonSchemaCompatibilityChecker.checkBackwardCompatibility(
            original, updated);

    if (result.foundAllDifferencesAreCompatible()) {
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
    import { JsonSchemaCompatibilityChecker, DiffContext } from '@apitomy/data-models';

    const result = JsonSchemaCompatibilityChecker.checkBackwardCompatibility(
        original, updated);

    if (result.foundAllDifferencesAreCompatible()) {
        console.log('All changes are backward compatible.');
    } else {
        console.log('Incompatible changes found:');
        result.getIncompatibleDifferences().forEach(diff => {
            console.log(`  ${diff.getDiffType()} at ${diff.getPathUpdated()}`);
        });
    }
    ```

You can also get just the incompatible differences directly:

=== "Java"

    ```java
    Set<Difference> breaking = JsonSchemaCompatibilityChecker.getIncompatibleDifferences(
            original, updated);
    ```

=== "TypeScript"

    ```typescript
    const breaking = JsonSchemaCompatibilityChecker.getIncompatibleDifferences(
        original, updated);
    ```

---

## Common Breaking Changes

The checker detects a wide range of schema differences. Here are some common examples:

| Change | Backward Compatible? |
|--------|---------------------|
| Adding an optional property | Yes |
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

---

## Supported Versions

The compatibility checker currently supports JSON Schema Draft 4, Draft 6, and Draft 7.
Modern versions (2019-09, 2020-12) are detected but flagged as unsupported in the diff
context via `hasUnsupportedFeatures()`.
