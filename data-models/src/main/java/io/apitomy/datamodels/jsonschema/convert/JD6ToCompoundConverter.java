package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValue;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValueImpl;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.visitors.JD6ToJCConversionVisitor;

import java.math.BigDecimal;

/**
 * Converts Draft 6 schemas to the compound schema type.
 * Handles: minimum/maximum number and exclusiveMinimum/Maximum number to RangeValue.
 * When both minimum and exclusiveMinimum are present, the tighter constraint wins.
 */
public class JD6ToCompoundConverter extends JD6ToJCConversionVisitor {

    @Override
    public void convertFullSchemaMinimum(Number value, JCFullSchema target) {
        if (value != null) {
            target.setMinimum(rangeValue(value, false));
        }
    }

    @Override
    public void convertFullSchemaMaximum(Number value, JCFullSchema target) {
        if (value != null) {
            target.setMaximum(rangeValue(value, false));
        }
    }

    @Override
    public void convertFullSchemaExclusiveMinimum(Number value, JCFullSchema target) {
        if (value != null) {
            JCRangeValue existing = target.getMinimum();
            if (existing == null || isTighterMinimum(value, true, existing)) {
                target.setMinimum(rangeValue(value, true));
            }
        }
    }

    @Override
    public void convertFullSchemaExclusiveMaximum(Number value, JCFullSchema target) {
        if (value != null) {
            JCRangeValue existing = target.getMaximum();
            if (existing == null || isTighterMaximum(value, true, existing)) {
                target.setMaximum(rangeValue(value, true));
            }
        }
    }

    /**
     * For minimum (lower bound), a higher value is tighter.
     * At the same value, exclusive is tighter than inclusive.
     */
    private static boolean isTighterMinimum(Number newValue, boolean newExclusive, JCRangeValue existing) {
        int cmp = new BigDecimal(newValue.toString()).compareTo(new BigDecimal(existing.getValue().toString()));
        if (cmp > 0) return true;
        if (cmp < 0) return false;
        // Same value: exclusive is tighter
        return newExclusive && !Boolean.TRUE.equals(existing.isExclusive());
    }

    /**
     * For maximum (upper bound), a lower value is tighter.
     * At the same value, exclusive is tighter than inclusive.
     */
    private static boolean isTighterMaximum(Number newValue, boolean newExclusive, JCRangeValue existing) {
        int cmp = new BigDecimal(newValue.toString()).compareTo(new BigDecimal(existing.getValue().toString()));
        if (cmp < 0) return true;
        if (cmp > 0) return false;
        // Same value: exclusive is tighter
        return newExclusive && !Boolean.TRUE.equals(existing.isExclusive());
    }

    private static JCRangeValue rangeValue(Number value, boolean exclusive) {
        JCRangeValueImpl rv = new JCRangeValueImpl();
        rv.setValue(value);
        rv.setExclusive(exclusive);
        return rv;
    }
}
