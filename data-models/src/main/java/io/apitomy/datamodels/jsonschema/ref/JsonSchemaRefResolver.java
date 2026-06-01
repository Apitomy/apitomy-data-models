package io.apitomy.datamodels.jsonschema.ref;

import java.util.Optional;

/**
 * Resolves a JSON Schema {@code $ref} to a schema node.
 * <p>
 * Implementations handle specific reference types (internal pointers, anchors,
 * external URLs, registry lookups, etc.) and return {@link Optional#empty()}
 * for references they cannot handle.
 */
@FunctionalInterface
public interface JsonSchemaRefResolver {

    /**
     * Attempt to resolve the given {@code $ref} value.
     *
     * @param ref     the parsed {@code $ref} reference
     * @param context resolution context providing the source node, base URI, and extensible attributes
     * @return the resolved reference, or empty if this resolver cannot handle it
     */
    Optional<ResolvedRef> resolve(JsonRef ref, RefResolutionContext context);
}
