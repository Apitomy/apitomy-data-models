package io.test.synthetic;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.union.BooleanSchemaSchemaListUnion;
import io.test.synthetic.union.BooleanSchemaUnion;
import java.util.List;
import java.util.Map;

public interface SynSchema extends Node, BooleanSchemaUnion, BooleanSchemaSchemaListUnion {

	public SynSchema createSchema();

	public List<BooleanSchemaUnion> getAllOf();

	public void addAllOf(BooleanSchemaUnion value);

	public void clearAllOf();

	public void removeAllOf(BooleanSchemaUnion value);

	public void insertAllOf(BooleanSchemaUnion value, int atIndex);

	public Map<String, BooleanSchemaUnion> getDefinitions();

	public void addDefinition(String name, BooleanSchemaUnion value);

	public void clearDefinitions();

	public void removeDefinition(String name);

	public void insertDefinition(String name, BooleanSchemaUnion value, int atIndex);

	public List<JsonNode> getEnum();

	public void setEnum(List<JsonNode> value);

	public BooleanSchemaSchemaListUnion getItems();

	public void setItems(BooleanSchemaSchemaListUnion value);

	public Integer getMaxLength();

	public void setMaxLength(Integer value);

	public Integer getMinLength();

	public void setMinLength(Integer value);

	public Map<String, BooleanSchemaUnion> getProperties();

	public void addProperty(String name, BooleanSchemaUnion value);

	public void clearProperties();

	public void removeProperty(String name);

	public void insertProperty(String name, BooleanSchemaUnion value, int atIndex);

	public String getType();

	public void setType(String value);
}