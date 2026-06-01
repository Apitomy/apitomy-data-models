package io.apitomy.datamodels.jsonschema.compat;

import java.util.Objects;

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
