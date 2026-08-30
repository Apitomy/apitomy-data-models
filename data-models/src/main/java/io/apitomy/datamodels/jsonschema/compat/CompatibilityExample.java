package io.apitomy.datamodels.jsonschema.compat;

import java.util.List;
import java.util.Objects;

/**
 * A worked example of a compatibility {@link DiffType}, drawn from the bundled example catalog.
 * <p>
 * Each example is one <em>direction</em> of one catalog case: a concrete {@code original} →
 * {@code updated} schema pair, the compatibility verdict for that direction, and every
 * {@link DiffType} the checker emits for it. The same case can yield two examples (one per
 * direction), and a single example demonstrates all of {@link #getDiffTypes()} at once.
 *
 * @see DiffType#getExamples()
 */
public final class CompatibilityExample {

    private final String id;
    private final Direction direction;
    private final boolean compatible;
    private final String originalSchema;
    private final String updatedSchema;
    private final List<DiffType> diffTypes;

    CompatibilityExample(String id, Direction direction, boolean compatible,
                         String originalSchema, String updatedSchema, List<DiffType> diffTypes) {
        this.id = Objects.requireNonNull(id);
        this.direction = Objects.requireNonNull(direction);
        this.compatible = compatible;
        this.originalSchema = Objects.requireNonNull(originalSchema);
        this.updatedSchema = Objects.requireNonNull(updatedSchema);
        this.diffTypes = List.copyOf(diffTypes);
    }

    /** The catalog case id this example was drawn from. */
    public String getId() {
        return id;
    }

    /** Which check direction this example demonstrates. */
    public Direction getDirection() {
        return direction;
    }

    /** Whether the change is compatible in this direction. */
    public boolean isCompatible() {
        return compatible;
    }

    /** The source ("original") schema, as a JSON string. */
    public String getOriginalSchema() {
        return originalSchema;
    }

    /** The target ("updated") schema, as a JSON string. */
    public String getUpdatedSchema() {
        return updatedSchema;
    }

    /** Every diff type the checker emits for this case in this direction (never empty). */
    public List<DiffType> getDiffTypes() {
        return diffTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompatibilityExample e)) return false;
        return compatible == e.compatible
                && id.equals(e.id)
                && direction == e.direction
                && originalSchema.equals(e.originalSchema)
                && updatedSchema.equals(e.updatedSchema)
                && diffTypes.equals(e.diffTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, direction, compatible, originalSchema, updatedSchema, diffTypes);
    }

    @Override
    public String toString() {
        return "CompatibilityExample{id='%s', direction=%s, compatible=%s, diffTypes=%s}"
                .formatted(id, direction, compatible, diffTypes);
    }
}
