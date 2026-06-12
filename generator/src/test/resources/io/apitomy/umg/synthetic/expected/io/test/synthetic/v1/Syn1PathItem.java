package io.test.synthetic.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.SynPathItem;
import java.util.Map;

public interface Syn1PathItem extends SynPathItem, Syn1Extensible, Syn1Referenceable {

	public String get$ref();

	public void set$ref(String value);

	public Map<String, JsonNode> getExtensions();

	public void addExtension(String name, JsonNode value);

	public void clearExtensions();

	public void removeExtension(String name);

	public void insertExtension(String name, JsonNode value, int atIndex);
}