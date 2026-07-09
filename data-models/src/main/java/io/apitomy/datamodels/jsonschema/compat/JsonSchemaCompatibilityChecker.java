package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.jsonschema.convert.CompoundSchemaConverter;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefDereferencer;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;

/**
 * Entry point for JSON Schema compatibility checking.
 * <p>
 * Schemas are parsed from JSON strings, optionally dereferenced, converted to a
 * compound schema type that merges all draft-version properties, then compared
 * using the diff visitor infrastructure.
 * <p>
 * The checker parses its own copies of the input JSON strings — the caller's
 * data is never modified.
 * <p>
 * Use the {@link #builder()} method to create and configure an instance:
 * <pre>{@code
 * var checker = JsonSchemaCompatibilityChecker.builder()
 *     .allowCrossVersionChecking(true)
 *     .dereferencer(deref)
 *     .build();
 *
 * CompatibilityCheckResult result = checker.checkBackward(original, updated);
 * if (!result.isCompatible()) {
 *     result.getIncompatibleDifferences().forEach(System.out::println);
 * }
 * }</pre>
 * <p>
 * Instances are immutable and safe to reuse across multiple checks.
 * <p>
 * <b>Note:</b> This API is experimental and subject to change in future versions.
 */
public final class JsonSchemaCompatibilityChecker {

    private final boolean allowCrossVersionChecking;
    private final JsonSchemaRefDereferencer dereferencer;

    /**
     * Package-private constructor — use {@link #builder()} to create instances.
     */
    JsonSchemaCompatibilityChecker(boolean allowCrossVersionChecking,
                                   JsonSchemaRefDereferencer dereferencer) {
        this.allowCrossVersionChecking = allowCrossVersionChecking;
        this.dereferencer = dereferencer;
    }

    /**
     * Returns a new builder for configuring a {@link JsonSchemaCompatibilityChecker}.
     *
     * @return a new builder instance
     */
    public static JsonSchemaCompatibilityCheckerBuilder builder() {
        return new JsonSchemaCompatibilityCheckerBuilder();
    }

    /**
     * Check backward compatibility: data written with the original schema can
     * be read by a consumer using the updated schema.
     *
     * @param originalJson the original JSON Schema document as a JSON string
     * @param updatedJson  the updated JSON Schema document as a JSON string
     * @return the result of the backward compatibility check
     * @throws IllegalArgumentException if the input is not a valid JSON Schema,
     *         or if cross-version checking is disabled and the schemas use different draft versions
     */
    public CompatibilityCheckResult checkBackward(String originalJson, String updatedJson) {
        return new CompatibilityCheckResult(doCheck(originalJson, updatedJson));
    }

    /**
     * Check forward compatibility: data written with the updated schema can
     * be read by a consumer using the original schema.
     * <p>
     * This is equivalent to checking backward compatibility with the arguments swapped.
     *
     * @param originalJson the original JSON Schema document as a JSON string
     * @param updatedJson  the updated JSON Schema document as a JSON string
     * @return the result of the forward compatibility check
     * @throws IllegalArgumentException if the input is not a valid JSON Schema,
     *         or if cross-version checking is disabled and the schemas use different draft versions
     */
    public CompatibilityCheckResult checkForward(String originalJson, String updatedJson) {
        return new CompatibilityCheckResult(doCheck(updatedJson, originalJson));
    }

    /**
     * Check full compatibility (both backward and forward).
     * <p>
     * This performs both a backward and a forward compatibility check and
     * combines the results into a {@link FullCompatibilityCheckResult}.
     *
     * @param originalJson the original JSON Schema document as a JSON string
     * @param updatedJson  the updated JSON Schema document as a JSON string
     * @return the combined result of both compatibility checks
     * @throws IllegalArgumentException if the input is not a valid JSON Schema,
     *         or if cross-version checking is disabled and the schemas use different draft versions
     */
    public FullCompatibilityCheckResult checkFull(String originalJson, String updatedJson) {
        var backward = checkBackward(originalJson, updatedJson);
        var forward = checkForward(originalJson, updatedJson);
        return new FullCompatibilityCheckResult(backward, forward);
    }

    // --- Internal ---

    private DiffContext doCheck(String originalSchemaJson, String updatedSchemaJson) {
        var originalParsed = parseSchema(originalSchemaJson);
        var updatedParsed = parseSchema(updatedSchemaJson);

        var originalModelType = originalParsed.root().modelType();
        var updatedModelType = updatedParsed.root().modelType();

        if (!allowCrossVersionChecking && originalModelType != updatedModelType) {
            throw new IllegalArgumentException(
                    "Cross-version checking is not enabled. Original: " + originalModelType
                    + ", Updated: " + updatedModelType
                    + ". Use allowCrossVersionChecking(true) to enable.");
        }

        var ctx = DiffContext.createRootContext();

        // Dereference if configured
        if (dereferencer != null) {
            var origResult = dereferencer.dereference(originalParsed);
            var updResult = dereferencer.dereference(updatedParsed);
            origResult.unresolvedRefs().forEach(ctx::addUnsupported);
            updResult.unresolvedRefs().forEach(ctx::addUnsupported);
        }

        // Convert both schemas to compound type
        var originalCompound = toCompoundFullSchema(originalParsed, originalModelType);
        var updatedCompound = toCompoundFullSchema(updatedParsed, updatedModelType);

        // TODO: Modern version support — flag for now, remove when diff classes handle all keywords
        flagModernVersions(ctx, originalModelType);
        flagModernVersions(ctx, updatedModelType);

        CompoundSchemaDiffVisitor.diffSchemas(ctx, originalCompound, updatedCompound);
        return ctx;
    }

    private static JFullSchema toCompoundFullSchema(JFullSchema doc, ModelType modelType) {
        JsonSchema compound = CompoundSchemaConverter.toCompound((JsonSchema) doc, modelType);
        if (compound instanceof JFullSchema) {
            return (JFullSchema) compound;
        }
        throw new IllegalArgumentException("Failed to convert schema to compound type");
    }

    private static void flagModernVersions(DiffContext ctx, ModelType modelType) {
        if (modelType == ModelType.JM201909 || modelType == ModelType.JM202012) {
            ctx.addUnsupported("JSON Schema %s (modern version support not yet implemented)".formatted(modelType));
        }
    }

    private static JFullSchema parseSchema(String schemaJson) {
        var doc = Library.readRootFromJSONString(schemaJson);
        if (!(doc instanceof JFullSchema jsonSchemaDoc)) {
            throw new IllegalArgumentException(
                    "Input is not a JSON Schema document. Detected type: " + doc.root().modelType());
        }
        return jsonSchemaDoc;
    }
}
