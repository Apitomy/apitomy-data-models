package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.visitors.JD4ToJCConversionVisitor;
import io.apitomy.datamodels.models.union.BooleanUnionValueImpl;

/**
 * Converts Draft 4 schemas to the compound schema type.
 * Handles: exclusiveMinimum/Maximum boolean → boolean|number union.
 */
public class JD4ToCompoundConverter extends JD4ToJCConversionVisitor {

    @Override
    public void convertFullSchemaExclusiveMinimum(Boolean value, JCFullSchema target) {
        if (value != null) {
            target.setExclusiveMinimum(new BooleanUnionValueImpl(value));
        }
    }

    @Override
    public void convertFullSchemaExclusiveMaximum(Boolean value, JCFullSchema target) {
        if (value != null) {
            target.setExclusiveMaximum(new BooleanUnionValueImpl(value));
        }
    }
}
