package io.apitomy.datamodels.jsonschema.compat.containment;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Normalizes dialect-specific keyword spellings into a single, dialect-neutral
 * shape that later proof rules (T7+) compare, without discarding provenance:
 * legacy boolean-paired exclusive bounds ({@code exclusiveMinimum: true} beside
 * {@code minimum: N}) become a value/exclusivity pair; a modern dialect's
 * independent {@code minimum}/{@code exclusiveMinimum} numeric assertions
 * collapse to whichever is tighter, compared exactly (per T5's
 * {@link ExactDecimal}, never as doubles); OAS 3.0's {@code nullable} widens
 * the effective type set only when a sibling {@code type} is actually present.
 */
public final class SchemaNormalizer {

    private SchemaNormalizer() {
    }

    /** True for dialects where {@code exclusiveMinimum}/{@code exclusiveMaximum} are booleans paired with {@code minimum}/{@code maximum}. */
    public static boolean usesLegacyExclusiveBoundStyle(SchemaDialect dialect) {
        return dialect == SchemaDialect.OAS20 || dialect == SchemaDialect.OAS30 || dialect == SchemaDialect.DRAFT4;
    }

    /** A normalized bound: an exact value and whether it excludes that value itself. */
    public static final class Bound {
        private final ExactDecimal value;
        private final boolean exclusive;

        Bound(ExactDecimal value, boolean exclusive) {
            this.value = value;
            this.exclusive = exclusive;
        }

        public ExactDecimal getValue() {
            return value;
        }

        public boolean isExclusive() {
            return exclusive;
        }
    }

    /** The effective lower bound on numeric instances, or {@code null} if none is present. */
    public static Bound normalizeMinimum(SchemaView schema) {
        return normalizeBound(schema, "minimum", "exclusiveMinimum", true);
    }

    /** The effective upper bound on numeric instances, or {@code null} if none is present. */
    public static Bound normalizeMaximum(SchemaView schema) {
        return normalizeBound(schema, "maximum", "exclusiveMaximum", false);
    }

    private static Bound normalizeBound(SchemaView schema, String inclusiveKeyword, String exclusiveKeyword, boolean isMinimum) {
        JsonNode inclusiveNode = schema.getKeyword(inclusiveKeyword);
        JsonNode exclusiveNode = schema.getKeyword(exclusiveKeyword);

        if (usesLegacyExclusiveBoundStyle(schema.getDialect())) {
            if (inclusiveNode == null || !JsonUtil.isNumber(inclusiveNode)) {
                return null;
            }
            boolean exclusive = exclusiveNode != null && JsonUtil.isBoolean(exclusiveNode)
                    && JsonUtil.toBoolean(exclusiveNode).booleanValue();
            return new Bound(exactValueOf(inclusiveNode), exclusive);
        }

        boolean hasInclusive = inclusiveNode != null && JsonUtil.isNumber(inclusiveNode);
        boolean hasExclusive = exclusiveNode != null && JsonUtil.isNumber(exclusiveNode);
        if (!hasInclusive && !hasExclusive) {
            return null;
        }
        if (hasInclusive && !hasExclusive) {
            return new Bound(exactValueOf(inclusiveNode), false);
        }
        if (!hasInclusive) {
            return new Bound(exactValueOf(exclusiveNode), true);
        }
        ExactDecimal inclusiveValue = exactValueOf(inclusiveNode);
        ExactDecimal exclusiveValue = exactValueOf(exclusiveNode);
        int comparison = inclusiveValue.compareTo(exclusiveValue);
        boolean inclusiveIsTighter = isMinimum ? comparison > 0 : comparison < 0;
        if (inclusiveIsTighter) {
            return new Bound(inclusiveValue, false);
        }
        // Exclusive is at least as tight (or the two are exactly equal, in which
        // case exclusive is the stricter of the two): prefer it.
        return new Bound(exclusiveValue, true);
    }

    private static ExactDecimal exactValueOf(JsonNode number) {
        return ExactDecimal.parse(JsonUtil.toNumber(number).toString());
    }

    /**
     * The effective set of instance type names this schema admits, folding OAS
     * 3.0's {@code nullable} into {@code type} when applicable. {@code type} may
     * be a single string or an array of strings in every dialect this checker
     * supports; the result always has array shape. Returns {@code null} if
     * {@code type} is absent (unconstrained by type).
     */
    public static java.util.List<String> normalizeEffectiveTypes(SchemaView schema) {
        JsonNode typeNode = schema.getKeyword("type");
        if (typeNode == null) {
            return null;
        }
        java.util.List<String> types = new java.util.ArrayList<String>();
        if (JsonUtil.isString(typeNode)) {
            types.add(JsonUtil.toString(typeNode));
        } else if (JsonUtil.isArray(typeNode)) {
            java.util.List<JsonNode> items = JsonUtil.toList(typeNode);
            for (int i = 0; i < items.size(); i++) {
                types.add(JsonUtil.toString(items.get(i)));
            }
        } else {
            return null;
        }
        if (schema.getDialect() == SchemaDialect.OAS30 || schema.getDialect() == SchemaDialect.OAS20) {
            JsonNode nullableNode = schema.getKeyword("nullable");
            boolean nullable = nullableNode != null && JsonUtil.isBoolean(nullableNode)
                    && JsonUtil.toBoolean(nullableNode).booleanValue();
            // `nullable` only has an effect because a sibling `type` is present here
            // (we already returned null above when `type` is absent) -- an ignored
            // Reference Object's nullable sibling never reaches this method at all,
            // since a $ref schema view's own keywords are never consulted for type.
            if (nullable && !types.contains("null")) {
                types.add("null");
            }
        }
        return types;
    }
}
