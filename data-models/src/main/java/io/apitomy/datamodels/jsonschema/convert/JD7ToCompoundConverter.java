package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaJsonSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.Dependency;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValue;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValueImpl;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.visitors.JD7ToJCConversionVisitor;

import java.util.Map;
import io.apitomy.datamodels.util.NumberUtil;

/**
 * Converts Draft 7 schemas to the compound schema type.
 * Handles: minimum/maximum number and exclusiveMinimum/Maximum number to RangeValue.
 * When both minimum and exclusiveMinimum are present, the tighter constraint wins.
 */
public class JD7ToCompoundConverter extends JD7ToJCConversionVisitor {

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
                CompoundSchemaConverter.recordSourceKeyword(target, "minimum", "exclusiveMinimum");
            }
        }
    }

    @Override
    public void convertFullSchemaExclusiveMaximum(Number value, JCFullSchema target) {
        if (value != null) {
            JCRangeValue existing = target.getMaximum();
            if (existing == null || isTighterMaximum(value, true, existing)) {
                target.setMaximum(rangeValue(value, true));
                CompoundSchemaConverter.recordSourceKeyword(target, "maximum", "exclusiveMaximum");
            }
        }
    }

    @Override
    public void convertFullSchemaItems(BooleanFullSchemaJsonSchemaListUnion value, JCFullSchema target) {
        CompoundSchemaConverter.normalizeItems(value, target, ModelType.JD7);
    }

    @Override
    public void convertFullSchemaDependencies(Map<String, Dependency> value, JCFullSchema target) {
        CompoundSchemaConverter.splitDependencies(value, target);
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
