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
    private final Map<String, Node> cache = new HashMap<>();
    // Stable per-document ids, replacing System.identityHashCode, which has no
    // transpiled equivalent. Model nodes do not override equals/hashCode, so this
    // map is keyed by identity. Scoped to this traversal, so it cannot grow unbounded.
    private final Map<Node, Integer> documentIds = new HashMap<>();
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
    public Optional<Node> resolveRef(String ref, Node from) {
        if (ref == null) {
            return Optional.empty();
        }
        return resolveRef(JsonRef.parse(ref), from);
    }

    public Optional<Node> resolveRef(JsonRef ref, Node from) {
        // TODO: Compute baseUri by walking up from 'from' to find $id values.
        //  For now, internal refs don't need base URI resolution.

        // Cache key includes document identity to avoid cross-document collisions
        String cacheKey = documentId((Node) from.root()) + ":" + ref.raw();

        Node cached = cache.get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        if (visiting.contains(cacheKey)) {
            return Optional.empty();
        }

        visiting.add(cacheKey);
        try {
            RefResolutionContext ctx = RefResolutionContext.builder(from).build();
            Optional<Node> result = resolver.resolve(ref, ctx);
            result.ifPresent(r -> cache.put(cacheKey, r));
            return result;
        } finally {
            visiting.remove(cacheKey);
        }
    }

    /** A stable id for a document root, assigned on first use. */
    private int documentId(Node root) {
        Integer id = documentIds.get(root);
        if (id == null) {
            id = documentIds.size() + 1;
            documentIds.put(root, id);
        }
        return id;
    }
}
