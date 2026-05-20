package io.apitomy.umg.base.union;

public interface UnionValue<T> {

    T getValue();

    void setValue(T value);

    boolean isList();

    boolean isMap();

}
