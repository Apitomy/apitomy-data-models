package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefTraversal;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaDocument;

import java.util.Set;

/**
 * Entry point for JSON Schema backward compatibility checking.
 * <p>
 * Usage:
 * <pre>
 *   var result = JsonSchemaCompatibilityChecker.checkBackwardCompatibility(oldSchema, newSchema);
 *   if (!result.isCompatible()) {
 *       result.getIncompatibleDifferences().forEach(d -> ...);
 *   }
 * </pre>
 */
public final class JsonSchemaCompatibilityChecker {

    private JsonSchemaCompatibilityChecker() {
    }

    /**
     * Check if the updated schema is backward compatible with the original.
     * Backward compatible means: data written with the original schema can be read
     * using the updated schema.
     *
     * @param originalSchemaJson JSON text of the original schema
     * @param updatedSchemaJson  JSON text of the updated schema
     * @return the diff context with all found differences
     */
    public static DiffContext checkBackwardCompatibility(String originalSchemaJson, String updatedSchemaJson) {
        var originalDoc = parseSchema(originalSchemaJson);
        var updatedDoc = parseSchema(updatedSchemaJson);

        var refTraversal = JsonSchemaRefTraversal.withDefaults();
        var ctx = DiffContext.createRootContext("", null, refTraversal);

        flagModernVersions(ctx, originalDoc);
        flagModernVersions(ctx, updatedDoc);

        var originalAccessor = SchemaAccessor.wrap(originalDoc);
        var updatedAccessor = SchemaAccessor.wrap(updatedDoc);

        SchemaDiffVisitor.diffSchemas(ctx, originalAccessor, updatedAccessor);
        return ctx;
    }

    /**
     * Check if the updated schema is backward compatible with the original.
     */
    public static boolean isBackwardCompatible(String originalSchemaJson, String updatedSchemaJson) {
        return checkBackwardCompatibility(originalSchemaJson, updatedSchemaJson)
                .foundAllDifferencesAreCompatible();
    }

    /**
     * Get only the incompatible differences between two schemas.
     */
    public static Set<Difference> getIncompatibleDifferences(String originalSchemaJson,
                                                              String updatedSchemaJson) {
        return checkBackwardCompatibility(originalSchemaJson, updatedSchemaJson)
                .getIncompatibleDifferences();
    }

    /**
     * Check forward compatibility (updated schema can read data written with original).
     * Implemented by swapping the argument order.
     */
    public static boolean isForwardCompatible(String originalSchemaJson, String updatedSchemaJson) {
        return isBackwardCompatible(updatedSchemaJson, originalSchemaJson);
    }

    /**
     * Check full compatibility (both backward and forward compatible).
     */
    public static boolean isFullyCompatible(String originalSchemaJson, String updatedSchemaJson) {
        return isBackwardCompatible(originalSchemaJson, updatedSchemaJson)
                && isForwardCompatible(originalSchemaJson, updatedSchemaJson);
    }

    private static void flagModernVersions(DiffContext ctx, JsonSchemaDocument doc) {
        var modelType = doc.root().modelType();
        if (modelType == ModelType.JS201909 || modelType == ModelType.JS202012) {
            ctx.addUnsupported("JSON Schema %s (modern version support not yet implemented)".formatted(modelType));
        }
    }

    private static JsonSchemaDocument parseSchema(String schemaJson) {
        var doc = Library.readDocumentFromJSONString(schemaJson);
        if (!(doc instanceof JsonSchemaDocument jsonSchemaDoc)) {
            throw new IllegalArgumentException(
                    "Input is not a JSON Schema document. Detected type: " + doc.root().modelType());
        }
        return jsonSchemaDoc;
    }
}
