package io.apitomy.umg.base.union;

import java.util.Map;

import io.apitomy.umg.base.Node;

public class EntityMapUnionValueImpl<T extends Node> extends MapUnionValueImpl<T> implements EntityMapUnionValue<T> {

    public EntityMapUnionValueImpl() {
        super();
    }

    public EntityMapUnionValueImpl(Map<String, T> value) {
        super(value);
    }

    @Override
    public boolean isEntityMap() {
        return true;
    }

}
