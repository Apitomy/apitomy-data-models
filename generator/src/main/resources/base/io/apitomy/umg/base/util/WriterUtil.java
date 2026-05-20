package io.apitomy.umg.base.util;

import io.apitomy.umg.base.Node;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

public class WriterUtil {

    public static final void writeExtraProperties(Node node, ObjectNode json) {
        if (node.hasExtraProperties()) {
            node.getExtraPropertyNames().forEach(name -> {
                JsonNode value = node.getExtraProperty(name);
                JsonUtil.setProperty(json, name, value);
            });
        }
    }
    
}
