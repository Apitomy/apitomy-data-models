package io.test.synthetic.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.Node;

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
