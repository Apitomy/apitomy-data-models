package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaFullSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.modern.v202012.visitors.JM202012ToJCConversionVisitor;
import io.apitomy.datamodels.models.union.NumberUnionValueImpl;

/**
 * Converts 2020-12 schemas to the compound schema type.
 * Handles: exclusiveMinimum/Maximum number → boolean|number union,
 *          items JsonSchema → boolean|FullSchema|[FullSchema] union.
 */
public class JM202012ToCompoundConverter extends JM202012ToJCConversionVisitor {

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

    @Override
    public void convertFullSchemaItems(JsonSchema value, JCFullSchema target) {
        if (value != null) {
            target.setItems((BooleanFullSchemaFullSchemaListUnion) value);
        }
    }
}
