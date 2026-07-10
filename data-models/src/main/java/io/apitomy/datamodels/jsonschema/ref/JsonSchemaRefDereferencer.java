package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Dereferences all {@code $ref} nodes in a JSON Schema tree by resolving them
 * and replacing them with the resolved content.
 * <p>
 * This class is independently usable outside the compatibility checker.
 *
 * <h3>Mutation</h3>
 * The schema tree is mutated in-place. Resolved nodes are inserted directly
 * (not cloned), which may create shared references when multiple {@code $ref}
 * nodes point to the same definition. Callers who need to preserve the
 * original tree should clone it before calling {@link #dereference}.
 *
 * <h3>Cycle handling</h3>
 * JSON Schema allows recursive references (e.g., a {@code Person} schema
 * whose {@code children} property references {@code Person} again). The
 * dereferencer handles both self-referencing and mutual recursion
 * (e.g., {@code Details} ↔ {@code Subject}).
 * <p>
 * Cycle detection uses an identity-based ancestry set: the dereferencer
 * tracks which nodes are on the current traversal path. When a {@code $ref}
 * resolves to a node that is already an ancestor, replacing it would create
 * an object graph cycle that downstream tree traversals (conversion, diff,
 * serialization) cannot handle. In this case, the {@code $ref} is left as-is
 * — only the <em>back-edge</em> that would close the loop is preserved.
 * Entry-point references into cyclic subtrees are resolved normally, so the
 * dereferenced tree contains at least one level of the cyclic structure.
 * <p>
 * Cyclic back-edges are reported in {@link DereferenceResult#cyclicRefs()}
 * as a map from the {@code $ref} string to the resolved target node. Callers
 * can use this map to follow cycles without re-resolving the {@code $ref}.
 * <p>
 * As a safety net, a configurable maximum recursion depth
 * (default {@value #DEFAULT_MAX_DEPTH}) prevents {@link StackOverflowError}
 * if cycle detection fails due to identity issues. A
 * {@link DereferenceException} is thrown when the limit is exceeded.
 *
 * <h3>Limitations</h3>
 * <ul>
 *   <li>Cycle detection relies on object identity ({@code ==}). If a
 *       {@link ResourceResolver} returns a new instance each time for the
 *       same logical document, cycles through external references will not
 *       be detected. Resolver implementations should return the same node
 *       instance for the same logical resource (see {@link ResourceResolver}
 *       javadoc).</li>
 *   <li>If two different {@code $ref} strings resolve to logically equal
 *       but identity-distinct documents, the dereferencer treats them as
 *       independent. Generated deep equality (G23) would address this
 *       in the future.</li>
 * </ul>
 *
 * @see DereferenceResult
 * @see ResourceResolver
 */
public class JsonSchemaRefDereferencer {

    static final int DEFAULT_MAX_DEPTH = 256;

    private final JsonSchemaRefTraversal refTraversal;
    private final UnresolvableRefStrategy strategy;
    private final int maxDepth;

    private JsonSchemaRefDereferencer(JsonSchemaRefTraversal refTraversal,
                                      UnresolvableRefStrategy strategy,
                                      int maxDepth) {
        this.refTraversal = refTraversal;
        this.strategy = strategy;
        this.maxDepth = maxDepth;
    }

    /**
     * Returns a new builder for configuring a {@link JsonSchemaRefDereferencer}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Dereferences all {@code $ref} nodes in the given schema tree.
     * <p>
     * The schema is mutated in-place — the returned
     * {@link DereferenceResult#schema()} is the same instance. After this
     * method returns:
     * <ul>
     *   <li>Non-cyclic {@code $ref} nodes have been replaced with their
     *       resolved targets (shared references, not clones).</li>
     *   <li>Cyclic back-edges retain their {@code $ref} string and are
     *       listed in {@link DereferenceResult#cyclicRefs()} with the
     *       resolved target node, so callers can follow the cycle without
     *       re-resolving.</li>
     *   <li>Unresolvable references are handled according to the configured
     *       {@link UnresolvableRefStrategy} and listed in
     *       {@link DereferenceResult#unresolvedRefs()} if using
     *       {@link UnresolvableRefStrategy#COLLECT}.</li>
     * </ul>
     *
     * @param schema the root schema to dereference
     * @return the dereference result
     * @throws ReferenceResolutionException if a reference cannot be resolved
     *         and {@link UnresolvableRefStrategy#FAIL} is configured, or if
     *         the reference resolver throws an exception
     * @throws DereferenceException if the maximum recursion depth is exceeded
     */
    public DereferenceResult dereference(JFullSchema schema) {
        var ctx = new DereferenceContext();
        dereferenceNode(schema, ctx);
        return new DereferenceResult(
                schema,
                List.copyOf(ctx.unresolvedRefs),
                Map.copyOf(ctx.cyclicRefs));
    }

    private void dereferenceNode(JFullSchema node, DereferenceContext ctx) {
        if (ctx.depth > maxDepth) {
            throw new DereferenceException(
                    "Maximum recursion depth (%d) exceeded during dereferencing. "
                    + "This may indicate a cycle that was not detected due to "
                    + "identity-distinct resolver results for the same logical document."
                    .formatted(maxDepth));
        }

        if (node instanceof Referenceable ref && ref.get$ref() != null) {
            var refValue = ref.get$ref();

            JFullSchema target;
            try {
                var resolved = refTraversal.resolveRef(refValue, node);
                if (resolved.isEmpty()) {
                    handleUnresolvable(refValue, ctx);
                    return;
                }
                target = (JFullSchema) resolved.get();
            } catch (ReferenceResolutionException e) {
                throw e;
            } catch (Exception e) {
                throw new ReferenceResolutionException(
                        "Failed to resolve $ref: " + refValue, refValue, e);
            }

            if (ctx.ancestry.contains(target)) {
                ctx.cyclicRefs.put(refValue, target);
                return;
            }

            replaceInParent(node, target);

            if (ctx.visited.add(target)) {
                ctx.ancestry.add(target);
                ctx.depth++;
                dereferenceChildren(target, ctx);
                ctx.depth--;
                ctx.ancestry.remove(target);
            }
            return;
        }

        if (!ctx.visited.add(node)) {
            return;
        }

        ctx.ancestry.add(node);
        ctx.depth++;
        dereferenceChildren(node, ctx);
        ctx.depth--;
        ctx.ancestry.remove(node);
    }

    private void dereferenceChildren(JFullSchema node, DereferenceContext ctx) {
        dereferenceUnion(node, "not", ctx);
        dereferenceUnion(node, "if", ctx);
        dereferenceUnion(node, "then", ctx);
        dereferenceUnion(node, "else", ctx);
        dereferenceUnion(node, "items", ctx);
        dereferenceUnion(node, "additionalItems", ctx);
        dereferenceUnion(node, "contains", ctx);
        dereferenceUnion(node, "additionalProperties", ctx);
        dereferenceUnion(node, "propertyNames", ctx);
        dereferenceUnion(node, "contentSchema", ctx);
        dereferenceUnion(node, "unevaluatedItems", ctx);
        dereferenceUnion(node, "unevaluatedProperties", ctx);

        dereferenceSchemaList(node, "allOf", ctx);
        dereferenceSchemaList(node, "anyOf", ctx);
        dereferenceSchemaList(node, "oneOf", ctx);
        dereferenceSchemaList(node, "prefixItems", ctx);

        dereferenceSchemaMap(node, "definitions", ctx);
        dereferenceSchemaMap(node, "$defs", ctx);
        dereferenceSchemaMap(node, "properties", ctx);
        dereferenceSchemaMap(node, "patternProperties", ctx);
        dereferenceSchemaMap(node, "dependencies", ctx);
        dereferenceSchemaMap(node, "dependentSchemas", ctx);
    }

    private void dereferenceUnion(Node parent, String propertyName, DereferenceContext ctx) {
        var value = NodeUtil.getProperty(parent, propertyName);
        if (value instanceof JFullSchema schema) {
            dereferenceNode(schema, ctx);
        }
    }

    private void dereferenceSchemaList(Node parent, String propertyName, DereferenceContext ctx) {
        var value = NodeUtil.getProperty(parent, propertyName);
        if (value instanceof List<?> list) {
            for (var item : list) {
                if (item instanceof JFullSchema schema) {
                    dereferenceNode(schema, ctx);
                } else if (item instanceof JsonSchema union && union.isFullSchema()) {
                    dereferenceNode(union.asFullSchema(), ctx);
                }
            }
        }
    }

    private void dereferenceSchemaMap(Node parent, String propertyName, DereferenceContext ctx) {
        var value = NodeUtil.getProperty(parent, propertyName);
        if (value instanceof Map<?, ?> map) {
            for (var entry : map.values()) {
                if (entry instanceof JFullSchema schema) {
                    dereferenceNode(schema, ctx);
                } else if (entry instanceof JsonSchema union && union.isFullSchema()) {
                    dereferenceNode(union.asFullSchema(), ctx);
                }
            }
        }
    }

    private void handleUnresolvable(String ref, DereferenceContext ctx) {
        switch (strategy) {
            case FAIL:
                throw new ReferenceResolutionException("Unresolvable $ref: " + ref, ref);
            case COLLECT:
                ctx.unresolvedRefs.add("Unresolvable $ref: " + ref);
                break;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void replaceInParent(Node refNode, Node target) {
        var parent = refNode.parent();
        if (parent == null) {
            if (refNode instanceof Referenceable ref) {
                ref.set$ref(null);
            }
            return;
        }

        var propName = refNode.parentPropertyName();
        var propType = refNode.parentPropertyType();

        switch (propType) {
            case standard:
                NodeUtil.setProperty(parent, propName, target);
                break;
            case array:
                var list = (List) NodeUtil.getProperty(parent, propName);
                if (list != null) {
                    int index = list.indexOf(refNode);
                    if (index >= 0) {
                        list.set(index, target);
                    }
                }
                break;
            case map:
                var map = (Map) NodeUtil.getProperty(parent, propName);
                if (map != null) {
                    var key = refNode.mapPropertyName();
                    if (key != null) {
                        map.put(key, target);
                    }
                }
                break;
        }
    }

    private static class DereferenceContext {
        final Set<Node> ancestry = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<String, JFullSchema> cyclicRefs = new LinkedHashMap<>();
        final List<String> unresolvedRefs = new ArrayList<>();
        int depth = 0;
    }

    /**
     * Builder for {@link JsonSchemaRefDereferencer}.
     */
    public static final class Builder {
        private JsonSchemaRefResolver refResolver;
        private UnresolvableRefStrategy strategy = UnresolvableRefStrategy.COLLECT;
        private int maxDepth = DEFAULT_MAX_DEPTH;

        private Builder() {
        }

        /**
         * Set the reference resolver used to resolve {@code $ref} values.
         *
         * @param resolver the resolver; must not be {@code null}
         * @return this builder
         */
        public Builder refResolver(JsonSchemaRefResolver resolver) {
            Objects.requireNonNull(resolver, "resolver must not be null");
            this.refResolver = resolver;
            return this;
        }

        /**
         * Set the strategy for handling unresolvable {@code $ref} values.
         * Defaults to {@link UnresolvableRefStrategy#COLLECT}.
         *
         * @param strategy the strategy
         * @return this builder
         */
        public Builder onUnresolvableRef(UnresolvableRefStrategy strategy) {
            Objects.requireNonNull(strategy, "strategy must not be null");
            this.strategy = strategy;
            return this;
        }

        /**
         * Set the maximum recursion depth for dereferencing.
         * Defaults to {@value #DEFAULT_MAX_DEPTH}. A {@link DereferenceException}
         * is thrown if the limit is exceeded.
         *
         * @param maxDepth the maximum depth; must be positive
         * @return this builder
         */
        public Builder maxDepth(int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive: " + maxDepth);
            }
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Builds the dereferencer.
         *
         * @return a new {@link JsonSchemaRefDereferencer} instance
         */
        public JsonSchemaRefDereferencer build() {
            if (refResolver == null) {
                refResolver = JsonSchemaRefResolverChain.withDefaults();
            }
            var refTraversal = new JsonSchemaRefTraversal(refResolver);
            return new JsonSchemaRefDereferencer(refTraversal, strategy, maxDepth);
        }
    }
}
