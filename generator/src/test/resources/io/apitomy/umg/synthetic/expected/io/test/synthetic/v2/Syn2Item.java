package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.SynItem;
import java.util.Map;

public interface Syn2Item extends SynItem, Syn2Extensible, Syn2Referenceable {

	public Boolean isDeprecated();

	public void setDeprecated(Boolean value);

	public String get$ref();

	public void set$ref(String value);

	public Map<String, JsonNode> getExtensions();

	public void addExtension(String name, JsonNode value);

	public void clearExtensions();

	public void removeExtension(String name);

	public void insertExtension(String name, JsonNode value, int atIndex);
}