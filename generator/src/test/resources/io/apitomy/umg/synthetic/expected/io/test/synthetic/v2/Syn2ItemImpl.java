package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynSchema;
import io.test.synthetic.union.BooleanSchemaUnion;
import io.test.synthetic.union.UnionValue;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v2.visitors.Syn2Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn2ItemImpl extends NodeImpl implements Syn2Item {

	private String $ref;
	private String description;
	private Boolean required;
	private Integer order;
	private Number weight;
	private JsonNode extra;
	private ObjectNode raw;
	private SynSchema schema;
	private List<JsonNode> examples;
	private BooleanSchemaUnion defaultValue;
	private String title;
	private Boolean deprecated;
	private Map<String, JsonNode> extensions;

	@Override
	public String get$ref() {
		return $ref;
	}

	@Override
	public void set$ref(String value) {
		this.$ref = value;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String value) {
		this.description = value;
	}

	@Override
	public Boolean isRequired() {
		return required;
	}

	@Override
	public void setRequired(Boolean value) {
		this.required = value;
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	@Override
	public void setOrder(Integer value) {
		this.order = value;
	}

	@Override
	public Number getWeight() {
		return weight;
	}

	@Override
	public void setWeight(Number value) {
		this.weight = value;
	}

	@Override
	public JsonNode getExtra() {
		return extra;
	}

	@Override
	public void setExtra(JsonNode value) {
		this.extra = value;
	}

	@Override
	public ObjectNode getRaw() {
		return raw;
	}

	@Override
	public void setRaw(ObjectNode value) {
		this.raw = value;
	}

	@Override
	public SynSchema getSchema() {
		return schema;
	}

	@Override
	public void setSchema(SynSchema value) {
		this.schema = value;
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("schema");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public Syn2Schema createSchema() {
		Syn2SchemaImpl node = new Syn2SchemaImpl();
		node.setParent(this);
		return node;
	}

	@Override
	public List<JsonNode> getExamples() {
		return examples;
	}

	@Override
	public void setExamples(List<JsonNode> value) {
		this.examples = value;
	}

	@Override
	public BooleanSchemaUnion getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(BooleanSchemaUnion value) {
		this.defaultValue = value;
		if (value != null) {
			if (value.isEntity()) {
				((NodeImpl) value)._setParentPropertyName("defaultValue");
				((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
			} else if (value.isEntityList()) {
				List<?> entityList = (List<?>) ((UnionValue<?>) value).getValue();
				for (Object entity : entityList) {
					if (entity != null) {
						((NodeImpl) entity)._setParentPropertyName("defaultValue");
						((NodeImpl) entity)._setParentPropertyType(ParentPropertyType.array);
					}
				}
			} else if (value.isEntityMap()) {
				Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) value).getValue();
				Collection<String> keys = entityMap.keySet();
				for (String key : keys) {
					NodeImpl entity = (NodeImpl) entityMap.get(key);
					if (entity != null) {
						entity._setParentPropertyName("defaultValue");
						entity._setParentPropertyType(ParentPropertyType.map);
						entity._setMapPropertyName(key);
					}
				}
			}
		}
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String value) {
		this.title = value;
	}

	@Override
	public Boolean isDeprecated() {
		return deprecated;
	}

	@Override
	public void setDeprecated(Boolean value) {
		this.deprecated = value;
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
	public void accept(Visitor visitor) {
		Syn2Visitor viz = (Syn2Visitor) visitor;
		viz.visitItem(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn2ItemImpl();
	}
}