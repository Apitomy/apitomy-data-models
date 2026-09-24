package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.ref.JsonPointer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Difference {

    private final DiffType diffType;
    private final JsonPointer pathOriginal;
    private final JsonPointer pathUpdated;

    public Difference(DiffType diffType, JsonPointer pathOriginal, JsonPointer pathUpdated) {
        Objects.requireNonNull(diffType);
        this.diffType = diffType;
        Objects.requireNonNull(pathOriginal);
        this.pathOriginal = pathOriginal;
        Objects.requireNonNull(pathUpdated);
        this.pathUpdated = pathUpdated;
    }

    public DiffType getDiffType() {
        return diffType;
    }

    /**
     * A human-readable one-line description of this difference. Delegates to
     * {@link DiffType#getShortDescription()}.
     */
    public String getShortDescription() {
        return diffType.getShortDescription();
    }

    /**
     * A long-form, {@code --explain}-style explanation of this difference, if one is curated for
     * its {@link DiffType}. Delegates to {@link DiffType#getHelp()}.
     * <p>
     * This accessor lives on {@code Difference} (not only {@code DiffType}) so that future help can
     * be enriched with the concrete paths carried by this instance.
     */
    public Optional<String> getHelp() {
        return diffType.getHelp();
    }

    /**
     * Worked examples of this kind of difference. Delegates to {@link DiffType#getExamples()}.
     */
    public List<CompatibilityExample> getExamples() {
        return diffType.getExamples();
    }

    /**
     * Where the change is in the original schema: a JSON Pointer ending in the keyword that changed,
     * e.g. {@code /properties/name/maxLength}, or pointing at the subschema when the subschema as a
     * whole changed, or at the member of a list or map that was removed ({@code /required/1}).
     * Keywords are named as the original schema's draft names them. The value at the path is not
     * copied into the difference; resolve the path against the schema to get it.
     */
    public JsonPointer getPathOriginal() {
        return pathOriginal;
    }

    /** As {@link #getPathOriginal()}, in the updated schema. */
    public JsonPointer getPathUpdated() {
        return pathUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Difference)) return false;
        Difference d = (Difference) o;
        return diffType == d.diffType
                && pathOriginal.equals(d.pathOriginal)
                && pathUpdated.equals(d.pathUpdated);
    }

    @Override
    public int hashCode() {
        // Hand-written: Objects.hash does not transpile
        return (diffType.hashCode() * 31 + pathOriginal.hashCode()) * 31 + pathUpdated.hashCode();
    }

    @Override
    public String toString() {
        return "Difference{type=" + diffType + ", pathUpdated='" + pathUpdated + "'}";
    }
}
