package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.jsonschema.compat.JsonSchemaCompatibilityException;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Dereferences all {@code $ref} nodes in a JSON Schema tree by resolving them
 * and replacing them with the resolved content.
 * <p>
 * After dereferencing, no {@code $ref} fields remain in the tree (unless they
 * could not be resolved and the {@link UnresolvableRefStrategy} allows it).
 * Cyclic references produce object graph cycles — the {@code $ref} field is
 * still cleared, but the node points back to an ancestor.
 * <p>
 * The schema is mutated in-place. Resolved nodes are used directly (not
 * cloned), which may create shared references in the tree. Downstream
 * processing (e.g., conversion to compound schema) creates independent copies
 * from the shared graph.
 * <p>
 * This class is independently usable outside the compatibility checker.
 *
 * @see DereferenceResult
 */
public class JsonSchemaRefDereferencer {

    private final JsonSchemaRefTraversal refTraversal;
    private final UnresolvableRefStrategy strategy;

    private JsonSchemaRefDereferencer(JsonSchemaRefTraversal refTraversal,
                                      UnresolvableRefStrategy strategy) {
        this.refTraversal = refTraversal;
        this.strategy = strategy;
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
     * The schema is mutated in-place. The returned result contains the same
     * schema instance, a list of unresolved reference messages (if
     * {@link UnresolvableRefStrategy#COLLECT} is used), and a flag indicating
     * whether any cycles were detected.
     *
     * @param schema the root schema to dereference
     * @return the dereference result
     * @throws JsonSchemaCompatibilityException if a reference cannot be resolved
     *         and {@link UnresolvableRefStrategy#FAIL} is configured
     */
    public DereferenceResult dereference(JFullSchema schema) {
        var ctx = new DereferenceContext();

        // Phase 1: find all refs that participate in cycles.
        // These cannot be inlined because the conversion traverser has no
        // cycle detection and would infinite-loop on the resulting object graph.
        findCyclicRefs(schema, ctx, new HashSet<>());

        // Phase 2: resolve non-cyclic refs by replacing $ref nodes with targets
        dereferenceNode(schema, ctx);

        return new DereferenceResult(schema, List.copyOf(ctx.unresolvedRefs), ctx.hasCycles);
    }

    private void findCyclicRefs(JFullSchema node, DereferenceContext ctx, Set<String> stack) {
        if (node instanceof Referenceable ref && ref.get$ref() != null) {
            var refValue = ref.get$ref();
            if (stack.contains(refValue)) {
                ctx.cyclicRefs.add(refValue);
                ctx.hasCycles = true;
                // Also mark all refs in the current stack — they're all part of the cycle
                ctx.cyclicRefs.addAll(stack);
                return;
            }

            var resolved = refTraversal.resolveRef(refValue, node);
            if (resolved.isPresent()) {
                stack.add(refValue);
                findCyclicRefs((JFullSchema) resolved.get(), ctx, stack);
                stack.remove(refValue);
            }
            return;
        }

        findCyclicRefsInChildren(node, ctx, stack);
    }

    private void findCyclicRefsInChildren(JFullSchema node, DereferenceContext ctx, Set<String> stack) {
        findCyclicRefsInUnion(node, "not", ctx, stack);
        findCyclicRefsInUnion(node, "if", ctx, stack);
        findCyclicRefsInUnion(node, "then", ctx, stack);
        findCyclicRefsInUnion(node, "else", ctx, stack);
        findCyclicRefsInUnion(node, "items", ctx, stack);
        findCyclicRefsInUnion(node, "additionalItems", ctx, stack);
        findCyclicRefsInUnion(node, "contains", ctx, stack);
        findCyclicRefsInUnion(node, "additionalProperties", ctx, stack);
        findCyclicRefsInUnion(node, "propertyNames", ctx, stack);
        findCyclicRefsInUnion(node, "contentSchema", ctx, stack);
        findCyclicRefsInUnion(node, "unevaluatedItems", ctx, stack);
        findCyclicRefsInUnion(node, "unevaluatedProperties", ctx, stack);
        findCyclicRefsInList(node, "allOf", ctx, stack);
        findCyclicRefsInList(node, "anyOf", ctx, stack);
        findCyclicRefsInList(node, "oneOf", ctx, stack);
        findCyclicRefsInList(node, "prefixItems", ctx, stack);
        findCyclicRefsInMap(node, "definitions", ctx, stack);
        findCyclicRefsInMap(node, "$defs", ctx, stack);
        findCyclicRefsInMap(node, "properties", ctx, stack);
        findCyclicRefsInMap(node, "patternProperties", ctx, stack);
        findCyclicRefsInMap(node, "dependencies", ctx, stack);
        findCyclicRefsInMap(node, "dependentSchemas", ctx, stack);
    }

    private void findCyclicRefsInUnion(Node parent, String prop, DereferenceContext ctx, Set<String> stack) {
        var value = NodeUtil.getProperty(parent, prop);
        if (value instanceof JFullSchema s) findCyclicRefs(s, ctx, stack);
    }

    private void findCyclicRefsInList(Node parent, String prop, DereferenceContext ctx, Set<String> stack) {
        var value = NodeUtil.getProperty(parent, prop);
        if (value instanceof List<?> list) {
            for (var item : list) {
                if (item instanceof JFullSchema s) findCyclicRefs(s, ctx, stack);
                else if (item instanceof JsonSchema u && u.isFullSchema()) findCyclicRefs(u.asFullSchema(), ctx, stack);
            }
        }
    }

    private void findCyclicRefsInMap(Node parent, String prop, DereferenceContext ctx, Set<String> stack) {
        var value = NodeUtil.getProperty(parent, prop);
        if (value instanceof Map<?, ?> map) {
            for (var entry : map.values()) {
                if (entry instanceof JFullSchema s) findCyclicRefs(s, ctx, stack);
                else if (entry instanceof JsonSchema u && u.isFullSchema()) findCyclicRefs(u.asFullSchema(), ctx, stack);
            }
        }
    }

    private void dereferenceNode(JFullSchema node, DereferenceContext ctx) {
        if (!ctx.visited.add(System.identityHashCode(node))) {
            return;
        }

        if (node instanceof Referenceable ref && ref.get$ref() != null) {
            var refValue = ref.get$ref();

            if (ctx.cyclicRefs.contains(refValue)) {
                // Cyclic ref — leave as-is
                return;
            }

            var resolved = refTraversal.resolveRef(refValue, node);
            if (resolved.isEmpty()) {
                handleUnresolvable(refValue, ctx);
            } else {
                var target = (JFullSchema) resolved.get();
                replaceInParent(node, target);
                dereferenceNode(target, ctx);
                return;
            }
        }

        dereferenceChildren(node, ctx);
    }

    private void dereferenceChildren(JFullSchema node, DereferenceContext ctx) {
        // Single schema properties
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

        // List schema properties
        dereferenceSchemaList(node, "allOf", ctx);
        dereferenceSchemaList(node, "anyOf", ctx);
        dereferenceSchemaList(node, "oneOf", ctx);
        dereferenceSchemaList(node, "prefixItems", ctx);

        // Map schema properties
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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
        if (ctx.unresolvableRefs.contains(ref)) {
            return;
        }
        switch (strategy) {
            case FAIL:
                throw new JsonSchemaCompatibilityException("Unresolvable $ref: " + ref);
            case COLLECT:
                ctx.unresolvedRefs.add("Unresolvable $ref: " + ref);
                ctx.unresolvableRefs.add(ref);
                break;
            case IGNORE:
                ctx.unresolvableRefs.add(ref);
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
        final Set<String> cyclicRefs = new HashSet<>();
        final Set<Integer> visited = new HashSet<>();
        final List<String> unresolvedRefs = new ArrayList<>();
        final Set<String> unresolvableRefs = new HashSet<>();
        boolean hasCycles = false;
    }

    /**
     * Builder for {@link JsonSchemaRefDereferencer}.
     */
    public static final class Builder {
        private JsonSchemaRefResolver refResolver;
        private UnresolvableRefStrategy strategy = UnresolvableRefStrategy.COLLECT;

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
         * Builds the dereferencer.
         *
         * @return a new {@link JsonSchemaRefDereferencer} instance
         */
        public JsonSchemaRefDereferencer build() {
            if (refResolver == null) {
                refResolver = JsonSchemaRefResolverChain.withDefaults();
            }
            var refTraversal = new JsonSchemaRefTraversal(refResolver);
            return new JsonSchemaRefDereferencer(refTraversal, strategy);
        }
    }
}
