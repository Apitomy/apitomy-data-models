package io.apitomy.umg.base.union;

import com.fasterxml.jackson.databind.JsonNode;

public class AnyUnionValueImpl extends PrimitiveUnionValueImpl<JsonNode> implements AnyUnionValue {

    public AnyUnionValueImpl() {
        super();
    }

    public AnyUnionValueImpl(JsonNode value) {
        super(value);
    }

}
