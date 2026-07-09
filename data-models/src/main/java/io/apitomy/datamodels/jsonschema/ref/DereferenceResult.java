package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.jsonschema.JFullSchema;

import java.util.List;

/**
 * Result of dereferencing a JSON Schema document.
 *
 * @param schema the dereferenced schema (same instance, mutated in-place)
 * @param unresolvedRefs messages for references that could not be resolved
 *        (populated when {@link UnresolvableRefStrategy#COLLECT} is used)
 * @param hasCycles {@code true} if cyclic {@code $ref} values were detected.
 *        Cyclic references are left as-is ({@code $ref} field is preserved)
 *        because downstream tree traversals do not support object graph cycles.
 *        Callers comparing dereferenced schemas should handle remaining
 *        {@code $ref} fields via string comparison.
 */
public record DereferenceResult(JFullSchema schema, List<String> unresolvedRefs, boolean hasCycles) {
}
