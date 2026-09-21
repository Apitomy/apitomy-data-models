package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;
import io.apitomy.datamodels.models.visitors.TraversalContext;
import io.apitomy.datamodels.models.visitors.TraversingVisitor;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import io.apitomy.datamodels.util.CollectionUtil;
import java.util.Optional;
import io.apitomy.datamodels.models.ParentPropertyType;

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
        DereferenceContext ctx = new DereferenceContext();
        dereferenceNode(schema, ctx);
        return new DereferenceResult(
                schema,
                CollectionUtil.copyOfList(ctx.unresolvedRefs),
                CollectionUtil.copyOfMap(ctx.cyclicRefs));
    }

    private void dereferenceNode(JFullSchema node, DereferenceContext ctx) {
        if (ctx.depth > maxDepth) {
            throw new DereferenceException(
                    "Maximum recursion depth (" + maxDepth + ") exceeded during dereferencing. "
                    + "This may indicate a cycle that was not detected due to "
                    + "identity-distinct resolver results for the same logical document.");
        }

        // Computed up front rather than as an instanceof pattern in the condition,
        // which the transpiler cannot express. A Referenceable carrying no $ref must
        // still fall through to the child traversal below.
        boolean hasRef = node instanceof Referenceable && ((Referenceable) node).get$ref() != null;
        if (hasRef) {
            // Follow the ref chain to the final non-$ref target
            JFullSchema target = resolveRefChain(node, ctx);
            if (target == null) {
                return;
            }

            if (ctx.ancestry.contains(target)) {
                ctx.cyclicRefs.put(((Referenceable) node).get$ref(), target);
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

    private JFullSchema resolveRefChain(JFullSchema node, DereferenceContext ctx) {
        JFullSchema current = node;
        HashSet<String> chainRefs = new HashSet<String>();
        // The loop condition is unrolled into the body because the transpiler cannot
        // express an instanceof pattern variable used within the same condition.
        while (true) {
            if (!(current instanceof Referenceable)) {
                break;
            }
            String refValue = ((Referenceable) current).get$ref();
            if (refValue == null) {
                break;
            }
            if (!chainRefs.add(refValue)) {
                ctx.cyclicRefs.put(refValue, current);
                return null;
            }
            try {
                Optional<Node> resolved = refTraversal.resolveRef(refValue, current);
                if (resolved.isEmpty()) {
                    handleUnresolvable(refValue, ctx);
                    return null;
                }
                current = (JFullSchema) resolved.get();
            } catch (ReferenceResolutionException e) {
                throw e;
            } catch (Exception e) {
                throw new ReferenceResolutionException(
                        "Failed to resolve $ref: " + refValue, refValue, e);
            }
        }
        return current;
    }

    private void dereferenceChildren(JFullSchema node, DereferenceContext ctx) {
        JsonSchemaRefDereferencer.ChildSchemaVisitor visitor = new ChildSchemaVisitor(node, ctx);
        VisitorUtil.visitTree(node, visitor, TraverserDirection.down);
    }

    private class ChildSchemaVisitor extends AllNodeVisitor implements TraversingVisitor {
        private final JFullSchema root;
        private final DereferenceContext ctx;
        private boolean isRoot = true;
        private TraversalContext traversalContext;

        ChildSchemaVisitor(JFullSchema root, DereferenceContext ctx) {
            this.root = root;
            this.ctx = ctx;
        }

        @Override
        public void setTraversalContext(TraversalContext context) {
            this.traversalContext = context;
        }

        @Override
        protected void visitNode(Node n) {
        }

        @Override
        public void visitFullSchema(JFullSchema n) {
            if (isRoot) {
                isRoot = false;
                return;
            }
            traversalContext.skip();
        }

        @Override
        public void afterVisitFullSchema(JFullSchema n) {
            if (n != root) {
                dereferenceNode(n, ctx);
            }
        }
    }

    private void handleUnresolvable(String refValue, DereferenceContext ctx) {
        switch (strategy) {
            case FAIL:
                throw new ReferenceResolutionException("Unresolvable $ref: " + refValue, refValue);
            case COLLECT:
                ctx.unresolvedRefs.add("Unresolvable $ref: " + refValue);
                break;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void replaceInParent(Node refNode, Node target) {
        Node parent = refNode.parent();
        if (parent == null) {
            if (refNode instanceof Referenceable) {
                ((Referenceable) refNode).set$ref(null);
            }
            return;
        }

        String propName = refNode.parentPropertyName();
        ParentPropertyType propType = refNode.parentPropertyType();

        switch (propType) {
            case standard:
                NodeUtil.setProperty(parent, propName, target);
                break;
            case array:
                List list = (List) NodeUtil.getProperty(parent, propName);
                if (list != null) {
                    int index = list.indexOf(refNode);
                    if (index >= 0) {
                        list.set(index, target);
                    }
                }
                break;
            case map:
                Map map = (Map) NodeUtil.getProperty(parent, propName);
                if (map != null) {
                    String key = refNode.mapPropertyName();
                    if (key != null) {
                        map.put(key, target);
                    }
                }
                break;
        }
    }

    private static class DereferenceContext {
        // Model nodes do not override equals/hashCode, so a plain HashSet already
        // compares by identity -- which is what this traversal needs.
        final Set<Node> ancestry = new HashSet<>();
        final Set<Node> visited = new HashSet<>();
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
            JsonSchemaRefTraversal refTraversal = new JsonSchemaRefTraversal(refResolver);
            return new JsonSchemaRefDereferencer(refTraversal, strategy, maxDepth);
        }
    }
}
