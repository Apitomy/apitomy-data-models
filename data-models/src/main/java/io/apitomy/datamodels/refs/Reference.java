package io.apitomy.datamodels.refs;

import java.util.Objects;

/**
 * Encapsulates a reference string in OpenAPI/AsyncAPI schema,
 * for easier manipulation and parsing.
 */
public class Reference {


    private final String ref;

    private String abs;
    private String rel;

    /**
     * @param ref Encapsulated reference string. MUST NOT be null.
     */
    public Reference(String ref) {
        if (ref == null)
            throw new IllegalArgumentException();
        this.ref = ref;
        // TODO consider using java.net.URL
        String[] parts = ref.split("#");
        if (parts.length == 0 || parts.length == 1) {
            abs = ref;
            rel = null; // TODO is this even valid?
        } else if (parts.length == 2) {
            abs = "".equals(parts[0]) ? null : parts[0];
            rel = "#" + parts[1];
        } else {
            throw new IllegalArgumentException("Wrong reference format.");
        }
    }

    public Reference(String abs, String rel) {
        this( (abs == null ? "" : abs) + rel);
    }

    /**
     * @return the original/full reference string.
     */
    public String getRef() {
        return ref;
    }

    public String getAbsPart() {
        return abs;
    }

    public String getRelPart() {
        return rel;
    }

    public boolean isRelative() {
        return abs == null;
    }

    /**
     * @return a `name` of the referenced component, as parsed from the reference string.
     * @throws java.lang.RuntimeException if the reference does not contain relative part
     */
    public String getName() {
        Objects.requireNonNull(rel, "No relative part in the reference.");
        String[] parts = rel.split("/");
        return parts[parts.length - 1];
    }

    /**
     * @param that other reference
     * @return a new reference with the absolute part copied form the other one,
     * and relative from this one
     */
    public Reference withAbsoluteFrom(Reference that) {
        if (that.getAbsPart() == null)
            throw new IllegalArgumentException();
        return new Reference(that.abs + rel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference reference = (Reference) o;

        return ref.equals(reference.ref);
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }

    @Override
    public String toString() {
        return "Reference [" + ref + "]";
    }

}
