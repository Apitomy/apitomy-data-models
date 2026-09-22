package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.ResourceSide;

/**
 * A localized resolution problem: a reference could not be followed to a
 * concrete node, either because the resource it points into was never
 * acquired ({@link FindingCode#RESOURCE_UNRESOLVED}) or because the resource
 * was acquired but the fragment inside it did not resolve
 * ({@link FindingCode#RESOURCE_INVALID}).
 * <p>
 * A problem is attached to the reference's owning side and to the URI it
 * could not resolve (the external resource URI for a cross-document
 * reference, or the owning document's own URI for an internal reference that
 * failed to resolve). This is diagnostic provenance, not a
 * {@code CompatibilityFinding} itself; later tasks translate a problem that
 * affects an analyzed interaction into a finding.
 */
public final class ResourceProblem {

    private final ResourceSide side;
    private final String uri;
    private final FindingCode code;
    private final String message;

    public ResourceProblem(ResourceSide side, String uri, FindingCode code, String message) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        if (uri == null || uri.length() == 0) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        if (code == null) {
            throw new IllegalArgumentException("code must not be null");
        }
        if (message == null || message.length() == 0) {
            throw new IllegalArgumentException("message must not be null or empty");
        }
        this.side = side;
        this.uri = uri;
        this.code = code;
        this.message = message;
    }

    /** Which side's resolution this problem occurred on. */
    public ResourceSide getSide() {
        return side;
    }

    /** The resource URI the problem is attached to. */
    public String getUri() {
        return uri;
    }

    /** The stable code classifying this problem. */
    public FindingCode getCode() {
        return code;
    }

    /** A human-readable description of the problem. */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ResourceProblem{side=" + side + ", uri='" + uri + "', code=" + code + ", message='" + message + "'}";
    }
}
