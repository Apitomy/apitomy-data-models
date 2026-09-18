package io.apitomy.datamodels.util;

/**
 * Numeric comparisons for JSON Schema bounds.
 * <p>
 * These were previously spelled with {@code BigDecimal}, which has no equivalent
 * in the TypeScript target. Collected here so the substitute exists once rather
 * than in every converter, and so a future exact implementation has a single
 * place to land.
 * <p>
 * <b>Precision:</b> comparisons are made as doubles, so values beyond double
 * precision are not distinguished. For JSON Schema bounds this is theoretical,
 * and the TypeScript build could not do better regardless — JSON numbers are
 * doubles there.
 */
public final class NumberUtil {

    private NumberUtil() {
    }

    /**
     * Compares two numbers, returning a negative value, zero, or a positive value
     * as {@code a} is less than, equal to, or greater than {@code b}.
     *
     * @param a the first value
     * @param b the second value
     * @return the sign of {@code a - b}
     */
    public static int compare(Number a, Number b) {
        double da = a.doubleValue();
        double db = b.doubleValue();
        if (da < db) {
            return -1;
        }
        if (da > db) {
            return 1;
        }
        return 0;
    }
}
