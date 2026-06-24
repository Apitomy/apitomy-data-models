package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.JD4FullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.JD6FullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.JD7FullSchema;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

public class NumberSchemaDiff {

    private final DiffContext ctx;
    private final JFullSchema original;
    private final JFullSchema updated;
    private final String effectiveType;

    public NumberSchemaDiff(DiffContext ctx, JFullSchema original, JFullSchema updated,
                                   String effectiveType) {
        this.ctx = ctx;
        this.original = original;
        this.updated = updated;
        this.effectiveType = effectiveType;
    }

    public void visit() {
        var origExclMin = getEffectiveExclusiveMinimum(original);
        var updExclMin = getEffectiveExclusiveMinimum(updated);
        var origExclMax = getEffectiveExclusiveMaximum(original);
        var updExclMax = getEffectiveExclusiveMaximum(updated);

        var origMinimum = original.getMinimum();
        var updMinimum = updated.getMinimum();
        if (isDraft4(original) && Boolean.TRUE.equals(getDraft4ExclusiveMinimum(original))) {
            origMinimum = null;
        }
        if (isDraft4(updated) && Boolean.TRUE.equals(getDraft4ExclusiveMinimum(updated))) {
            updMinimum = null;
        }
        if (origMinimum == null && updMinimum != null && origExclMin != null) {
            if (toBigDecimal(origExclMin).compareTo(toBigDecimal(updMinimum)) >= 0) {
                updMinimum = null;
            }
        }
        if (origMinimum != null && updMinimum == null && updExclMin != null) {
            if (toBigDecimal(updExclMin).compareTo(toBigDecimal(origMinimum)) >= 0) {
                origMinimum = null;
            }
        }
        diffNumber(ctx, origMinimum, updMinimum,
                NUMBER_TYPE_MINIMUM_ADDED, NUMBER_TYPE_MINIMUM_REMOVED,
                NUMBER_TYPE_MINIMUM_INCREASED, NUMBER_TYPE_MINIMUM_DECREASED);

        var origMaximum = original.getMaximum();
        var updMaximum = updated.getMaximum();
        if (isDraft4(original) && Boolean.TRUE.equals(getDraft4ExclusiveMaximum(original))) {
            origMaximum = null;
        }
        if (isDraft4(updated) && Boolean.TRUE.equals(getDraft4ExclusiveMaximum(updated))) {
            updMaximum = null;
        }
        if (origMaximum == null && updMaximum != null && origExclMax != null) {
            if (toBigDecimal(origExclMax).compareTo(toBigDecimal(updMaximum)) <= 0) {
                updMaximum = null;
            }
        }
        if (origMaximum != null && updMaximum == null && updExclMax != null) {
            if (toBigDecimal(updExclMax).compareTo(toBigDecimal(origMaximum)) <= 0) {
                origMaximum = null;
            }
        }
        diffNumber(ctx, origMaximum, updMaximum,
                NUMBER_TYPE_MAXIMUM_ADDED, NUMBER_TYPE_MAXIMUM_REMOVED,
                NUMBER_TYPE_MAXIMUM_INCREASED, NUMBER_TYPE_MAXIMUM_DECREASED);

        diffExclusiveMinimum();
        diffExclusiveMaximum();
        diffMultipleOf();
        diffIntegerRequired();
    }

