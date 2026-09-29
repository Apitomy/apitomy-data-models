package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * What this checker knows about JSON Schema {@code format} values: known
 * numeric ranges (using {@link ExactDecimal}, never a native number), and
 * which pairs of formats are known to be compatible, incompatible, purely
 * informational, or unresolved.
 * <p>
 * {@code format} is an annotation everywhere except where a dialect or
 * profile promotes specific values to assertions; this registry does not
 * assume every registered format name is itself a validation assertion, and
 * does not assume {@code float}/{@code double} share a representation.
 */
public final class FormatRegistry {

    /** The relationship between a source and a target format (or their explicit bounds). */
    public enum FormatRelation {
        /** Every value valid under the source is guaranteed valid under the target. */
        COMPATIBLE,
        /** A value valid under the source is not guaranteed valid under the target. */
        INCOMPATIBLE,
        /** The formats differ, but neither is known to constrain validation (a hint change only). */
        INFORMATIONAL,
        /** Not enough is known about one or both formats to relate them. */
        UNKNOWN
    }

    private FormatRegistry() {
    }

    /** The inclusive [minimum, maximum] range implied by a known integer format, or {@code null} if {@code format} is not one. */
    public static ExactDecimal[] integerRange(String format) {
        if ("int32".equals(format)) {
            return new ExactDecimal[] { ExactDecimal.parse("-2147483648"), ExactDecimal.parse("2147483647") };
        }
        if ("int64".equals(format)) {
            return new ExactDecimal[] { ExactDecimal.parse("-9223372036854775808"), ExactDecimal.parse("9223372036854775807") };
        }
        return null;
    }

    /**
     * Relates a source and target numeric format together with each side's own
     * explicit {@code minimum}/{@code maximum} (which may narrow a format's
     * implied range further; the tighter of the two always wins). Both sides
     * must be a known integer format ({@code int32}/{@code int64}) or this
     * returns {@link FormatRelation#UNKNOWN} unless the format names are
     * identical (in which case explicit bounds alone decide it) --
     * {@code float}/{@code double} are not given numeric-range treatment here,
     * since IEEE-754 precision, not a simple bound, distinguishes them.
     */
    public static FormatRelation relateNumericRange(String sourceFormat, SchemaNormalizer.Bound sourceMinimum,
            SchemaNormalizer.Bound sourceMaximum, String targetFormat, SchemaNormalizer.Bound targetMinimum,
            SchemaNormalizer.Bound targetMaximum) {
        ExactDecimal[] sourceRange = integerRange(sourceFormat);
        ExactDecimal[] targetRange = integerRange(targetFormat);
        boolean sameFormat = sourceFormat == null ? targetFormat == null : sourceFormat.equals(targetFormat);
        if ((sourceRange == null || targetRange == null) && !sameFormat) {
            return FormatRelation.UNKNOWN;
        }

        ExactDecimal effectiveSourceMin = tighterLower(sourceRange != null ? sourceRange[0] : null, sourceMinimum);
        ExactDecimal effectiveSourceMax = tighterUpper(sourceRange != null ? sourceRange[1] : null, sourceMaximum);
        ExactDecimal effectiveTargetMin = tighterLower(targetRange != null ? targetRange[0] : null, targetMinimum);
        ExactDecimal effectiveTargetMax = tighterUpper(targetRange != null ? targetRange[1] : null, targetMaximum);

        if (effectiveSourceMin == null || effectiveSourceMax == null || effectiveTargetMin == null || effectiveTargetMax == null) {
            return FormatRelation.UNKNOWN;
        }
        boolean contained = effectiveSourceMin.compareTo(effectiveTargetMin) >= 0 && effectiveSourceMax.compareTo(effectiveTargetMax) <= 0;
        return contained ? FormatRelation.COMPATIBLE : FormatRelation.INCOMPATIBLE;
    }

    private static ExactDecimal tighterLower(ExactDecimal formatBound, SchemaNormalizer.Bound explicitBound) {
        ExactDecimal explicit = explicitBound != null ? explicitBound.getValue() : null;
        if (formatBound == null) {
            return explicit;
        }
        if (explicit == null) {
            return formatBound;
        }
        return explicit.compareTo(formatBound) > 0 ? explicit : formatBound;
    }

    private static ExactDecimal tighterUpper(ExactDecimal formatBound, SchemaNormalizer.Bound explicitBound) {
        ExactDecimal explicit = explicitBound != null ? explicitBound.getValue() : null;
        if (formatBound == null) {
            return explicit;
        }
        if (explicit == null) {
            return formatBound;
        }
        return explicit.compareTo(formatBound) < 0 ? explicit : formatBound;
    }

    /**
     * Relates a source and target format that are not given numeric-range
     * treatment (a non-numeric format such as {@code date}/{@code date-time}/
     * {@code uuid}/{@code byte}/{@code binary}/{@code password}, or a pair this
     * registry does not otherwise recognize). Identical formats (including
     * both absent) are compatible. {@code password} is purely a UI hint with
     * no validation effect in any dialect this checker supports, so adding,
     * removing, or changing it alongside an otherwise-identical format is
     * informational, never incompatible or unknown. Anything else differing
     * is Unknown, not assumed either way.
     */
    public static FormatRelation relate(String sourceFormat, String targetFormat) {
        if (sourceFormat == null ? targetFormat == null : sourceFormat.equals(targetFormat)) {
            return FormatRelation.COMPATIBLE;
        }
        if ("password".equals(sourceFormat) || "password".equals(targetFormat)) {
            return FormatRelation.INFORMATIONAL;
        }
        return FormatRelation.UNKNOWN;
    }
}
