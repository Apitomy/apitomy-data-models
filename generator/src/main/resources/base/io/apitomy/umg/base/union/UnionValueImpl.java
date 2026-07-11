package io.apitomy.umg.base.union;

import io.apitomy.umg.base.visitors.Visitor;

/**
 * Base class for all union value implementations.
 * @author eric.wittmann@gmail.com
 */
public abstract class UnionValueImpl<T> implements UnionValue<T>, Union {

    private T value;

    public UnionValueImpl() {
    }

    public UnionValueImpl(T value) {
        this.value = value;
    }

    @Override
    public Object unionValue() {
        return value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @Override
    public boolean isEntityList() {
        return false;
    }

    @Override
    public boolean isEntityMap() {
        return false;
    }

    @Override
    public void accept(Visitor visitor) {
    }

}
