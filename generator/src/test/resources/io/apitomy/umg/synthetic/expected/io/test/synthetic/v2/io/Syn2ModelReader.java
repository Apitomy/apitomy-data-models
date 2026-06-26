package io.test.synthetic.v2.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.ModelType;
import io.test.synthetic.RootCapable;
import io.test.synthetic.SchemaListUnionValueImpl;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.io.ModelReader;
import io.test.synthetic.union.BooleanUnionValueImpl;
import io.test.synthetic.util.JsonUtil;
import io.test.synthetic.util.ReaderUtil;
import io.test.synthetic.v2.Syn2Contact;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import io.test.synthetic.v2.Syn2SchemaImpl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn2ModelReader implements ModelReader {

	public void readDocument(ObjectNode json, Syn2Document node) {
		{
			JsonNode _version = JsonUtil.getProperty(json, "version");
			if (JsonUtil.isString(_version)) {
				node.setVersion(JsonUtil.toString(_version));
				json.remove("version");
			}
		}
		{
			JsonNode _info = JsonUtil.getProperty(json, "info");
			if (JsonUtil.isObject(_info)) {
				node.setInfo(node.createInfo());
				readInfo(JsonUtil.toObject(_info), (Syn2Info) node.getInfo());
				json.remove("info");
			}
		}
		{
			JsonNode _items = JsonUtil.getProperty(json, "items");
			if (JsonUtil.isArray(_items) && JsonUtil.allMatch(_items, "object")) {
				List<JsonNode> _nodes = JsonUtil.toList(_items);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					ObjectNode object = JsonUtil.toObject(_nodes.get(_i));
					Syn2Item model = (Syn2Item) node.createItem();
					node.addItem(model);
					this.readItem(object, model);
				}
				json.remove("items");
			}
		}
		{
			JsonNode _tags = JsonUtil.getProperty(json, "tags");
			if (JsonUtil.isArray(_tags) && JsonUtil.allMatch(_tags, "string")) {
				List<String> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_tags);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toString(_nodes.get(_i)));
				}
				node.setTags(items);
				json.remove("tags");
			}
		}
		{
			JsonNode _metadata = JsonUtil.getProperty(json, "metadata");
			if (JsonUtil.isObject(_metadata) && JsonUtil.allValuesMatch(JsonUtil.toObject(_metadata), "string")) {
				Map<String, String> items = new LinkedHashMap<>();
				List<String> _keys = JsonUtil.keys(JsonUtil.toObject(_metadata));
				for (int _i = 0; _i < _keys.size(); _i++) {
					String _key = _keys.get(_i);
					items.put(_key, JsonUtil.toString(JsonUtil.getProperty(JsonUtil.toObject(_metadata), _key)));
				}
				node.setMetadata(items);
				json.remove("metadata");
			}
		}
		{
			JsonNode _webhooks = JsonUtil.getProperty(json, "webhooks");
			if (JsonUtil.isObject(_webhooks)) {
				ObjectNode _obj = JsonUtil.toObject(_webhooks);
				List<String> _keys = JsonUtil.keys(_obj);
				for (int _i = 0; _i < _keys.size(); _i++) {
					String _key = _keys.get(_i);
					JsonNode _val = JsonUtil.getProperty(_obj, _key);
					if (JsonUtil.isObject(_val)) {
						Syn2PathItem model = (Syn2PathItem) node.createPathItem();
						node.addWebhook(_key, model);
						this.readPathItem(JsonUtil.toObject(_val), model);
					}
				}
				json.remove("webhooks");
			}
		}
		{
			JsonNode _additionalSchema = JsonUtil.getProperty(json, "additionalSchema");
			if (JsonUtil.isJsonNode(_additionalSchema)) {
				node.setAdditionalSchema(this.readSchemaOrBoolean(_additionalSchema, null));
				json.remove("additionalSchema");
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readInfo(ObjectNode json, Syn2Info node) {
		{
			JsonNode _name = JsonUtil.getProperty(json, "name");
			if (JsonUtil.isString(_name)) {
				node.setName(JsonUtil.toString(_name));
				json.remove("name");
			}
		}
		{
			JsonNode _contact = JsonUtil.getProperty(json, "contact");
			if (JsonUtil.isObject(_contact)) {
				node.setContact(node.createContact());
				readContact(JsonUtil.toObject(_contact), (Syn2Contact) node.getContact());
				json.remove("contact");
			}
		}
		{
			JsonNode _version = JsonUtil.getProperty(json, "version");
			if (JsonUtil.isString(_version)) {
				node.setVersion(JsonUtil.toString(_version));
				json.remove("version");
			}
		}
		{
			JsonNode _license = JsonUtil.getProperty(json, "license");
			if (JsonUtil.isString(_license)) {
				node.setLicense(JsonUtil.toString(_license));
				json.remove("license");
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readContact(ObjectNode json, Syn2Contact node) {
		{
			JsonNode _name = JsonUtil.getProperty(json, "name");
			if (JsonUtil.isString(_name)) {
				node.setName(JsonUtil.toString(_name));
				json.remove("name");
			}
		}
		{
			JsonNode _email = JsonUtil.getProperty(json, "email");
			if (JsonUtil.isString(_email)) {
				node.setEmail(JsonUtil.toString(_email));
				json.remove("email");
			}
		}
		{
			JsonNode _url = JsonUtil.getProperty(json, "url");
			if (JsonUtil.isString(_url)) {
				node.setUrl(JsonUtil.toString(_url));
				json.remove("url");
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readItem(ObjectNode json, Syn2Item node) {
		{
			JsonNode __ref = JsonUtil.getProperty(json, "$ref");
			if (JsonUtil.isString(__ref)) {
				node.set$ref(JsonUtil.toString(__ref));
				json.remove("$ref");
			}
		}
		{
			JsonNode _description = JsonUtil.getProperty(json, "description");
			if (JsonUtil.isString(_description)) {
				node.setDescription(JsonUtil.toString(_description));
				json.remove("description");
			}
		}
		{
			JsonNode _required = JsonUtil.getProperty(json, "required");
			if (JsonUtil.isBoolean(_required)) {
				node.setRequired(JsonUtil.toBoolean(_required));
				json.remove("required");
			}
		}
		{
			JsonNode _order = JsonUtil.getProperty(json, "order");
			if (JsonUtil.isNumber(_order)) {
				node.setOrder(JsonUtil.toInteger(_order));
				json.remove("order");
			}
		}
		{
			JsonNode _weight = JsonUtil.getProperty(json, "weight");
			if (JsonUtil.isNumber(_weight)) {
				node.setWeight(JsonUtil.toNumber(_weight));
				json.remove("weight");
			}
		}
		{
			JsonNode _extra = JsonUtil.getProperty(json, "extra");
			if (JsonUtil.isJsonNode(_extra)) {
				node.setExtra(JsonUtil.toJsonNode(_extra));
				json.remove("extra");
			}
		}
		{
			JsonNode _raw = JsonUtil.getProperty(json, "raw");
			if (JsonUtil.isObject(_raw)) {
				node.setRaw(JsonUtil.toObject(_raw));
				json.remove("raw");
			}
		}
		{
			JsonNode _schema = JsonUtil.getProperty(json, "schema");
			if (JsonUtil.isObject(_schema)) {
				node.setSchema(node.createSchema());
				readSchema(JsonUtil.toObject(_schema), (Syn2Schema) node.getSchema());
				json.remove("schema");
			}
		}
		{
			JsonNode _examples = JsonUtil.getProperty(json, "examples");
			if (JsonUtil.isArray(_examples) && JsonUtil.allMatch(_examples, "any")) {
				List<JsonNode> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_examples);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toJsonNode(_nodes.get(_i)));
				}
				node.setExamples(items);
				json.remove("examples");
			}
		}
		{
			JsonNode _defaultValue = JsonUtil.getProperty(json, "defaultValue");
			if (JsonUtil.isJsonNode(_defaultValue)) {
				node.setDefaultValue(this.readBooleanSchemaUnion(_defaultValue, null));
				json.remove("defaultValue");
			}
		}
		{
			JsonNode _title = JsonUtil.getProperty(json, "title");
			if (JsonUtil.isString(_title)) {
				node.setTitle(JsonUtil.toString(_title));
				json.remove("title");
			}
		}
		{
			JsonNode _deprecated = JsonUtil.getProperty(json, "deprecated");
			if (JsonUtil.isBoolean(_deprecated)) {
				node.setDeprecated(JsonUtil.toBoolean(_deprecated));
				json.remove("deprecated");
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readSchema(ObjectNode json, Syn2Schema node) {
		{
			JsonNode __ref = JsonUtil.getProperty(json, "$ref");
			if (JsonUtil.isString(__ref)) {
				node.set$ref(JsonUtil.toString(__ref));
				json.remove("$ref");
			}
		}
		{
			JsonNode _type = JsonUtil.getProperty(json, "type");
			if (JsonUtil.isString(_type)) {
				node.setType(JsonUtil.toString(_type));
				json.remove("type");
			}
		}
		{
			JsonNode _items = JsonUtil.getProperty(json, "items");
			if (JsonUtil.isJsonNode(_items)) {
				node.setItems(this.readBooleanSchemaSchemaListUnion(_items, null));
				json.remove("items");
			}
		}
		{
			JsonNode _properties = JsonUtil.getProperty(json, "properties");
			if (JsonUtil.isObject(_properties)) {
				ObjectNode _obj = JsonUtil.toObject(_properties);
				List<String> _keys = JsonUtil.keys(_obj);
				for (int _i = 0; _i < _keys.size(); _i++) {
					String _key = _keys.get(_i);
					JsonNode _val = JsonUtil.getProperty(_obj, _key);
					if (JsonUtil.isJsonNode(_val)) {
						BooleanSchemaUnion model = this.readBooleanSchemaUnion(_val, null);
						if (model != null)
							node.addProperty(_key, model);
					}
				}
				json.remove("properties");
			}
		}
		{
			JsonNode _allOf = JsonUtil.getProperty(json, "allOf");
			if (JsonUtil.isArray(_allOf)) {
				List<JsonNode> _nodes = JsonUtil.toList(_allOf);
				List<BooleanSchemaUnion> _items = new ArrayList<>();
				boolean _valid = true;
				for (int _i = 0; _i < _nodes.size(); _i++) {
					BooleanSchemaUnion _result = this.readBooleanSchemaUnion(_nodes.get(_i), null);
					if (_result == null) {
						_valid = false;
						break;
					}
					_items.add(_result);
				}
				if (_valid) {
					for (int _i = 0; _i < _items.size(); _i++) {
						node.addAllOf(_items.get(_i));
					}
					json.remove("allOf");
				}
			}
		}
		{
			JsonNode _definitions = JsonUtil.getProperty(json, "definitions");
			if (JsonUtil.isObject(_definitions)) {
				ObjectNode _obj = JsonUtil.toObject(_definitions);
				List<String> _keys = JsonUtil.keys(_obj);
				for (int _i = 0; _i < _keys.size(); _i++) {
					String _key = _keys.get(_i);
					JsonNode _val = JsonUtil.getProperty(_obj, _key);
					if (JsonUtil.isJsonNode(_val)) {
						BooleanSchemaUnion model = this.readBooleanSchemaUnion(_val, null);
						if (model != null)
							node.addDefinition(_key, model);
					}
				}
				json.remove("definitions");
			}
		}
		{
			JsonNode _nestedSchemas = JsonUtil.getProperty(json, "nestedSchemas");
			if (JsonUtil.isObject(_nestedSchemas)) {
				ObjectNode _obj = JsonUtil.toObject(_nestedSchemas);
				List<String> _keys = JsonUtil.keys(_obj);
				for (int _i = 0; _i < _keys.size(); _i++) {
					String _key = _keys.get(_i);
					JsonNode _val = JsonUtil.getProperty(_obj, _key);
					if (JsonUtil.isJsonNode(_val)) {
						SchemaOrBoolean model = this.readSchemaOrBoolean(_val, null);
						if (model != null)
							node.addNestedSchema(_key, model);
					}
				}
				json.remove("nestedSchemas");
			}
		}
		{
			JsonNode _composedSchemas = JsonUtil.getProperty(json, "composedSchemas");
			if (JsonUtil.isArray(_composedSchemas)) {
				List<JsonNode> _nodes = JsonUtil.toList(_composedSchemas);
				List<SchemaOrBoolean> _items = new ArrayList<>();
				boolean _valid = true;
				for (int _i = 0; _i < _nodes.size(); _i++) {
					SchemaOrBoolean _result = this.readSchemaOrBoolean(_nodes.get(_i), null);
					if (_result == null) {
						_valid = false;
						break;
					}
					_items.add(_result);
				}
				if (_valid) {
					for (int _i = 0; _i < _items.size(); _i++) {
						node.addComposedSchema(_items.get(_i));
					}
					json.remove("composedSchemas");
				}
			}
		}
		{
			JsonNode _minLength = JsonUtil.getProperty(json, "minLength");
			if (JsonUtil.isNumber(_minLength)) {
				node.setMinLength(JsonUtil.toInteger(_minLength));
				json.remove("minLength");
			}
		}
		{
			JsonNode _maxLength = JsonUtil.getProperty(json, "maxLength");
			if (JsonUtil.isNumber(_maxLength)) {
				node.setMaxLength(JsonUtil.toInteger(_maxLength));
				json.remove("maxLength");
			}
		}
		{
			JsonNode _enum = JsonUtil.getProperty(json, "enum");
			if (JsonUtil.isArray(_enum) && JsonUtil.allMatch(_enum, "any")) {
				List<JsonNode> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_enum);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toJsonNode(_nodes.get(_i)));
				}
				node.setEnum(items);
				json.remove("enum");
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readPaths(ObjectNode json, Syn2Paths node) {
		{
			List<String> propertyNames = JsonUtil.keys(json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isObject(_val)) {
					Syn2PathItem model = (Syn2PathItem) node.createPathItem();
					node.addItem(name, model);
					this.readPathItem((ObjectNode) _val, model);
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readPathItem(ObjectNode json, Syn2PathItem node) {
		{
			JsonNode __ref = JsonUtil.getProperty(json, "$ref");
			if (JsonUtil.isString(__ref)) {
				node.set$ref(JsonUtil.toString(__ref));
				json.remove("$ref");
			}
		}
		{
			JsonNode _summary = JsonUtil.getProperty(json, "summary");
			if (JsonUtil.isString(_summary)) {
				node.setSummary(JsonUtil.toString(_summary));
				json.remove("summary");
			}
		}
		{
			JsonNode _get = JsonUtil.getProperty(json, "get");
			if (JsonUtil.isObject(_get)) {
				node.setGet(node.createOperation());
				readOperation(JsonUtil.toObject(_get), (Syn2Operation) node.getGet());
				json.remove("get");
			}
		}
		{
			JsonNode _put = JsonUtil.getProperty(json, "put");
			if (JsonUtil.isObject(_put)) {
				node.setPut(node.createOperation());
				readOperation(JsonUtil.toObject(_put), (Syn2Operation) node.getPut());
				json.remove("put");
			}
		}
		{
			JsonNode _post = JsonUtil.getProperty(json, "post");
			if (JsonUtil.isObject(_post)) {
				node.setPost(node.createOperation());
				readOperation(JsonUtil.toObject(_post), (Syn2Operation) node.getPost());
				json.remove("post");
			}
		}
		{
			JsonNode _delete = JsonUtil.getProperty(json, "delete");
			if (JsonUtil.isObject(_delete)) {
				node.setDelete(node.createOperation());
				readOperation(JsonUtil.toObject(_delete), (Syn2Operation) node.getDelete());
				json.remove("delete");
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readOperation(ObjectNode json, Syn2Operation node) {
		{
			JsonNode _operationId = JsonUtil.getProperty(json, "operationId");
			if (JsonUtil.isString(_operationId)) {
				node.setOperationId(JsonUtil.toString(_operationId));
				json.remove("operationId");
			}
		}
		{
			JsonNode _summary = JsonUtil.getProperty(json, "summary");
			if (JsonUtil.isString(_summary)) {
				node.setSummary(JsonUtil.toString(_summary));
				json.remove("summary");
			}
		}
		{
			JsonNode _tags = JsonUtil.getProperty(json, "tags");
			if (JsonUtil.isArray(_tags) && JsonUtil.allMatch(_tags, "string")) {
				List<String> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_tags);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toString(_nodes.get(_i)));
				}
				node.setTags(items);
				json.remove("tags");
			}
		}
		{
			JsonNode _parameters = JsonUtil.getProperty(json, "parameters");
			if (JsonUtil.isArray(_parameters) && JsonUtil.allMatch(_parameters, "object")) {
				List<JsonNode> _nodes = JsonUtil.toList(_parameters);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					ObjectNode object = JsonUtil.toObject(_nodes.get(_i));
					Syn2Item model = (Syn2Item) node.createItem();
					node.addParameter(model);
					this.readItem(object, model);
				}
				json.remove("parameters");
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isJsonNode(_val)) {
					node.addExtension(name, JsonUtil.toJsonNode(_val));
					JsonUtil.removeProperty(json, name);
				}
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	private SchemaOrBoolean readSchemaOrBoolean(JsonNode json, ModelType modelType) {
		if (json == null)
			return null;
		if (JsonUtil.isObjectWithProperty(json, "type")) {
			Syn2Schema node = new Syn2SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json), modelType);
		}
		return null;
	}

	private BooleanSchemaSchemaListUnion readBooleanSchemaSchemaListUnion(JsonNode json, ModelType modelType) {
		if (json == null)
			return null;
		if (JsonUtil.isObjectWithProperty(json, "type")) {
			Syn2Schema node = new Syn2SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isArray(json)) {
			List<JsonNode> array = JsonUtil.toList(json);
			List<Syn2Schema> models = new ArrayList<>();
			array.forEach(item -> {
				ObjectNode object = JsonUtil.toObject(item);
				Syn2Schema model = new Syn2SchemaImpl();
				this.readSchema(object, model);
				models.add(model);
			});
			@SuppressWarnings({"unchecked", "rawtypes"})
			SchemaListUnionValueImpl unionValue = new SchemaListUnionValueImpl((List) models);
			return unionValue;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json), modelType);
		}
		return null;
	}

	private BooleanSchemaUnion readBooleanSchemaUnion(JsonNode json, ModelType modelType) {
		if (json == null)
			return null;
		if (JsonUtil.isObjectWithProperty(json, "type")) {
			Syn2Schema node = new Syn2SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json), modelType);
		}
		return null;
	}

	@Override
	public RootCapable readRoot(JsonNode json) {
		return (RootCapable) this.readSchemaOrBoolean(json, ModelType.SYN2);
	}
}