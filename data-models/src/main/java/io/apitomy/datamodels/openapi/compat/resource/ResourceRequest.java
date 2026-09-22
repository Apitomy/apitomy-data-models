package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.openapi.compat.ResourceSide;

/**
 * A request for a single missing resource: a side and an absolute retrieval
 * URI. Produced by {@link ResourceDiscovery#discover} for every reference the
 * current resource set cannot satisfy, and consumed by an
 * {@code AsyncResourceLoader} (a later task) to acquire it.
 * <p>
 * Value semantics: two requests for the same side and URI are equal, so a
 * discovery pass can be deduplicated with an ordinary {@link java.util.Set}
 * or used as a map key without extra bookkeeping.
 */
public final class ResourceRequest {

    private final ResourceSide side;
    private final String uri;

    public ResourceRequest(ResourceSide side, String uri) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        if (uri == null || uri.length() == 0) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        this.side = side;
        this.uri = uri;
    }

    /** Which side's resource set is missing this resource. */
    public ResourceSide getSide() {
        return side;
    }

    /** The absolute retrieval URI to acquire. */
    public String getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceRequest)) {
            return false;
        }
        ResourceRequest that = (ResourceRequest) other;
        return this.side == that.side && this.uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        int result = side.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResourceRequest{side=" + side + ", uri='" + uri + "'}";
    }
}
