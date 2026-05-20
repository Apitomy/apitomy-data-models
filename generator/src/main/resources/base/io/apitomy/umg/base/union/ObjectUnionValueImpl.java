package io.apitomy.umg.base.union;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectUnionValueImpl extends PrimitiveUnionValueImpl<ObjectNode> implements ObjectUnionValue {

    public ObjectUnionValueImpl() {
        super();
    }

    public ObjectUnionValueImpl(Object value) {
        super((ObjectNode) value);
    }

}
