package io.apitomy.umg.base.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.umg.base.Node;

public class ReaderUtil {

    public static final void readExtraProperties(ObjectNode json, Node node) {
        if (json != null) {
            java.util.List<String> _keys = JsonUtil.keys(json);
            for (int _i = 0; _i < _keys.size(); _i++) {
                String key = _keys.get(_i);
                JsonNode value = JsonUtil.getProperty(json, key);
                if (JsonUtil.isJsonNode(value)) {
                    node.addExtraProperty(key, value);
                    json.remove(key);
                }
            }
        }
    }
    
}
