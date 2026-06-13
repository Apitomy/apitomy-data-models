package io.apitomy.umg.base.union;

import io.apitomy.umg.base.ModelType;

public abstract class PrimitiveUnionValueImpl<T> extends UnionValueImpl<T> implements PrimitiveUnionValue<T> {

    public PrimitiveUnionValueImpl() {
    }

    public PrimitiveUnionValueImpl(T value) {
        super(value);
    }

    public PrimitiveUnionValueImpl(T value, ModelType modelType) {
        super(value, modelType);
    }

}
