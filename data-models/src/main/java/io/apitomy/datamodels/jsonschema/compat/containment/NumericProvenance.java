package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * Whether a numeric value's original decimal precision is known exactly, and
 * if so, the exact value and its source token.
 * <p>
 * A JSON number that was parsed as a JavaScript-style double may already have
 * lost precision by the time it reaches this checker (an unsafe integer, or a
 * fractional value beyond double precision). Provenance distinguishes "this
 * number's exact original value is known" from "we can no longer be sure" so
 * that a rule can report {@code NUMERIC_PRECISION_UNCERTAIN} instead of
 * silently comparing a rounded value as if it were exact.
 */
public final class NumericProvenance {

    /** The largest integer a double (and therefore a JS number) represents exactly: 2^53 - 1. */
    public static final long MAX_SAFE_INTEGER = 9007199254740991L;

    private final ExactDecimal exact;
    private final String sourceText;

    private NumericProvenance(ExactDecimal exact, String sourceText) {
        this.exact = exact;
        this.sourceText = sourceText;
    }

    /**
     * Exact provenance from the number's own literal source text (for example,
     * text captured by a lexical numeric-token scan of the original JSON, before
     * any parser could round it).
     */
    public static NumericProvenance fromToken(String token) {
        return new NumericProvenance(ExactDecimal.parse(token), token);
    }

    /** No known exact provenance; the original decimal value may have already been lost. */
    public static NumericProvenance uncertain() {
        return new NumericProvenance(null, null);
    }

    /**
     * Conservative provenance for a {@link Number} already produced by the
     * ordinary (double-based) model reader: exact for a finite integer within
     * {@link #MAX_SAFE_INTEGER}, uncertain for anything fractional or outside
     * that range, since a double cannot distinguish those from a neighboring
     * value.
     */
    public static NumericProvenance fromParsedNumber(Number value) {
        if (value == null) {
            return uncertain();
        }
        double doubleValue = value.doubleValue();
        if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
            return uncertain();
        }
        if (doubleValue != Math.floor(doubleValue)) {
            return uncertain();
        }
        if (doubleValue > (double) MAX_SAFE_INTEGER || doubleValue < -(double) MAX_SAFE_INTEGER) {
            return uncertain();
        }
        long longValue = (long) doubleValue;
        return fromToken(Long.toString(longValue));
    }

    /** True if this value's exact original precision is known. */
    public boolean isExact() {
        return exact != null;
    }

    /**
     * The exact value.
     *
     * @throws IllegalStateException if {@link #isExact()} is false
     */
    public ExactDecimal getExact() {
        if (exact == null) {
            throw new IllegalStateException("No exact provenance is available");
        }
        return exact;
    }

    /**
     * The literal source text this provenance was derived from.
     *
     * @throws IllegalStateException if {@link #isExact()} is false
     */
    public String getSourceText() {
        if (sourceText == null) {
            throw new IllegalStateException("No exact provenance is available");
        }
        return sourceText;
    }
}
