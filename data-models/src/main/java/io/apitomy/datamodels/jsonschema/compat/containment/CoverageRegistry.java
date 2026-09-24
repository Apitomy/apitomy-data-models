package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * What this checker knows about the keywords of each {@link SchemaDialect}:
 * which affect containment ({@link Category#ASSERTION}), which are
 * schema-valued children to recurse into ({@link Category#SCHEMA_CHILD}),
 * which are non-constraining metadata ({@link Category#ANNOTATION}), and
 * which are recognized but not (yet) analyzed, with an explicit reason
 * ({@link Category#UNSUPPORTED}).
 * <p>
 * There is no default "no-op means supported": {@link #classify} returns
 * {@code null} for a keyword this registry does not know about under a given
 * dialect at all, and a caller must treat that the same as
 * {@link Category#UNSUPPORTED} -- an unrecognized keyword must never be
 * silently treated as a non-constraining annotation just because nothing
 * else claimed it.
 */
public final class CoverageRegistry {

    /** How a keyword affects (or does not affect) a containment proof. */
    public enum Category {
        /** Constrains which instances validate; containment must account for it. */
        ASSERTION,
        /** A schema-valued keyword (or map/list of schemas) to recurse into. */
        SCHEMA_CHILD,
        /** Non-constraining metadata; safe to ignore for containment. */
        ANNOTATION,
        /** Recognized, but this checker does not (yet) analyze its effect. */
        UNSUPPORTED
    }

    /** A keyword's classification under a dialect, with a reason when {@link Category#UNSUPPORTED}. */
    public static final class Coverage {
        private final Category category;
        private final String reason;

        Coverage(Category category, String reason) {
            this.category = category;
            this.reason = reason;
        }

        public Category getCategory() {
            return category;
        }

        /** Why this keyword is unsupported, non-null only when {@link #getCategory()} is {@link Category#UNSUPPORTED}. */
        public String getReason() {
            return reason;
        }
    }

    private static final Map<SchemaDialect, Map<String, Coverage>> BY_DIALECT = new LinkedHashMap<SchemaDialect, Map<String, Coverage>>();

    static {
        Map<String, Coverage> modern = new LinkedHashMap<String, Coverage>();
        putAll(modern, Category.ASSERTION, new String[] {
                "type", "enum", "const", "multipleOf", "maximum", "exclusiveMaximum", "minimum", "exclusiveMinimum",
                "maxLength", "minLength", "pattern", "maxItems", "minItems", "uniqueItems", "maxContains", "minContains",
                "maxProperties", "minProperties", "required", "dependentRequired", "format",
        });
        putAll(modern, Category.SCHEMA_CHILD, new String[] {
                "items", "prefixItems", "contains", "additionalProperties", "properties", "patternProperties",
                "propertyNames", "unevaluatedItems", "unevaluatedProperties", "allOf", "anyOf", "oneOf", "not",
                "if", "then", "else", "dependentSchemas", "contentSchema", "$defs",
        });
        putAll(modern, Category.ANNOTATION, new String[] {
                "title", "description", "default", "examples", "example", "deprecated", "readOnly", "writeOnly",
                "contentMediaType", "contentEncoding", "$comment", "$id", "$schema", "$anchor", "$dynamicAnchor",
                "discriminator", "xml", "externalDocs",
        });
        putAll(modern, Category.UNSUPPORTED, new String[] { "$dynamicRef" },
                "$dynamicRef resolves against the dynamic scope at validation time, which this checker does not model");
        putAll(modern, Category.UNSUPPORTED, new String[] { "$vocabulary" },
                "custom vocabulary requirements are not evaluated");
        BY_DIALECT.put(SchemaDialect.OAS31, modern);
        BY_DIALECT.put(SchemaDialect.OAS32, modern);
        BY_DIALECT.put(SchemaDialect.DRAFT2020_12, modern);
        BY_DIALECT.put(SchemaDialect.DRAFT2019_09, modern);

        Map<String, Coverage> legacy = new LinkedHashMap<String, Coverage>();
        putAll(legacy, Category.ASSERTION, new String[] {
                "type", "enum", "multipleOf", "maximum", "exclusiveMaximum", "minimum", "exclusiveMinimum",
                "maxLength", "minLength", "pattern", "maxItems", "minItems", "uniqueItems",
                "maxProperties", "minProperties", "required", "format",
        });
        putAll(legacy, Category.SCHEMA_CHILD, new String[] {
                "items", "additionalProperties", "properties", "patternProperties", "allOf", "anyOf", "oneOf", "not",
                "definitions",
        });
        putAll(legacy, Category.ANNOTATION, new String[] {
                "title", "description", "default", "example", "$comment", "id",
        });
        BY_DIALECT.put(SchemaDialect.DRAFT4, legacy);
        BY_DIALECT.put(SchemaDialect.DRAFT6, copyWith(legacy, Category.ASSERTION, new String[] { "const" }));
        BY_DIALECT.put(SchemaDialect.DRAFT7, copyWith(legacy, Category.ANNOTATION, new String[] { "contentMediaType", "contentEncoding" }));

        Map<String, Coverage> oas20 = copyWith(legacy, Category.ANNOTATION, new String[] { "xml", "externalDocs" });
        BY_DIALECT.put(SchemaDialect.OAS20, oas20);

        Map<String, Coverage> oas30 = copyWith(oas20, Category.ANNOTATION, new String[0]);
        // OAS 3.0's `nullable` conditionally widens the effective `type` and so
        // affects containment; SchemaNormalizer folds it into `type` before proof
        // rules see it, but coverage still records it as an assertion, not
        // metadata, so an un-normalized view is never mistaken for having no
        // constraint here.
        putAll(oas30, Category.ASSERTION, new String[] { "nullable" });
        putAll(oas30, Category.ANNOTATION, new String[] { "discriminator" });
        BY_DIALECT.put(SchemaDialect.OAS30, oas30);
    }

    private CoverageRegistry() {
    }

    private static void putAll(Map<String, Coverage> target, Category category, String[] keywords) {
        for (int i = 0; i < keywords.length; i++) {
            target.put(keywords[i], new Coverage(category, null));
        }
    }

    private static void putAll(Map<String, Coverage> target, Category category, String[] keywords, String reason) {
        for (int i = 0; i < keywords.length; i++) {
            target.put(keywords[i], new Coverage(category, reason));
        }
    }

    private static Map<String, Coverage> copyWith(Map<String, Coverage> base, Category category, String[] keywords) {
        Map<String, Coverage> copy = new LinkedHashMap<String, Coverage>();
        for (String key : base.keySet()) {
            copy.put(key, base.get(key));
        }
        putAll(copy, category, keywords);
        return copy;
    }

    /**
     * This keyword's classification under {@code dialect}, or {@code null} if
     * this registry does not know about it at all -- which a caller must treat
     * as a coverage gap, the same as an explicit {@link Category#UNSUPPORTED}.
     */
    public static Coverage classify(SchemaDialect dialect, String keyword) {
        Map<String, Coverage> keywords = BY_DIALECT.get(dialect);
        if (keywords == null) {
            return null;
        }
        return keywords.get(keyword);
    }

    /** Every keyword this registry knows about under {@code dialect}, regardless of category. */
    public static Set<String> knownKeywords(SchemaDialect dialect) {
        Map<String, Coverage> keywords = BY_DIALECT.get(dialect);
        Set<String> result = new HashSet<String>();
        if (keywords == null) {
            return result;
        }
        for (String keyword : keywords.keySet()) {
            result.add(keyword);
        }
        return result;
    }
}
