package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.jsonschema.JFullSchema;

import java.util.List;
import java.util.Map;

/**
 * Result of dereferencing a JSON Schema document.
 * <p>
 * Written out as a class rather than a record because records do not survive
 * transpilation to TypeScript. The accessors keep the record's naming, so this
 * is source-compatible with the record form.
 */
public final class DereferenceResult {

    private final JFullSchema schema;
    private final List<String> unresolvedRefs;
    private final Map<String, JFullSchema> cyclicRefs;

    /**
     * @param schema the dereferenced schema (same instance, mutated in-place)
     * @param unresolvedRefs messages for references that could not be resolved
     *        (populated when {@link UnresolvableRefStrategy#COLLECT} is used)
     * @param cyclicRefs map of cyclic back-edge {@code $ref} strings to their
     *        resolved target nodes. These {@code $ref} values were left in the
     *        tree because replacing them would create an object graph cycle.
     *        Callers can use this map to follow cycles without re-resolving
     *        the {@code $ref} strings.
     */
    public DereferenceResult(JFullSchema schema, List<String> unresolvedRefs,
            Map<String, JFullSchema> cyclicRefs) {
        this.schema = schema;
        this.unresolvedRefs = unresolvedRefs;
        this.cyclicRefs = cyclicRefs;
    }

    /**
     * The dereferenced schema (the same instance that was passed in, mutated
     * in place).
     */
    public JFullSchema schema() {
        return schema;
    }

    /**
     * Messages for references that could not be resolved, populated when
     * {@link UnresolvableRefStrategy#COLLECT} is used.
     */
    public List<String> unresolvedRefs() {
        return unresolvedRefs;
    }

    /**
     * Cyclic back-edge {@code $ref} strings mapped to their resolved target
     * nodes. These {@code $ref} values were left in the tree because replacing
     * them would create an object graph cycle.
     */
    public Map<String, JFullSchema> cyclicRefs() {
        return cyclicRefs;
    }

    /**
     * Returns {@code true} if any cyclic {@code $ref} values were detected.
     */
    public boolean hasCycles() {
        return !cyclicRefs.isEmpty();
    }
}
