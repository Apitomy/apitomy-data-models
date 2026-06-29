package io.test.synthetic;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

public interface SynSchema extends Node, SchemaOrBoolean, BooleanSchemaSchemaListUnion, BooleanSchemaUnion {

	public SynSchema createSchema();

	public List<BooleanSchemaUnion> getAllOf();

	public void addAllOf(BooleanSchemaUnion value);

	public void clearAllOf();

	public void removeAllOf(BooleanSchemaUnion value);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertAllOf(BooleanSchemaUnion value, int atIndex);

	public List<SchemaOrBoolean> getComposedSchemas();

	public void addComposedSchema(SchemaOrBoolean value);

	public void clearComposedSchemas();

	public void removeComposedSchema(SchemaOrBoolean value);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertComposedSchema(SchemaOrBoolean value, int atIndex);

	public Map<String, BooleanSchemaUnion> getDefinitions();

	public void addDefinition(String name, BooleanSchemaUnion value);

	public void clearDefinitions();

	public void removeDefinition(String name);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertDefinition(String name, BooleanSchemaUnion value, int atIndex);

	public List<JsonNode> getEnum();

	public void setEnum(List<JsonNode> value);

	public BooleanSchemaSchemaListUnion getItems();

	public void setItems(BooleanSchemaSchemaListUnion value);

	public Integer getMaxLength();

	public void setMaxLength(Integer value);

	public Integer getMinLength();

	public void setMinLength(Integer value);

	public Map<String, SchemaOrBoolean> getNestedSchemas();

	public void addNestedSchema(String name, SchemaOrBoolean value);

	public void clearNestedSchemas();

	public void removeNestedSchema(String name);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertNestedSchema(String name, SchemaOrBoolean value, int atIndex);

	public Map<String, BooleanSchemaUnion> getProperties();

	public void addProperty(String name, BooleanSchemaUnion value);

	public void clearProperties();

	public void removeProperty(String name);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertProperty(String name, BooleanSchemaUnion value, int atIndex);

	public String getType();

	public void setType(String value);
}