package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.jsonschema.convert.CompoundSchemaConverter;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolver;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolverChain;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefTraversal;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;

import java.util.Set;

/**
 * Entry point for JSON Schema backward compatibility checking.
 * <p>
 * Schemas are first converted to a compound schema type that merges all
 * draft-version properties, then compared using the diff visitor infrastructure.
 * <p>
 * <b>Note:</b> This API is experimental and subject to change in future versions.
 */
public final class JsonSchemaCompatibilityChecker {

    private boolean allowCrossVersionChecking = false;

    public JsonSchemaCompatibilityChecker() {
    }

    public JsonSchemaCompatibilityChecker allowCrossVersionChecking(boolean allow) {
        this.allowCrossVersionChecking = allow;
        return this;
    }

    /**
     * Check if the updated schema is backward compatible with the original.
     */
    public DiffContext check(String originalSchemaJson, String updatedSchemaJson) {
        return check(originalSchemaJson, updatedSchemaJson,
                JsonSchemaRefResolverChain.withDefaults());
    }

    /**
     * Check backward compatibility using a custom reference resolver.
     */
    public DiffContext check(String originalSchemaJson, String updatedSchemaJson,
                              JsonSchemaRefResolver resolver) {
        java.util.Objects.requireNonNull(resolver, "resolver must not be null");
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

        // Convert both schemas to compound type
        var originalCompound = toCompoundFullSchema(originalParsed, originalModelType);
        var updatedCompound = toCompoundFullSchema(updatedParsed, updatedModelType);

        var refTraversal = new JsonSchemaRefTraversal(resolver);
        var ctx = DiffContext.createRootContext("", null, refTraversal);

        // TODO: Modern version support — flag for now, remove when diff classes handle all keywords
        flagModernVersions(ctx, originalModelType);
        flagModernVersions(ctx, updatedModelType);

        SchemaDiffVisitor.diffSchemas(ctx, originalCompound, updatedCompound);
        return ctx;
    }

    // --- Static convenience methods (backward-compatible API) ---

    // --- Static convenience methods (delegate to instance) ---

    public static DiffContext checkBackwardCompatibility(String originalSchemaJson, String updatedSchemaJson) {
        return new JsonSchemaCompatibilityChecker()
                .allowCrossVersionChecking(true)
                .check(originalSchemaJson, updatedSchemaJson);
    }

    public static DiffContext checkBackwardCompatibility(String originalSchemaJson, String updatedSchemaJson,
                                                          JsonSchemaRefResolver resolver) {
        return new JsonSchemaCompatibilityChecker()
                .allowCrossVersionChecking(true)
                .check(originalSchemaJson, updatedSchemaJson, resolver);
    }

    public static boolean isBackwardCompatible(String originalSchemaJson, String updatedSchemaJson) {
        return checkBackwardCompatibility(originalSchemaJson, updatedSchemaJson)
                .foundAllDifferencesAreCompatible();
    }

    public static Set<Difference> getIncompatibleDifferences(String originalSchemaJson,
                                                              String updatedSchemaJson) {
        return checkBackwardCompatibility(originalSchemaJson, updatedSchemaJson)
                .getIncompatibleDifferences();
    }

    public static boolean isForwardCompatible(String originalSchemaJson, String updatedSchemaJson) {
        return isBackwardCompatible(updatedSchemaJson, originalSchemaJson);
    }

    public static boolean isFullyCompatible(String originalSchemaJson, String updatedSchemaJson) {
        return isBackwardCompatible(originalSchemaJson, updatedSchemaJson)
                && isForwardCompatible(originalSchemaJson, updatedSchemaJson);
    }

    // --- Internal ---

    private static JFullSchema toCompoundFullSchema(JFullSchema doc, ModelType modelType) {
        // Convert to compound schema
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
