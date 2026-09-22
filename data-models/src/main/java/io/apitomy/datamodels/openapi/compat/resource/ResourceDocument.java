package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.openapi.compat.ResourceSide;

/**
 * A single acquired document, identified by its retrieval URI, on one side of
 * a compatibility check.
 * <p>
 * The retrieval URI is whatever the caller supplied or resolved a reference
 * to; it is not normalized or re-derived from document content. The same URI
 * string can be present in both the {@link ResourceSide#ORIGINAL} and
 * {@link ResourceSide#UPDATED} resource sets, each holding an independent
 * {@link #getRoot()} — resolution must never substitute one side's document
 * for the other.
 */
public final class ResourceDocument {

    private final ResourceSide side;
    private final String uri;
    private final RootCapable root;

    public ResourceDocument(ResourceSide side, String uri, RootCapable root) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        if (uri == null || uri.length() == 0) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        if (root == null) {
            throw new IllegalArgumentException("root must not be null");
        }
        this.side = side;
        this.uri = uri;
        this.root = root;
    }

    /** Which side of the check this document belongs to. */
    public ResourceSide getSide() {
        return side;
    }

    /** The retrieval URI this document was acquired from, exactly as supplied or resolved. */
    public String getUri() {
        return uri;
    }

    /** The root node of the acquired document. */
    public RootCapable getRoot() {
        return root;
    }

    /** The document's model type (OAS family/version, AsyncAPI, OpenRPC, or a JSON Schema dialect). */
    public ModelType getModelType() {
        return root.modelType();
    }
}
