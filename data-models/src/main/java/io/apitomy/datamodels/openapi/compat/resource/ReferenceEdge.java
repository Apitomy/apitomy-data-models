package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.openapi.compat.ResourceSide;

/**
 * A single typed reference edge discovered while indexing a document: a node
 * that carries a {@code $ref} (or equivalent), the raw reference string, and
 * the kind of reference it is. An edge is data about where a reference
 * appears; whether it resolves is recorded separately on the
 * {@link ReferenceGraph} that produced it.
 */
public final class ReferenceEdge {

    private final ResourceSide side;
    private final String ownerUri;
    private final Node source;
    private final String rawReference;
    private final ReferenceKind kind;

    public ReferenceEdge(ResourceSide side, String ownerUri, Node source, String rawReference, ReferenceKind kind) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        if (ownerUri == null || ownerUri.length() == 0) {
            throw new IllegalArgumentException("ownerUri must not be null or empty");
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (rawReference == null || rawReference.length() == 0) {
            throw new IllegalArgumentException("rawReference must not be null or empty");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        this.side = side;
        this.ownerUri = ownerUri;
        this.source = source;
        this.rawReference = rawReference;
        this.kind = kind;
    }

    /** Which side this edge belongs to. */
    public ResourceSide getSide() {
        return side;
    }

    /** The retrieval URI of the document that contains {@link #getSource()}. */
    public String getOwnerUri() {
        return ownerUri;
    }

    /** The node that carries the reference (e.g. the Schema Object with the {@code $ref}). */
    public Node getSource() {
        return source;
    }

    /** The raw, unparsed reference string. */
    public String getRawReference() {
        return rawReference;
    }

    /** The kind of reference this is. */
    public ReferenceKind getKind() {
        return kind;
    }
}
