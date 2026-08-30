package io.apitomy.datamodels.jsonschema.compat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Difference {

    private final DiffType diffType;
    private final String pathOriginal;
    private final String pathUpdated;
    private final String subSchemaOriginal;
    private final String subSchemaUpdated;

    public Difference(DiffType diffType, String pathOriginal, String pathUpdated,
                      String subSchemaOriginal, String subSchemaUpdated) {
        this.diffType = Objects.requireNonNull(diffType);
        this.pathOriginal = Objects.requireNonNull(pathOriginal);
        this.pathUpdated = Objects.requireNonNull(pathUpdated);
        this.subSchemaOriginal = Objects.requireNonNull(subSchemaOriginal);
        this.subSchemaUpdated = Objects.requireNonNull(subSchemaUpdated);
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
     * be enriched with the concrete paths and sub-schemas carried by this instance.
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

    public String getPathOriginal() {
        return pathOriginal;
    }

    public String getPathUpdated() {
        return pathUpdated;
    }

    public String getSubSchemaOriginal() {
        return subSchemaOriginal;
    }

    public String getSubSchemaUpdated() {
        return subSchemaUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Difference d)) return false;
        return diffType == d.diffType
                && pathOriginal.equals(d.pathOriginal)
                && pathUpdated.equals(d.pathUpdated)
                && subSchemaOriginal.equals(d.subSchemaOriginal)
                && subSchemaUpdated.equals(d.subSchemaUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffType, pathOriginal, pathUpdated, subSchemaOriginal, subSchemaUpdated);
    }

    @Override
    public String toString() {
        return "Difference{type=%s, pathUpdated='%s'}".formatted(diffType, pathUpdated);
    }
}
