package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.openapi.compat.ResourceSide;
import io.apitomy.datamodels.util.CollectionUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The documents acquired so far for one side of a check, keyed by retrieval
 * URI. A {@link ResourceSet} only ever holds documents for a single
 * {@link ResourceSide}; the original and updated sides are always separate
 * sets, even when a URI is shared between them.
 */
public final class ResourceSet {

    private final ResourceSide side;
    private final Map<String, ResourceDocument> documentsByUri = new LinkedHashMap<String, ResourceDocument>();

    public ResourceSet(ResourceSide side) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        this.side = side;
    }

    /** Which side this set holds documents for. */
    public ResourceSide getSide() {
        return side;
    }

    /**
     * Adds or replaces the document at its own {@link ResourceDocument#getUri()}.
     *
     * @throws IllegalArgumentException if {@code document}'s side does not match this set's side
     */
    public void add(ResourceDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        if (document.getSide() != side) {
            throw new IllegalArgumentException("document side " + document.getSide() + " does not match resource set side " + side);
        }
        documentsByUri.put(document.getUri(), document);
    }

    /** The document acquired for {@code uri}, or {@code null} if not present. */
    public ResourceDocument get(String uri) {
        return documentsByUri.get(uri);
    }

    /** True if a document has been acquired for {@code uri}. */
    public boolean contains(String uri) {
        return documentsByUri.containsKey(uri);
    }

    /** Every document currently in this set, in the order they were added. */
    public List<ResourceDocument> getDocuments() {
        return CollectionUtil.copyOfList(new ArrayList<ResourceDocument>(documentsByUri.values()));
    }
}
