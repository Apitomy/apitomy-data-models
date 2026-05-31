package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.Objects;

/**
 * Result of resolving a JSON Schema {@code $ref}.
 *
 * @param node     the resolved schema node
 * @param attached true if the node is part of the source document tree (internal reference),
 *                 false if it was parsed separately (external reference).
 *                 Detached nodes must be cloned or attached by the consumer when inlining
 *                 (e.g., during dereferencing).
 */
public record ResolvedRef(Node node, boolean attached) {

    public ResolvedRef {
        Objects.requireNonNull(node);
    }

    public static ResolvedRef attached(Node node) {
        return new ResolvedRef(node, true);
    }

    public static ResolvedRef detached(Node node) {
        return new ResolvedRef(node, false);
    }
}
