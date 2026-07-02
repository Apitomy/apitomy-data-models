package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.visitors.JD6ToJCConversionVisitor;
import io.apitomy.datamodels.models.union.NumberUnionValueImpl;

/**
 * Converts Draft 6 schemas to the compound schema type.
 * Handles: exclusiveMinimum/Maximum number → boolean|number union.
 */
public class JD6ToCompoundConverter extends JD6ToJCConversionVisitor {

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