    private void diffExclusiveMinimum() {
        if (isDraft4(original) && isDraft4(updated)) {
            var origExcl = getDraft4ExclusiveMinimum(original);
            var updExcl = getDraft4ExclusiveMinimum(updated);
            diffBooleanTransition(ctx, origExcl, updExcl, false,
                    NUMBER_TYPE_IS_MINIMUM_EXCLUSIVE_FALSE_TO_TRUE,
                    NUMBER_TYPE_IS_MINIMUM_EXCLUSIVE_TRUE_TO_FALSE,
                    NUMBER_TYPE_IS_MINIMUM_EXCLUSIVE_UNCHANGED);
        } else if (isDraft4(original) && !isDraft4(updated)) {
            var origIsExcl = getDraft4ExclusiveMinimum(original);
            var origMin = original.getMinimum();
            Number effectiveOrigExclMin = (Boolean.TRUE.equals(origIsExcl) && origMin != null) ? origMin : null;
            var updExcl = getExclusiveMinimumAsNumber(updated);
            diffNumber(ctx, effectiveOrigExclMin, updExcl,
                    NUMBER_TYPE_EXCLUSIVE_MINIMUM_ADDED, NUMBER_TYPE_EXCLUSIVE_MINIMUM_REMOVED,
                    NUMBER_TYPE_EXCLUSIVE_MINIMUM_INCREASED, NUMBER_TYPE_EXCLUSIVE_MINIMUM_DECREASED);
        } else if (!isDraft4(original) && isDraft4(updated)) {
            var origExcl = getExclusiveMinimumAsNumber(original);
            var updIsExcl = getDraft4ExclusiveMinimum(updated);
            var updMin = updated.getMinimum();
            Number effectiveUpdExclMin = (Boolean.TRUE.equals(updIsExcl) && updMin != null) ? updMin : null;
            diffNumber(ctx, origExcl, effectiveUpdExclMin,
                    NUMBER_TYPE_EXCLUSIVE_MINIMUM_ADDED, NUMBER_TYPE_EXCLUSIVE_MINIMUM_REMOVED,
                    NUMBER_TYPE_EXCLUSIVE_MINIMUM_INCREASED, NUMBER_TYPE_EXCLUSIVE_MINIMUM_DECREASED);
        } else {
            var origExcl = getExclusiveMinimumAsNumber(original);
            var updExcl = getExclusiveMinimumAsNumber(updated);
            diffNumber(ctx, origExcl, updExcl,
                    NUMBER_TYPE_EXCLUSIVE_MINIMUM_ADDED, NUMBER_TYPE_EXCLUSIVE_MINIMUM_REMOVED,
                    NUMBER_TYPE_EXCLUSIVE_MINIMUM_INCREASED, NUMBER_TYPE_EXCLUSIVE_MINIMUM_DECREASED);
        }
    }

    private void diffExclusiveMaximum() {
        if (isDraft4(original) && isDraft4(updated)) {
            var origExcl = getDraft4ExclusiveMaximum(original);
            var updExcl = getDraft4ExclusiveMaximum(updated);
            diffBooleanTransition(ctx, origExcl, updExcl, false,
                    NUMBER_TYPE_IS_MAXIMUM_EXCLUSIVE_FALSE_TO_TRUE,
                    NUMBER_TYPE_IS_MAXIMUM_EXCLUSIVE_TRUE_TO_FALSE,
                    NUMBER_TYPE_IS_MAXIMUM_EXCLUSIVE_UNCHANGED);
        } else if (isDraft4(original) && !isDraft4(updated)) {
            var origIsExcl = getDraft4ExclusiveMaximum(original);
            var origMax = original.getMaximum();
            Number effectiveOrigExclMax = (Boolean.TRUE.equals(origIsExcl) && origMax != null) ? origMax : null;
            var updExcl = getExclusiveMaximumAsNumber(updated);
            diffNumber(ctx, effectiveOrigExclMax, updExcl,
                    NUMBER_TYPE_EXCLUSIVE_MAXIMUM_ADDED, NUMBER_TYPE_EXCLUSIVE_MAXIMUM_REMOVED,
                    NUMBER_TYPE_EXCLUSIVE_MAXIMUM_INCREASED, NUMBER_TYPE_EXCLUSIVE_MAXIMUM_DECREASED);
        } else if (!isDraft4(original) && isDraft4(updated)) {
            var origExcl = getExclusiveMaximumAsNumber(original);
            var updIsExcl = getDraft4ExclusiveMaximum(updated);
            var updMax = updated.getMaximum();
            Number effectiveUpdExclMax = (Boolean.TRUE.equals(updIsExcl) && updMax != null) ? updMax : null;
            diffNumber(ctx, origExcl, effectiveUpdExclMax,
                    NUMBER_TYPE_EXCLUSIVE_MAXIMUM_ADDED, NUMBER_TYPE_EXCLUSIVE_MAXIMUM_REMOVED,
                    NUMBER_TYPE_EXCLUSIVE_MAXIMUM_INCREASED, NUMBER_TYPE_EXCLUSIVE_MAXIMUM_DECREASED);
        } else {
            var origExcl = getExclusiveMaximumAsNumber(original);
            var updExcl = getExclusiveMaximumAsNumber(updated);
            diffNumber(ctx, origExcl, updExcl,
                    NUMBER_TYPE_EXCLUSIVE_MAXIMUM_ADDED, NUMBER_TYPE_EXCLUSIVE_MAXIMUM_REMOVED,
                    NUMBER_TYPE_EXCLUSIVE_MAXIMUM_INCREASED, NUMBER_TYPE_EXCLUSIVE_MAXIMUM_DECREASED);
        }
    }

