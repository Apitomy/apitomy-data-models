package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.jsonschema.JFullSchema;

import java.util.List;
import java.util.Map;

/**
 * Result of dereferencing a JSON Schema document.
 *
 * @param schema the dereferenced schema (same instance, mutated in-place)
 * @param unresolvedRefs messages for references that could not be resolved
 *        (populated when {@link UnresolvableRefStrategy#COLLECT} is used)
 * @param cyclicRefs map of cyclic back-edge {@code $ref} strings to their
 *        resolved target nodes. These {@code $ref} values were left in the
 *        tree because replacing them would create an object graph cycle.
 *        Callers can use this map to follow cycles without re-resolving
 *        the {@code $ref} strings.
 */
public record DereferenceResult(
        JFullSchema schema,
        List<String> unresolvedRefs,
        Map<String, JFullSchema> cyclicRefs) {

    /**
     * Returns {@code true} if any cyclic {@code $ref} values were detected.
     */
    public boolean hasCycles() {
        return !cyclicRefs.isEmpty();
    }
}
