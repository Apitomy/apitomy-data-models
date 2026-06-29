package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.ModelType;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.RootCapableImpl;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.SynSchema;
import io.test.synthetic.union.UnionValue;
import io.test.synthetic.union.UnionValueImpl;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v2.visitors.Syn2Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.ArrayList;
import java.util.Collection;
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
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("items");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
			} else if (value.isEntityList()) {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("items");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);
				List<?> entityList = (List<?>) ((UnionValue<?>) value).getValue();
				for (Object entity : entityList) {
					if (entity != null) {
						((NodeImpl) entity)._setParent(this);
						((NodeImpl) entity)._setParentPropertyName("items");
						((NodeImpl) entity)._setParentPropertyType(ParentPropertyType.array);
					}
				}
			} else if (value.isEntityMap()) {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("items");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);
				Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) value).getValue();
				Collection<String> keys = entityMap.keySet();
				for (String key : keys) {
					NodeImpl entity = (NodeImpl) entityMap.get(key);
					if (entity != null) {
						entity._setParent(this);
						entity._setParentPropertyName("items");
						entity._setParentPropertyType(ParentPropertyType.map);
						entity._setMapPropertyName(key);
					}
				}
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("items");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);
			}
		}
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
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("properties");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
				((NodeImpl) value)._setMapPropertyName(name);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("properties");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.map);
				((UnionValueImpl<?>) value)._setMapPropertyName(name);
			}
		}
	}

	@Override
	public void clearProperties() {
		if (this.properties != null) {
			for (Object item : this.properties.values()) {
				if (item != null)
					((Node) item).detach();
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

	@Override
	public void insertProperty(String name, BooleanSchemaUnion value, int atIndex) {
		if (this.properties == null) {
			this.properties = new LinkedHashMap<>();
			this.properties.put(name, value);
		} else {
			this.properties = DataModelUtil.insertMapEntry(this.properties, name, value, atIndex);
		}
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("properties");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
				((NodeImpl) value)._setMapPropertyName(name);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("properties");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.map);
				((UnionValueImpl<?>) value)._setMapPropertyName(name);
			}
		}
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
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("allOf");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("allOf");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.array);
			}
		}
	}

	@Override
	public void clearAllOf() {
		if (this.allOf != null) {
			for (Object item : this.allOf) {
				if (item != null)
					((Node) item).detach();
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

	@Override
	public void insertAllOf(BooleanSchemaUnion value, int atIndex) {
		if (this.allOf == null) {
			this.allOf = new ArrayList<>();
			this.allOf.add(value);
		} else {
			this.allOf = DataModelUtil.insertListEntry(this.allOf, value, atIndex);
		}
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("allOf");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("allOf");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.array);
			}
		}
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
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("definitions");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
				((NodeImpl) value)._setMapPropertyName(name);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("definitions");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.map);
				((UnionValueImpl<?>) value)._setMapPropertyName(name);
			}
		}
	}

	@Override
	public void clearDefinitions() {
		if (this.definitions != null) {
			for (Object item : this.definitions.values()) {
				if (item != null)
					((Node) item).detach();
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

	@Override
	public void insertDefinition(String name, BooleanSchemaUnion value, int atIndex) {
		if (this.definitions == null) {
			this.definitions = new LinkedHashMap<>();
			this.definitions.put(name, value);
		} else {
			this.definitions = DataModelUtil.insertMapEntry(this.definitions, name, value, atIndex);
		}
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("definitions");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
				((NodeImpl) value)._setMapPropertyName(name);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("definitions");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.map);
				((UnionValueImpl<?>) value)._setMapPropertyName(name);
			}
		}
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
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("nestedSchemas");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
				((NodeImpl) value)._setMapPropertyName(name);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("nestedSchemas");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.map);
				((UnionValueImpl<?>) value)._setMapPropertyName(name);
			}
		}
	}

	@Override
	public void clearNestedSchemas() {
		if (this.nestedSchemas != null) {
			for (Object item : this.nestedSchemas.values()) {
				if (item != null)
					((Node) item).detach();
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

	@Override
	public void insertNestedSchema(String name, SchemaOrBoolean value, int atIndex) {
		if (this.nestedSchemas == null) {
			this.nestedSchemas = new LinkedHashMap<>();
			this.nestedSchemas.put(name, value);
		} else {
			this.nestedSchemas = DataModelUtil.insertMapEntry(this.nestedSchemas, name, value, atIndex);
		}
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("nestedSchemas");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
				((NodeImpl) value)._setMapPropertyName(name);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("nestedSchemas");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.map);
				((UnionValueImpl<?>) value)._setMapPropertyName(name);
			}
		}
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
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("composedSchemas");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("composedSchemas");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.array);
			}
		}
	}

	@Override
	public void clearComposedSchemas() {
		if (this.composedSchemas != null) {
			for (Object item : this.composedSchemas) {
				if (item != null)
					((Node) item).detach();
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

	@Override
	public void insertComposedSchema(SchemaOrBoolean value, int atIndex) {
		if (this.composedSchemas == null) {
			this.composedSchemas = new ArrayList<>();
			this.composedSchemas.add(value);
		} else {
			this.composedSchemas = DataModelUtil.insertListEntry(this.composedSchemas, value, atIndex);
		}
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParent(this);
				((NodeImpl) value)._setParentPropertyName("composedSchemas");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);
			} else {
				((UnionValueImpl<?>) value)._setParent(this);
				((UnionValueImpl<?>) value)._setParentPropertyName("composedSchemas");
				((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.array);
			}
		}
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

	@Override
	public void insertExtension(String name, JsonNode value, int atIndex) {
		if (this.extensions == null) {
			this.extensions = new LinkedHashMap<>();
			this.extensions.put(name, value);
		} else {
			this.extensions = DataModelUtil.insertMapEntry(this.extensions, name, value, atIndex);
		}
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