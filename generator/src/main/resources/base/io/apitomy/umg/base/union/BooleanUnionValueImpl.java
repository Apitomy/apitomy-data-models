package io.apitomy.umg.base.union;

import io.apitomy.umg.base.ModelType;

public class BooleanUnionValueImpl extends PrimitiveUnionValueImpl<Boolean> implements BooleanUnionValue {

    public BooleanUnionValueImpl(Boolean value) {
        super(value);
    }

    public BooleanUnionValueImpl(Boolean value, ModelType modelType) {
        super(value, modelType);
    }

}