    private void diffMultipleOf() {
        var origMultipleOf = original.getMultipleOf();
        var updMultipleOf = updated.getMultipleOf();
        if (diffAddedRemoved(ctx, origMultipleOf, updMultipleOf,
                NUMBER_TYPE_MULTIPLE_OF_ADDED, NUMBER_TYPE_MULTIPLE_OF_REMOVED)) {
            diffNumberOriginalMultipleOfUpdated(ctx, origMultipleOf, updMultipleOf,
                    NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_DIVISIBLE,
                    NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_NOT_DIVISIBLE);
        }
    }

    private void diffIntegerRequired() {
        if (effectiveType == null) return;
        var origType = DiffUtil.getTypeString(original);
        var updType = DiffUtil.getTypeString(updated);
        var origIsInteger = "integer".equals(origType);
        var updIsInteger = "integer".equals(updType);
        diffBooleanTransition(ctx, origIsInteger, updIsInteger, false,
                NUMBER_TYPE_INTEGER_REQUIRED_FALSE_TO_TRUE,
                NUMBER_TYPE_INTEGER_REQUIRED_TRUE_TO_FALSE,
                NUMBER_TYPE_INTEGER_REQUIRED_UNCHANGED);
    }

    private static boolean isDraft4(JFullSchema schema) {
        return schema instanceof JD4FullSchema;
    }

    private static Boolean getDraft4ExclusiveMinimum(JFullSchema schema) {
        if (schema instanceof JD4FullSchema d) return d.isExclusiveMinimum();
        return null;
    }

    private static Boolean getDraft4ExclusiveMaximum(JFullSchema schema) {
        if (schema instanceof JD4FullSchema d) return d.isExclusiveMaximum();
        return null;
    }

    private static Number getExclusiveMinimumAsNumber(JFullSchema schema) {
        if (schema instanceof JD6FullSchema d) return d.getExclusiveMinimum();
        if (schema instanceof JD7FullSchema d) return d.getExclusiveMinimum();
        return null;
    }

    private static Number getExclusiveMaximumAsNumber(JFullSchema schema) {
        if (schema instanceof JD6FullSchema d) return d.getExclusiveMaximum();
        if (schema instanceof JD7FullSchema d) return d.getExclusiveMaximum();
        return null;
    }

    private static Number getEffectiveExclusiveMinimum(JFullSchema schema) {
        if (isDraft4(schema)) {
            if (Boolean.TRUE.equals(getDraft4ExclusiveMinimum(schema))) {
                return schema.getMinimum();
            }
            return null;
        }
        return getExclusiveMinimumAsNumber(schema);
    }

    private static Number getEffectiveExclusiveMaximum(JFullSchema schema) {
        if (isDraft4(schema)) {
            if (Boolean.TRUE.equals(getDraft4ExclusiveMaximum(schema))) {
                return schema.getMaximum();
            }
            return null;
        }
        return getExclusiveMaximumAsNumber(schema);
    }

    private static java.math.BigDecimal toBigDecimal(Number n) {
        return new java.math.BigDecimal(n.toString());
    }
}
