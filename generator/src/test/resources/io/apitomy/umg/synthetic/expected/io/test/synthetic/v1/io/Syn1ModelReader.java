package io.test.synthetic.v1.io;

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
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import io.test.synthetic.v1.Syn1SchemaImpl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn1ModelReader implements ModelReader {

	public void readDocument(ObjectNode json, Syn1Document node) {
		{
			JsonNode _version = JsonUtil.getProperty(json, "version");
			if (JsonUtil.isString(_version)) {
				node.setVersion(JsonUtil.toString(_version));
				JsonUtil.removeProperty(json, "version");
			}
		}
		{
			JsonNode _info = JsonUtil.getProperty(json, "info");
			if (JsonUtil.isObject(_info)) {
				node.setInfo(node.createInfo());
				readInfo(JsonUtil.toObject(_info), (Syn1Info) node.getInfo());
				JsonUtil.removeProperty(json, "info");
			}
		}
		{
			JsonNode _items = JsonUtil.getProperty(json, "items");
			if (JsonUtil.isArray(_items) && JsonUtil.allMatch(_items, JsonUtil.JsonType.OBJECT)) {
				List<JsonNode> _nodes = JsonUtil.toList(_items);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					ObjectNode object = JsonUtil.toObject(_nodes.get(_i));
					Syn1Item model = (Syn1Item) node.createItem();
					node.addItem(model);
					this.readItem(object, model);
				}
				JsonUtil.removeProperty(json, "items");
			}
		}
		{
			JsonNode _tags = JsonUtil.getProperty(json, "tags");
			if (JsonUtil.isArray(_tags) && JsonUtil.allMatch(_tags, JsonUtil.JsonType.STRING)) {
				List<String> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_tags);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toString(_nodes.get(_i)));
				}
				node.setTags(items);
				JsonUtil.removeProperty(json, "tags");
			}
		}
		{
			JsonNode _metadata = JsonUtil.getProperty(json, "metadata");
			if (JsonUtil.isObject(_metadata)
					&& JsonUtil.allValuesMatch(JsonUtil.toObject(_metadata), JsonUtil.JsonType.STRING)) {
				Map<String, String> items = new LinkedHashMap<>();
				List<String> _keys = JsonUtil.keys(JsonUtil.toObject(_metadata));
				for (int _i = 0; _i < _keys.size(); _i++) {
					String _key = _keys.get(_i);
					items.put(_key, JsonUtil.toString(JsonUtil.getProperty(JsonUtil.toObject(_metadata), _key)));
				}
				node.setMetadata(items);
				JsonUtil.removeProperty(json, "metadata");
			}
		}
		{
			JsonNode _additionalSchema = JsonUtil.getProperty(json, "additionalSchema");
			if (JsonUtil.isJsonNode(_additionalSchema)) {
				node.setAdditionalSchema(this.readSchemaOrBoolean(_additionalSchema, null));
				JsonUtil.removeProperty(json, "additionalSchema");
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

	public void readInfo(ObjectNode json, Syn1Info node) {
		{
			JsonNode _name = JsonUtil.getProperty(json, "name");
			if (JsonUtil.isString(_name)) {
				node.setName(JsonUtil.toString(_name));
				JsonUtil.removeProperty(json, "name");
			}
		}
		{
			JsonNode _contact = JsonUtil.getProperty(json, "contact");
			if (JsonUtil.isObject(_contact)) {
				node.setContact(node.createContact());
				readContact(JsonUtil.toObject(_contact), (Syn1Contact) node.getContact());
				JsonUtil.removeProperty(json, "contact");
			}
		}
		{
			JsonNode _version = JsonUtil.getProperty(json, "version");
			if (JsonUtil.isString(_version)) {
				node.setVersion(JsonUtil.toString(_version));
				JsonUtil.removeProperty(json, "version");
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

	public void readContact(ObjectNode json, Syn1Contact node) {
		{
			JsonNode _name = JsonUtil.getProperty(json, "name");
			if (JsonUtil.isString(_name)) {
				node.setName(JsonUtil.toString(_name));
				JsonUtil.removeProperty(json, "name");
			}
		}
		{
			JsonNode _email = JsonUtil.getProperty(json, "email");
			if (JsonUtil.isString(_email)) {
				node.setEmail(JsonUtil.toString(_email));
				JsonUtil.removeProperty(json, "email");
			}
		}
		{
			JsonNode _url = JsonUtil.getProperty(json, "url");
			if (JsonUtil.isString(_url)) {
				node.setUrl(JsonUtil.toString(_url));
				JsonUtil.removeProperty(json, "url");
			}
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readItem(ObjectNode json, Syn1Item node) {
		{
			JsonNode __ref = JsonUtil.getProperty(json, "$ref");
			if (JsonUtil.isString(__ref)) {
				node.set$ref(JsonUtil.toString(__ref));
				JsonUtil.removeProperty(json, "$ref");
			}
		}
		{
			JsonNode _description = JsonUtil.getProperty(json, "description");
			if (JsonUtil.isString(_description)) {
				node.setDescription(JsonUtil.toString(_description));
				JsonUtil.removeProperty(json, "description");
			}
		}
		{
			JsonNode _required = JsonUtil.getProperty(json, "required");
			if (JsonUtil.isBoolean(_required)) {
				node.setRequired(JsonUtil.toBoolean(_required));
				JsonUtil.removeProperty(json, "required");
			}
		}
		{
			JsonNode _order = JsonUtil.getProperty(json, "order");
			if (JsonUtil.isNumber(_order)) {
				node.setOrder(JsonUtil.toInteger(_order));
				JsonUtil.removeProperty(json, "order");
			}
		}
		{
			JsonNode _weight = JsonUtil.getProperty(json, "weight");
			if (JsonUtil.isNumber(_weight)) {
				node.setWeight(JsonUtil.toNumber(_weight));
				JsonUtil.removeProperty(json, "weight");
			}
		}
		{
			JsonNode _extra = JsonUtil.getProperty(json, "extra");
			if (JsonUtil.isJsonNode(_extra)) {
				node.setExtra(JsonUtil.toJsonNode(_extra));
				JsonUtil.removeProperty(json, "extra");
			}
		}
		{
			JsonNode _raw = JsonUtil.getProperty(json, "raw");
			if (JsonUtil.isObject(_raw)) {
				node.setRaw(JsonUtil.toObject(_raw));
				JsonUtil.removeProperty(json, "raw");
			}
		}
		{
			JsonNode _schema = JsonUtil.getProperty(json, "schema");
			if (JsonUtil.isObject(_schema)) {
				node.setSchema(node.createSchema());
				readSchema(JsonUtil.toObject(_schema), (Syn1Schema) node.getSchema());
				JsonUtil.removeProperty(json, "schema");
			}
		}
		{
			JsonNode _examples = JsonUtil.getProperty(json, "examples");
			if (JsonUtil.isArray(_examples) && JsonUtil.allMatch(_examples, JsonUtil.JsonType.ANY)) {
				List<JsonNode> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_examples);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toJsonNode(_nodes.get(_i)));
				}
				node.setExamples(items);
				JsonUtil.removeProperty(json, "examples");
			}
		}
		{
			JsonNode _defaultValue = JsonUtil.getProperty(json, "defaultValue");
			if (JsonUtil.isJsonNode(_defaultValue)) {
				node.setDefaultValue(this.readBooleanSchemaUnion(_defaultValue, null));
				JsonUtil.removeProperty(json, "defaultValue");
			}
		}
		{
			JsonNode _title = JsonUtil.getProperty(json, "title");
			if (JsonUtil.isString(_title)) {
				node.setTitle(JsonUtil.toString(_title));
				JsonUtil.removeProperty(json, "title");
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

	public void readSchema(ObjectNode json, Syn1Schema node) {
		{
			JsonNode __ref = JsonUtil.getProperty(json, "$ref");
			if (JsonUtil.isString(__ref)) {
				node.set$ref(JsonUtil.toString(__ref));
				JsonUtil.removeProperty(json, "$ref");
			}
		}
		{
			JsonNode _type = JsonUtil.getProperty(json, "type");
			if (JsonUtil.isString(_type)) {
				node.setType(JsonUtil.toString(_type));
				JsonUtil.removeProperty(json, "type");
			}
		}
		{
			JsonNode _items = JsonUtil.getProperty(json, "items");
			if (JsonUtil.isJsonNode(_items)) {
				node.setItems(this.readBooleanSchemaSchemaListUnion(_items, null));
				JsonUtil.removeProperty(json, "items");
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
				JsonUtil.removeProperty(json, "properties");
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
					JsonUtil.removeProperty(json, "allOf");
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
				JsonUtil.removeProperty(json, "definitions");
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
				JsonUtil.removeProperty(json, "nestedSchemas");
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
					JsonUtil.removeProperty(json, "composedSchemas");
				}
			}
		}
		{
			JsonNode _minLength = JsonUtil.getProperty(json, "minLength");
			if (JsonUtil.isNumber(_minLength)) {
				node.setMinLength(JsonUtil.toInteger(_minLength));
				JsonUtil.removeProperty(json, "minLength");
			}
		}
		{
			JsonNode _maxLength = JsonUtil.getProperty(json, "maxLength");
			if (JsonUtil.isNumber(_maxLength)) {
				node.setMaxLength(JsonUtil.toInteger(_maxLength));
				JsonUtil.removeProperty(json, "maxLength");
			}
		}
		{
			JsonNode _enum = JsonUtil.getProperty(json, "enum");
			if (JsonUtil.isArray(_enum) && JsonUtil.allMatch(_enum, JsonUtil.JsonType.ANY)) {
				List<JsonNode> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_enum);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toJsonNode(_nodes.get(_i)));
				}
				node.setEnum(items);
				JsonUtil.removeProperty(json, "enum");
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

	public void readPaths(ObjectNode json, Syn1Paths node) {
		{
			List<String> propertyNames = JsonUtil.keys(json);
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String name = propertyNames.get(_i);
				JsonNode _val = JsonUtil.getProperty(json, name);
				if (JsonUtil.isObject(_val)) {
					Syn1PathItem model = (Syn1PathItem) node.createPathItem();
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

	public void readPathItem(ObjectNode json, Syn1PathItem node) {
		{
			JsonNode __ref = JsonUtil.getProperty(json, "$ref");
			if (JsonUtil.isString(__ref)) {
				node.set$ref(JsonUtil.toString(__ref));
				JsonUtil.removeProperty(json, "$ref");
			}
		}
		{
			JsonNode _summary = JsonUtil.getProperty(json, "summary");
			if (JsonUtil.isString(_summary)) {
				node.setSummary(JsonUtil.toString(_summary));
				JsonUtil.removeProperty(json, "summary");
			}
		}
		{
			JsonNode _get = JsonUtil.getProperty(json, "get");
			if (JsonUtil.isObject(_get)) {
				node.setGet(node.createOperation());
				readOperation(JsonUtil.toObject(_get), (Syn1Operation) node.getGet());
				JsonUtil.removeProperty(json, "get");
			}
		}
		{
			JsonNode _put = JsonUtil.getProperty(json, "put");
			if (JsonUtil.isObject(_put)) {
				node.setPut(node.createOperation());
				readOperation(JsonUtil.toObject(_put), (Syn1Operation) node.getPut());
				JsonUtil.removeProperty(json, "put");
			}
		}
		{
			JsonNode _post = JsonUtil.getProperty(json, "post");
			if (JsonUtil.isObject(_post)) {
				node.setPost(node.createOperation());
				readOperation(JsonUtil.toObject(_post), (Syn1Operation) node.getPost());
				JsonUtil.removeProperty(json, "post");
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

	public void readOperation(ObjectNode json, Syn1Operation node) {
		{
			JsonNode _operationId = JsonUtil.getProperty(json, "operationId");
			if (JsonUtil.isString(_operationId)) {
				node.setOperationId(JsonUtil.toString(_operationId));
				JsonUtil.removeProperty(json, "operationId");
			}
		}
		{
			JsonNode _summary = JsonUtil.getProperty(json, "summary");
			if (JsonUtil.isString(_summary)) {
				node.setSummary(JsonUtil.toString(_summary));
				JsonUtil.removeProperty(json, "summary");
			}
		}
		{
			JsonNode _tags = JsonUtil.getProperty(json, "tags");
			if (JsonUtil.isArray(_tags) && JsonUtil.allMatch(_tags, JsonUtil.JsonType.STRING)) {
				List<String> items = new ArrayList<>();
				List<JsonNode> _nodes = JsonUtil.toList(_tags);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					items.add(JsonUtil.toString(_nodes.get(_i)));
				}
				node.setTags(items);
				JsonUtil.removeProperty(json, "tags");
			}
		}
		{
			JsonNode _parameters = JsonUtil.getProperty(json, "parameters");
			if (JsonUtil.isArray(_parameters) && JsonUtil.allMatch(_parameters, JsonUtil.JsonType.OBJECT)) {
				List<JsonNode> _nodes = JsonUtil.toList(_parameters);
				for (int _i = 0; _i < _nodes.size(); _i++) {
					ObjectNode object = JsonUtil.toObject(_nodes.get(_i));
					Syn1Item model = (Syn1Item) node.createItem();
					node.addParameter(model);
					this.readItem(object, model);
				}
				JsonUtil.removeProperty(json, "parameters");
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
		if (JsonUtil.isObjectWithProperty(json, "type")) {
			Syn1Schema node = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json), modelType);
		}
		return null;
	}

	private BooleanSchemaSchemaListUnion readBooleanSchemaSchemaListUnion(JsonNode json, ModelType modelType) {
		if (JsonUtil.isObjectWithProperty(json, "type")) {
			Syn1Schema node = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isArray(json)) {
			List<JsonNode> array = JsonUtil.toList(json);
			List<Syn1Schema> models = new ArrayList<>();
			for (int _idx = 0; _idx < array.size(); _idx++) {
				ObjectNode object = JsonUtil.toObject(array.get(_idx));
				Syn1Schema model = new Syn1SchemaImpl();
				this.readSchema(object, model);
				models.add(model);
			}
			@SuppressWarnings({"unchecked", "rawtypes"})
			SchemaListUnionValueImpl unionValue = new SchemaListUnionValueImpl((List) models);
			return unionValue;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json), modelType);
		}
		return null;
	}

	private BooleanSchemaUnion readBooleanSchemaUnion(JsonNode json, ModelType modelType) {
		if (JsonUtil.isObjectWithProperty(json, "type")) {
			Syn1Schema node = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json), modelType);
		}
		return null;
	}

	@Override
	public RootCapable readRoot(JsonNode json) {
		return this.readSchemaOrBoolean(json, ModelType.SYN1);
	}
}