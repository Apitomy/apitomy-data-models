package io.apitomy.datamodels.openapi.compat;

import io.apitomy.datamodels.openapi.compat.resource.ResourceSet;

/**
 * Immutable, per-invocation configuration for a compatibility check.
 * <p>
 * {@link #defaults()} supplies empty resource sets, no resource acquisition,
 * and distinct virtual root URIs for the two sides. A "virtual" URI is a
 * synthetic identifier used only to key the supplied document within its own
 * resource set; it is never treated as, or compared against, an actual API
 * deployment address. Callers analyzing documents that were retrieved from
 * real locations should supply those retrieval URIs instead, so that any
 * relative external {@code $ref} in the document resolves against its real
 * base rather than the synthetic default.
 */
public final class CheckOptions {

    /** The default virtual retrieval URI for the original document, when none is supplied. */
    public static final String DEFAULT_ORIGINAL_URI = "urn:openapi-compat:original";

    /** The default virtual retrieval URI for the updated document, when none is supplied. */
    public static final String DEFAULT_UPDATED_URI = "urn:openapi-compat:updated";

    private final String originalUri;
    private final String updatedUri;
    private final ResourceSet originalResources;
    private final ResourceSet updatedResources;
    private final CheckCancellation cancellation;

    private CheckOptions(String originalUri, String updatedUri, ResourceSet originalResources,
            ResourceSet updatedResources, CheckCancellation cancellation) {
        if (originalUri == null || originalUri.length() == 0) {
            throw new IllegalArgumentException("originalUri must not be null or empty");
        }
        if (updatedUri == null || updatedUri.length() == 0) {
            throw new IllegalArgumentException("updatedUri must not be null or empty");
        }
        if (originalResources == null) {
            throw new IllegalArgumentException("originalResources must not be null");
        }
        if (updatedResources == null) {
            throw new IllegalArgumentException("updatedResources must not be null");
        }
        if (originalResources.getSide() != ResourceSide.ORIGINAL) {
            throw new IllegalArgumentException("originalResources must be a ResourceSet for ResourceSide.ORIGINAL");
        }
        if (updatedResources.getSide() != ResourceSide.UPDATED) {
            throw new IllegalArgumentException("updatedResources must be a ResourceSet for ResourceSide.UPDATED");
        }
        this.originalUri = originalUri;
        this.updatedUri = updatedUri;
        this.originalResources = originalResources;
        this.updatedResources = updatedResources;
        this.cancellation = cancellation;
    }

    /** Default options: empty resource sets, no cancellation, distinct virtual root URIs. */
    public static CheckOptions defaults() {
        return new CheckOptions(DEFAULT_ORIGINAL_URI, DEFAULT_UPDATED_URI,
                new ResourceSet(ResourceSide.ORIGINAL), new ResourceSet(ResourceSide.UPDATED), null);
    }

    /** A copy of these options with the original document's retrieval URI replaced. */
    public CheckOptions withOriginalUri(String uri) {
        return new CheckOptions(uri, updatedUri, originalResources, updatedResources, cancellation);
    }

    /** A copy of these options with the updated document's retrieval URI replaced. */
    public CheckOptions withUpdatedUri(String uri) {
        return new CheckOptions(originalUri, uri, originalResources, updatedResources, cancellation);
    }

    /** A copy of these options with the original side's pre-acquired external resources replaced. */
    public CheckOptions withOriginalResources(ResourceSet resources) {
        return new CheckOptions(originalUri, updatedUri, resources, updatedResources, cancellation);
    }

    /** A copy of these options with the updated side's pre-acquired external resources replaced. */
    public CheckOptions withUpdatedResources(ResourceSet resources) {
        return new CheckOptions(originalUri, updatedUri, originalResources, resources, cancellation);
    }

    /** A copy of these options with the given cooperative cancellation signal. */
    public CheckOptions withCancellation(CheckCancellation cancellation) {
        return new CheckOptions(originalUri, updatedUri, originalResources, updatedResources, cancellation);
    }

    /** The original document's retrieval URI. */
    public String getOriginalUri() {
        return originalUri;
    }

    /** The updated document's retrieval URI. */
    public String getUpdatedUri() {
        return updatedUri;
    }

    /** External resources already acquired for the original side. */
    public ResourceSet getOriginalResources() {
        return originalResources;
    }

    /** External resources already acquired for the updated side. */
    public ResourceSet getUpdatedResources() {
        return updatedResources;
    }

    /** The cancellation signal for this check, or {@code null} if none was supplied. */
    public CheckCancellation getCancellation() {
        return cancellation;
    }
}
