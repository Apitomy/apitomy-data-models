package io.apitomy.datamodels.jsonschema.ref;

import java.util.Objects;

/**
 * A parsed JSON Schema {@code $ref} value.
 * <p>
 * A {@code $ref} is a URI-reference (RFC 3986) with an optional fragment.
 * The fragment can be either a JSON Pointer (RFC 6901, starts with '/') or
 * an anchor name (plain string, used with {@code $anchor} or {@code $id} fragments).
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code #/definitions/Address} — internal JSON Pointer reference</li>
 *   <li>{@code #Address} — internal anchor reference</li>
 *   <li>{@code other.json#/defs/Bar} — external with JSON Pointer fragment</li>
 *   <li>{@code https://example.com/schema.json} — external, no fragment</li>
 *   <li>{@code #} — root reference</li>
 * </ul>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc3986">RFC 3986 — Uniform Resource Identifier (URI)</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6901">RFC 6901 — JSON Pointer</a>
 */
public final class JsonRef {

    private final String raw;
    private final String resource;
    private final String fragment;
    private final JsonPointer pointer;
    private final String anchor;

    private JsonRef(String raw, String resource, String fragment, JsonPointer pointer, String anchor) {
        this.raw = raw;
        this.resource = resource;
        this.fragment = fragment;
        this.pointer = pointer;
        this.anchor = anchor;
    }

    /**
     * Parse a {@code $ref} string into its components.
     */
    public static JsonRef parse(String ref) {
        Objects.requireNonNull(ref, "$ref value must not be null");

        var hashIndex = ref.indexOf('#');
        String resource;
        String fragment;

        if (hashIndex < 0) {
            resource = ref;
            fragment = null;
        } else {
            resource = hashIndex > 0 ? ref.substring(0, hashIndex) : null;
            fragment = ref.substring(hashIndex + 1);
        }

        JsonPointer pointer = null;
        String anchor = null;

        if (fragment != null) {
            if (fragment.isEmpty()) {
                // "#" alone — root reference, both pointer and anchor are null
            } else if (fragment.startsWith("/")) {
                // "#/..." — JSON Pointer (RFC 6901)
                pointer = JsonPointer.parse(fragment);
            } else {
                // "#name" — anchor reference
                anchor = fragment;
            }
        }

        return new JsonRef(ref, resource, fragment, pointer, anchor);
    }

    /** The original {@code $ref} string. */
    public String raw() {
        return raw;
    }

    /**
     * The resource part (before {@code #}), or null for same-document references.
     * Can be a relative path, absolute URL, or any URI-reference.
     */
    public String resource() {
        return resource;
    }

    /** The raw fragment string (after {@code #}), or null if no fragment. */
    public String fragment() {
        return fragment;
    }

    /**
     * The parsed JSON Pointer, or null if the fragment is not a pointer
     * (i.e., it's an anchor or there is no fragment).
     */
    public JsonPointer pointer() {
        return pointer;
    }

    /**
     * The anchor name, or null if the fragment is not an anchor
     * (i.e., it's a JSON Pointer or there is no fragment).
     */
    public String anchor() {
        return anchor;
    }

    /** True if this is a same-document reference (no resource part). */
    public boolean isInternal() {
        return resource == null;
    }

    /** True if this is an external reference (has a resource part). */
    public boolean isExternal() {
        return resource != null;
    }

    /** True if the fragment is a JSON Pointer. */
    public boolean isPointer() {
        return pointer != null;
    }

    /** True if the fragment is an anchor name. */
    public boolean isAnchor() {
        return anchor != null;
    }

    /** True if this is a root reference ({@code #} with no path or anchor). */
    public boolean isRoot() {
        return fragment != null && fragment.isEmpty() && resource == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRef r)) return false;
        return raw.equals(r.raw);
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    @Override
    public String toString() {
        return raw;
    }
}
