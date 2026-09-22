package io.apitomy.datamodels.openapi.compat;

/**
 * A single location within one side of a compatibility check: which document
 * (identified by its check-scoped URI) and which position within it (as an
 * escaped JSON Pointer).
 * <p>
 * Locations always refer to the caller-supplied document, never to any
 * internal normalized or resolved copy -- resolving a {@code $ref} must not
 * make a finding point at a synthetic location the caller cannot find in
 * their own source.
 */
public final class SourceLocation {

    private final ResourceSide side;
    private final String uri;
    private final String pointer;

    /**
     * @param side the side (original or updated) this location belongs to
     * @param uri the check-scoped URI of the document containing this location
     * @param pointer an escaped JSON Pointer (RFC 6901) into that document
     */
    public SourceLocation(ResourceSide side, String uri, String pointer) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        if (pointer == null) {
            throw new IllegalArgumentException("pointer must not be null");
        }
        this.side = side;
        this.uri = uri;
        this.pointer = pointer;
    }

    /** The side (original or updated) this location belongs to. */
    public ResourceSide getSide() {
        return side;
    }

    /** The check-scoped URI of the document containing this location. */
    public String getUri() {
        return uri;
    }

    /** An escaped JSON Pointer (RFC 6901) into that document. */
    public String getPointer() {
        return pointer;
    }

    /**
     * Total order over locations, used to keep {@link CompatibilityResult#getFindings()}
     * deterministically sorted: by URI, then by pointer.
     */
    int compareForSorting(SourceLocation other) {
        int byUri = this.uri.compareTo(other.uri);
        if (byUri != 0) {
            return byUri;
        }
        return this.pointer.compareTo(other.pointer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SourceLocation)) {
            return false;
        }
        SourceLocation other = (SourceLocation) o;
        return this.side == other.side
                && this.uri.equals(other.uri)
                && this.pointer.equals(other.pointer);
    }

    @Override
    public int hashCode() {
        int result = side.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + pointer.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return side + " " + uri + "#" + pointer;
    }
}
