package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Any;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.ModelType;
import io.test.synthetic.Node;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.RootCapableImpl;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.SynSchema;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v2.visitors.Syn2Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn2SchemaImpl extends RootCapableImpl implements Syn2Schema {

	private String $ref;
	private String type;
	private BooleanSchemaSchemaListUnion items;
	private Map<String, BooleanSchemaUnion> properties;
	private List<BooleanSchemaUnion> allOf;
	private Map<String, BooleanSchemaUnion> definitions;
	private Map<String, SchemaOrBoolean> nestedSchemas;
	private List<SchemaOrBoolean> composedSchemas;
	private Integer minLength;
	private Integer maxLength;
	private List<JsonNode> _enum;
	private Map<String, JsonNode> extensions;

	public Syn2SchemaImpl() {
		super(ModelType.SYN2);
	}

	@Override
	public String get$ref() {
		return $ref;
	}

	@Override
	public void set$ref(String value) {
		this.$ref = value;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String value) {
		this.type = value;
	}

	@Override
	public BooleanSchemaSchemaListUnion getItems() {
		return items;
	}

	@Override
	public void setItems(BooleanSchemaSchemaListUnion value) {
		this.items = value;
		DataModelUtil.setParent(value, this, "items", ParentPropertyType.standard);
	}

	@Override
	public Syn2Schema createSchema() {
		Syn2SchemaImpl node = new Syn2SchemaImpl();
		node._setParent(this);
		return node;
	}

	@Override
	public Map<String, BooleanSchemaUnion> getProperties() {
		return properties;
	}

	@Override
	public void addProperty(String name, BooleanSchemaUnion value) {
		if (this.properties == null) {
			this.properties = new LinkedHashMap<>();
		}
		this.properties.put(name, value);
		DataModelUtil.setParentMap(value, this, "properties", ParentPropertyType.map, name);
	}

	@Override
	public void clearProperties() {
		if (this.properties != null) {
			for (Any item : this.properties.values()) {
				if (item != null)
					item.detach();
			}
			this.properties.clear();
		}
	}

	@Override
	public void removeProperty(String name) {
		if (this.properties != null) {
			this.properties.remove(name);
		}
	}

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertProperty(String name, BooleanSchemaUnion value, int atIndex) {
		if (this.properties == null) {
			this.properties = new LinkedHashMap<>();
		}
		this.properties = DataModelUtil.insertMapEntry(this.properties, name, value, atIndex);
		DataModelUtil.setParentMap(value, this, "properties", ParentPropertyType.map, name);
	}

	@Override
	public List<BooleanSchemaUnion> getAllOf() {
		return allOf;
	}

	@Override
	public void addAllOf(BooleanSchemaUnion value) {
		if (this.allOf == null) {
			this.allOf = new ArrayList<>();
		}
		this.allOf.add(value);
		DataModelUtil.setParent(value, this, "allOf", ParentPropertyType.array);
	}

	@Override
	public void clearAllOf() {
		if (this.allOf != null) {
			for (Any item : this.allOf) {
				if (item != null)
					item.detach();
			}
			this.allOf.clear();
		}
	}

	@Override
	public void removeAllOf(BooleanSchemaUnion value) {
		if (this.allOf != null) {
			if (value != null && this.allOf.remove(value)) {
				value.detach();
			}
		}
	}

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertAllOf(BooleanSchemaUnion value, int atIndex) {
		if (this.allOf == null) {
			this.allOf = new ArrayList<>();
		}
		DataModelUtil.insertListEntry(this.allOf, value, atIndex);
		DataModelUtil.setParent(value, this, "allOf", ParentPropertyType.array);
	}

	@Override
	public Map<String, BooleanSchemaUnion> getDefinitions() {
		return definitions;
	}

	@Override
	public void addDefinition(String name, BooleanSchemaUnion value) {
		if (this.definitions == null) {
			this.definitions = new LinkedHashMap<>();
		}
		this.definitions.put(name, value);
		DataModelUtil.setParentMap(value, this, "definitions", ParentPropertyType.map, name);
	}

	@Override
	public void clearDefinitions() {
		if (this.definitions != null) {
			for (Any item : this.definitions.values()) {
				if (item != null)
					item.detach();
			}
			this.definitions.clear();
		}
	}

	@Override
	public void removeDefinition(String name) {
		if (this.definitions != null) {
			this.definitions.remove(name);
		}
	}

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertDefinition(String name, BooleanSchemaUnion value, int atIndex) {
		if (this.definitions == null) {
			this.definitions = new LinkedHashMap<>();
		}
		this.definitions = DataModelUtil.insertMapEntry(this.definitions, name, value, atIndex);
		DataModelUtil.setParentMap(value, this, "definitions", ParentPropertyType.map, name);
	}

	@Override
	public Map<String, SchemaOrBoolean> getNestedSchemas() {
		return nestedSchemas;
	}

	@Override
	public void addNestedSchema(String name, SchemaOrBoolean value) {
		if (this.nestedSchemas == null) {
			this.nestedSchemas = new LinkedHashMap<>();
		}
		this.nestedSchemas.put(name, value);
		DataModelUtil.setParentMap(value, this, "nestedSchemas", ParentPropertyType.map, name);
	}

	@Override
	public void clearNestedSchemas() {
		if (this.nestedSchemas != null) {
			for (Any item : this.nestedSchemas.values()) {
				if (item != null)
					item.detach();
			}
			this.nestedSchemas.clear();
		}
	}

	@Override
	public void removeNestedSchema(String name) {
		if (this.nestedSchemas != null) {
			this.nestedSchemas.remove(name);
		}
	}

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertNestedSchema(String name, SchemaOrBoolean value, int atIndex) {
		if (this.nestedSchemas == null) {
			this.nestedSchemas = new LinkedHashMap<>();
		}
		this.nestedSchemas = DataModelUtil.insertMapEntry(this.nestedSchemas, name, value, atIndex);
		DataModelUtil.setParentMap(value, this, "nestedSchemas", ParentPropertyType.map, name);
	}

	@Override
	public List<SchemaOrBoolean> getComposedSchemas() {
		return composedSchemas;
	}

	@Override
	public void addComposedSchema(SchemaOrBoolean value) {
		if (this.composedSchemas == null) {
			this.composedSchemas = new ArrayList<>();
		}
		this.composedSchemas.add(value);
		DataModelUtil.setParent(value, this, "composedSchemas", ParentPropertyType.array);
	}

	@Override
	public void clearComposedSchemas() {
		if (this.composedSchemas != null) {
			for (Any item : this.composedSchemas) {
				if (item != null)
					item.detach();
			}
			this.composedSchemas.clear();
		}
	}

	@Override
	public void removeComposedSchema(SchemaOrBoolean value) {
		if (this.composedSchemas != null) {
			if (value != null && this.composedSchemas.remove(value)) {
				value.detach();
			}
		}
	}

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertComposedSchema(SchemaOrBoolean value, int atIndex) {
		if (this.composedSchemas == null) {
			this.composedSchemas = new ArrayList<>();
		}
		DataModelUtil.insertListEntry(this.composedSchemas, value, atIndex);
		DataModelUtil.setParent(value, this, "composedSchemas", ParentPropertyType.array);
	}

	@Override
	public Integer getMinLength() {
		return minLength;
	}

	@Override
	public void setMinLength(Integer value) {
		this.minLength = value;
	}

	@Override
	public Integer getMaxLength() {
		return maxLength;
	}

	@Override
	public void setMaxLength(Integer value) {
		this.maxLength = value;
	}

	@Override
	public List<JsonNode> getEnum() {
		return _enum;
	}

	@Override
	public void setEnum(List<JsonNode> value) {
		this._enum = value;
	}

	@Override
	public Map<String, JsonNode> getExtensions() {
		return extensions;
	}

	@Override
	public void addExtension(String name, JsonNode value) {
		if (this.extensions == null) {
			this.extensions = new LinkedHashMap<>();
		}
		this.extensions.put(name, value);
	}

	@Override
	public void clearExtensions() {
		if (this.extensions != null) {
			this.extensions.clear();
		}
	}

	@Override
	public void removeExtension(String name) {
		if (this.extensions != null) {
			this.extensions.remove(name);
		}
	}

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertExtension(String name, JsonNode value, int atIndex) {
		if (this.extensions == null) {
			this.extensions = new LinkedHashMap<>();
		}
		this.extensions = DataModelUtil.insertMapEntry(this.extensions, name, value, atIndex);
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	public Boolean asBoolean() {
		throw new ClassCastException();
	}

	@Override
	public boolean isSchema() {
		return true;
	}

	@Override
	public SynSchema asSchema() {
		return this;
	}

	@Override
	public Object unionValue() {
		return this;
	}

	@Override
	public boolean isSchemaList() {
		return false;
	}

	@Override
	public List<SynSchema> asSchemaList() {
		throw new ClassCastException();
	}

	@Override
	public void accept(Visitor visitor) {
		Syn2Visitor viz = (Syn2Visitor) visitor;
		viz.visitSchema(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn2SchemaImpl();
	}
}