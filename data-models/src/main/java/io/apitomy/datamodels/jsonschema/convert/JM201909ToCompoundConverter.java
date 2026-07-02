package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.modern.v201909.visitors.JM201909ToJCConversionVisitor;
import io.apitomy.datamodels.models.union.NumberUnionValueImpl;

/**
 * Converts 2019-09 schemas to the compound schema type.
 * Handles: exclusiveMinimum/Maximum number → boolean|number union.
 */
public class JM201909ToCompoundConverter extends JM201909ToJCConversionVisitor {

    @Override
    public void convertFullSchemaExclusiveMinimum(Number value, JCFullSchema target) {
        if (value != null) {
            target.setExclusiveMinimum(new NumberUnionValueImpl(value));
        }
    }

    @Override
    public void convertFullSchemaExclusiveMaximum(Number value, JCFullSchema target) {
        if (value != null) {
            target.setExclusiveMaximum(new NumberUnionValueImpl(value));
        }
    }
}
