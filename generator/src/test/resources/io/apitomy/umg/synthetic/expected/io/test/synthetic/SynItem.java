package io.test.synthetic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

public interface SynItem extends Node {

	public BooleanSchemaUnion getDefaultValue();

	public void setDefaultValue(BooleanSchemaUnion value);

	public SynSchema createSchema();

	public String getDescription();

	public void setDescription(String value);

	public List<JsonNode> getExamples();

	public void setExamples(List<JsonNode> value);

	public JsonNode getExtra();

	public void setExtra(JsonNode value);

	public Integer getOrder();

	public void setOrder(Integer value);

	public ObjectNode getRaw();

	public void setRaw(ObjectNode value);

	public Boolean isRequired();

	public void setRequired(Boolean value);

	public SynSchema getSchema();

	public void setSchema(SynSchema value);

	public String getTitle();

	public void setTitle(String value);

	public Number getWeight();

	public void setWeight(Number value);
}