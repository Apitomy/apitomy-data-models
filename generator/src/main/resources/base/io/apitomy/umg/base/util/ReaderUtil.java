package io.apitomy.umg.base.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.umg.base.Node;

public class ReaderUtil {

    public static final void readExtraProperties(ObjectNode json, Node node) {
        if (json != null) {
            JsonUtil.keys(json).forEach(key -> {
                JsonNode value = JsonUtil.consumeAnyProperty(json, key);
                node.addExtraProperty(key, value);
            });
        }
    }
    
}
