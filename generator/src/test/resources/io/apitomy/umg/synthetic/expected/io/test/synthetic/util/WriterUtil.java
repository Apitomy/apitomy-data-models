package io.test.synthetic.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.Node;

public class WriterUtil {

	public static final void writeExtraProperties(Node node, ObjectNode json) {
		if (node.hasExtraProperties()) {
			java.util.List<String> extraPropertyNames = node.getExtraPropertyNames();
			for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
				String name = extraPropertyNames.get(_idx);
				JsonNode value = node.getExtraProperty(name);
				JsonUtil.setProperty(json, name, value);
			}
		}
	}

}
