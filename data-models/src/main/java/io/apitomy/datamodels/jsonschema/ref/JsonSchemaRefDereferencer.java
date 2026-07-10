package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.jsonschema.compat.JsonSchemaCompatibilityException;
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
 * After dereferencing, no {@code $ref} fields remain in the tree unless they
 * are cyclic back-edges or could not be resolved. Cyclic back-edges are left
 * as {@code $ref} strings and reported in
 * {@link DereferenceResult#cyclicRefs()}.
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
     * {@link UnresolvableRefStrategy#COLLECT} is used), and a map of cyclic
     * back-edge references that were left as {@code $ref} strings.
     *
     * @param schema the root schema to dereference
     * @return the dereference result
     * @throws JsonSchemaCompatibilityException if a reference cannot be resolved
     *         and {@link UnresolvableRefStrategy#FAIL} is configured
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
        if (node instanceof Referenceable ref && ref.get$ref() != null) {
            var refValue = ref.get$ref();

            var resolved = refTraversal.resolveRef(refValue, node);
            if (resolved.isEmpty()) {
                handleUnresolvable(refValue, ctx);
                return;
            }

            var target = (JFullSchema) resolved.get();

            if (ctx.ancestry.contains(target)) {
                // Back-edge: replacing would create an object graph cycle.
                // Leave $ref as-is and record the cycle for the caller.
                ctx.cyclicRefs.put(refValue, target);
                return;
            }

            replaceInParent(node, target);

            // Recurse into the target if not already fully processed
            if (ctx.visited.add(target)) {
                ctx.ancestry.add(target);
                dereferenceChildren(target, ctx);
                ctx.ancestry.remove(target);
            }
            return;
        }

        if (!ctx.visited.add(node)) {
            return;
        }

        ctx.ancestry.add(node);
        dereferenceChildren(node, ctx);
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
        final Set<Node> ancestry = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<String, JFullSchema> cyclicRefs = new LinkedHashMap<>();
        final List<String> unresolvedRefs = new ArrayList<>();
        final Set<String> unresolvableRefs = Collections.newSetFromMap(new LinkedHashMap<>());
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
