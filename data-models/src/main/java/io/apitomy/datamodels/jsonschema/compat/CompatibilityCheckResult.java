package io.apitomy.datamodels.jsonschema.compat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import io.apitomy.datamodels.util.CollectionUtil;

/**
 * Result of a single-direction compatibility check (backward or forward).
 * <p>
 * Wraps the internal {@link DiffContext} into a clean, immutable public API.
 * Instances are created by {@link JsonSchemaCompatibilityChecker} and are not
 * directly constructible by callers.
 */
public final class CompatibilityCheckResult {

    private final Set<Difference> differences;
    private final List<String> unsupportedFeatures;

    /**
     * Package-private constructor — created by the checker.
     */
    CompatibilityCheckResult(DiffContext ctx) {
        this.differences = ctx.getDiffs();
        this.unsupportedFeatures = ctx.getUnsupportedFeatures();
    }

    /**
     * Returns {@code true} if all differences found are compatible in the
     * checked direction (i.e. there are no incompatible differences).
     *
     * @return {@code true} when there are no incompatible differences
     */
    public boolean isCompatible() {
        for (Difference d : differences) {
            if (!d.getDiffType().isBackwardsCompatible()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns all differences found between the original and updated schemas,
     * regardless of whether they are compatible or incompatible.
     *
     * @return an unmodifiable set of all differences
     */
    public Set<Difference> getDifferences() {
        return CollectionUtil.copyOfSet(differences);
    }

    /**
     * Returns only the incompatible differences — those that would break
     * consumers in the checked direction.
     *
     * @return an unmodifiable set of incompatible differences
     */
    public Set<Difference> getIncompatibleDifferences() {
        Set<Difference> incompatible = new LinkedHashSet<Difference>();
        for (Difference d : differences) {
            if (!d.getDiffType().isBackwardsCompatible()) {
                incompatible.add(d);
            }
        }
        return incompatible;
    }

    /**
     * Returns {@code true} if any schema features were flagged as unsupported
     * during the comparison (e.g. modern JSON Schema versions whose keywords
     * are not yet fully handled by the diff engine).
     *
     * @return {@code true} when unsupported features were encountered
     */
    public boolean hasUnsupportedFeatures() {
        return !unsupportedFeatures.isEmpty();
    }

    /**
     * Returns the list of unsupported-feature messages collected during
     * the comparison.
     *
     * @return an unmodifiable list of unsupported-feature descriptions
     */
    public List<String> getUnsupportedFeatures() {
        return CollectionUtil.copyOfList(unsupportedFeatures);
    }
}
