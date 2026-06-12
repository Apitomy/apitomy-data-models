package io.test.synthetic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.union.BooleanSchemaUnion;
import java.util.List;

public interface SynItem extends Node {

	public Number getWeight();

	public void setWeight(Number value);

	public Boolean isRequired();

	public void setRequired(Boolean value);

	public SynSchema getSchema();

	public void setSchema(SynSchema value);

	public SynSchema createSchema();

	public List<JsonNode> getExamples();

	public void setExamples(List<JsonNode> value);

	public JsonNode getExtra();

	public void setExtra(JsonNode value);

	public ObjectNode getRaw();

	public void setRaw(ObjectNode value);

	public String getTitle();

	public void setTitle(String value);

	public BooleanSchemaUnion getDefaultValue();

	public void setDefaultValue(BooleanSchemaUnion value);

	public Integer getOrder();

	public void setOrder(Integer value);

	public String getDescription();

	public void setDescription(String value);
}