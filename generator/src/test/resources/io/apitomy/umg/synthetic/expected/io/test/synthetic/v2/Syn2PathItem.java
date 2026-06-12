package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.SynPathItem;
import java.util.Map;

public interface Syn2PathItem extends SynPathItem, Syn2Extensible, Syn2Referenceable {

	public Syn2Operation getDelete();

	public void setDelete(Syn2Operation value);

	public Syn2Operation createOperation();

	public String get$ref();

	public void set$ref(String value);

	public Map<String, JsonNode> getExtensions();

	public void addExtension(String name, JsonNode value);

	public void clearExtensions();

	public void removeExtension(String name);

	public void insertExtension(String name, JsonNode value, int atIndex);
}