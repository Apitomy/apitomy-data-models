package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages reference resolution with caching and cycle detection.
 * <p>
 * This is the main entry point for resolving {@code $ref} during operations
 * like compatibility checking. It delegates to a {@link JsonSchemaRefResolver}
 * (typically a {@link JsonSchemaRefResolverChain}) and adds:
 * <ul>
 *   <li>Caching of resolved references (same $ref resolved once)</li>
 *   <li>Cycle detection to prevent infinite recursion on circular references</li>
 * </ul>
 * <p>
 * A future dereferencer would also use this class, adding inlining of resolved content.
 */
public class JsonSchemaRefTraversal {

    private final JsonSchemaRefResolver resolver;
    private final Map<String, ResolvedRef> cache = new HashMap<>();
    private final Set<String> visiting = new HashSet<>();

    public JsonSchemaRefTraversal(JsonSchemaRefResolver resolver) {
        this.resolver = resolver;
    }

    public static JsonSchemaRefTraversal withDefaults() {
        return new JsonSchemaRefTraversal(JsonSchemaRefResolverChain.withDefaults());
    }

    /**
     * Resolve a {@code $ref} value to a schema node.
     *
     * @param ref  the $ref string as it appears in the schema
     * @param from the node containing the $ref
     * @return the resolved node, or empty if unresolvable or a cycle was detected
     */
    public Optional<ResolvedRef> resolveRef(String ref, Node from) {
        if (ref == null) {
            return Optional.empty();
        }
        return resolveRef(JsonRef.parse(ref), from);
    }

    public Optional<ResolvedRef> resolveRef(JsonRef ref, Node from) {
        // TODO: Compute baseUri by walking up from 'from' to find $id values.
        //  For now, internal refs don't need base URI resolution.

        // Cache key includes document identity to avoid cross-document collisions
        var cacheKey = System.identityHashCode(from.root()) + ":" + ref.raw();

        var cached = cache.get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        if (visiting.contains(cacheKey)) {
            return Optional.empty();
        }

        visiting.add(cacheKey);
        try {
            var ctx = RefResolutionContext.builder(from).build();
            var result = resolver.resolve(ref, ctx);
            result.ifPresent(r -> cache.put(cacheKey, r));
            return result;
        } finally {
            visiting.remove(cacheKey);
        }
    }
}
