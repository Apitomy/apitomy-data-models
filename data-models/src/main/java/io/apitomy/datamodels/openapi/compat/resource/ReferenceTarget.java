package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.models.Node;

/**
 * A reference resolved to a concrete node, together with the document that
 * owns it. A {@link ReferenceGraph} maps each resolved {@link ReferenceEdge}
 * to exactly one target.
 */
public final class ReferenceTarget {

    private final ResourceDocument owner;
    private final Node node;
    private final String pointer;

    public ReferenceTarget(ResourceDocument owner, Node node, String pointer) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null");
        }
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        this.owner = owner;
        this.node = node;
        this.pointer = pointer;
    }

    /** The document that owns the resolved node. */
    public ResourceDocument getOwner() {
        return owner;
    }

    /** The resolved node itself. */
    public Node getNode() {
        return node;
    }

    /**
     * The fragment (JSON Pointer or anchor name) that was used to reach the node
     * within {@link #getOwner()}, or {@code null} if the reference resolved to
     * the document root as a whole.
     */
    public String getPointer() {
        return pointer;
    }
}
