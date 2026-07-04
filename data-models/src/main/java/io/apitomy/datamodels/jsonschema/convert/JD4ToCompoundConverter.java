package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValue;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValueImpl;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.visitors.JD4ToJCConversionVisitor;

/**
 * Converts Draft 4 schemas to the compound schema type.
 * Handles: exclusiveMinimum/Maximum boolean + minimum/maximum number to RangeValue.
 */
public class JD4ToCompoundConverter extends JD4ToJCConversionVisitor {

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
    public void convertFullSchemaExclusiveMinimum(Boolean value, JCFullSchema target) {
        // In Draft 4, exclusiveMinimum is a boolean that modifies the minimum field.
        // If true, mark the existing minimum as exclusive.
        if (Boolean.TRUE.equals(value) && target.getMinimum() != null) {
            target.getMinimum().setExclusive(true);
        }
    }

    @Override
    public void convertFullSchemaExclusiveMaximum(Boolean value, JCFullSchema target) {
        // In Draft 4, exclusiveMaximum is a boolean that modifies the maximum field.
        // If true, mark the existing maximum as exclusive.
        if (Boolean.TRUE.equals(value) && target.getMaximum() != null) {
            target.getMaximum().setExclusive(true);
        }
    }

    private static JCRangeValue rangeValue(Number value, boolean exclusive) {
        JCRangeValueImpl rv = new JCRangeValueImpl();
        rv.setValue(value);
        rv.setExclusive(exclusive);
        return rv;
    }
}
