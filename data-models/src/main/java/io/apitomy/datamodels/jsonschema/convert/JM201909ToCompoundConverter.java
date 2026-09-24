package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaJsonSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValue;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValueImpl;
import io.apitomy.datamodels.models.jsonschema.modern.v201909.visitors.JM201909ToJCConversionVisitor;
import io.apitomy.datamodels.util.NumberUtil;


/**
 * Converts 2019-09 schemas to the compound schema type.
 * Handles: minimum/maximum number and exclusiveMinimum/Maximum number to RangeValue.
 * When both minimum and exclusiveMinimum are present, the tighter constraint wins.
 * Tuple {@code items} becomes {@code prefixItems}, as in drafts 4-7, whose tuple syntax 2019-09 keeps.
 */
public class JM201909ToCompoundConverter extends JM201909ToJCConversionVisitor {

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

    @Override
    public void convertFullSchemaItems(BooleanFullSchemaJsonSchemaListUnion value, JCFullSchema target) {
        CompoundSchemaConverter.normalizeItems(value, target, ModelType.JM201909);
    }

    private static boolean isTighterMinimum(Number newValue, boolean newExclusive, JCRangeValue existing) {
        int cmp = NumberUtil.compare(newValue, existing.getValue());
        if (cmp > 0) return true;
        if (cmp < 0) return false;
        return newExclusive && !Boolean.TRUE.equals(existing.isExclusive());
    }

    private static boolean isTighterMaximum(Number newValue, boolean newExclusive, JCRangeValue existing) {
        int cmp = NumberUtil.compare(newValue, existing.getValue());
        if (cmp < 0) return true;
        if (cmp > 0) return false;
        return newExclusive && !Boolean.TRUE.equals(existing.isExclusive());
    }

    private static JCRangeValue rangeValue(Number value, boolean exclusive) {
        JCRangeValueImpl rv = new JCRangeValueImpl();
        rv.setValue(value);
        rv.setExclusive(exclusive);
        return rv;
    }
}
