package io.test.synthetic.v1.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.ModelType;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.RootCapable;
import io.test.synthetic.io.ModelReader;
import io.test.synthetic.union.BooleanSchemaSchemaListUnion;
import io.test.synthetic.union.BooleanSchemaUnion;
import io.test.synthetic.union.BooleanUnionValue;
import io.test.synthetic.union.BooleanUnionValueImpl;
import io.test.synthetic.union.SchemaListUnionValue;
import io.test.synthetic.union.SchemaListUnionValueImpl;
import io.test.synthetic.union.SchemaOrBoolean;
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
import java.util.List;
import java.util.Map;

public class Syn1ModelReader implements ModelReader {

	public void readDocument(ObjectNode json, Syn1Document node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "version");
			node.setVersion(value);
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "info");
			if (object != null) {
				node.setInfo(node.createInfo());
				readInfo(object, (Syn1Info) node.getInfo());
			}
		}
		{
			List<ObjectNode> objects = JsonUtil.consumeObjectArrayProperty(json, "items");
			if (objects != null) {
				objects.forEach(object -> {
					Syn1Item model = (Syn1Item) node.createItem();
					node.addItem(model);
					this.readItem(object, model);
				});
			}
		}
		{
			List<String> value = JsonUtil.consumeStringArrayProperty(json, "tags");
			node.setTags(value);
		}
		{
			Map<String, String> value = JsonUtil.consumeStringMapProperty(json, "metadata");
			node.setMetadata(value);
		}
		{
			JsonNode value = JsonUtil.consumeAnyProperty(json, "additionalSchema");
			if (value != null) {
				node.setAdditionalSchema(this.readSchemaOrBoolean(value));
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readInfo(ObjectNode json, Syn1Info node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "name");
			node.setName(value);
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "contact");
			if (object != null) {
				node.setContact(node.createContact());
				readContact(object, (Syn1Contact) node.getContact());
			}
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "version");
			node.setVersion(value);
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readContact(ObjectNode json, Syn1Contact node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "name");
			node.setName(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "email");
			node.setEmail(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "url");
			node.setUrl(value);
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readItem(ObjectNode json, Syn1Item node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "$ref");
			node.set$ref(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "description");
			node.setDescription(value);
		}
		{
			Boolean value = JsonUtil.consumeBooleanProperty(json, "required");
			node.setRequired(value);
		}
		{
			Integer value = JsonUtil.consumeIntegerProperty(json, "order");
			node.setOrder(value);
		}
		{
			Number value = JsonUtil.consumeNumberProperty(json, "weight");
			node.setWeight(value);
		}
		{
			JsonNode value = JsonUtil.consumeAnyProperty(json, "extra");
			node.setExtra(value);
		}
		{
			ObjectNode value = JsonUtil.consumeObjectProperty(json, "raw");
			node.setRaw(value);
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "schema");
			if (object != null) {
				node.setSchema(node.createSchema());
				readSchema(object, (Syn1Schema) node.getSchema());
			}
		}
		{
			List<JsonNode> value = JsonUtil.consumeAnyArrayProperty(json, "examples");
			node.setExamples(value);
		}
		{
			JsonNode value = JsonUtil.consumeAnyProperty(json, "defaultValue");
			if (value != null) {
				if (JsonUtil.isObjectWithProperty(value, "type")) {
					ObjectNode object = JsonUtil.toObject(value);
					node.setDefaultValue(node.createSchema());
					readSchema(object, (Syn1Schema) node.getDefaultValue());
				} else if (JsonUtil.isBoolean(value)) {
					Boolean pValue = JsonUtil.toBoolean(value);
					BooleanUnionValue unionValue = new BooleanUnionValueImpl(pValue);
					node.setDefaultValue(unionValue);
				} else {
					node.addExtraProperty("defaultValue", value);
				}
			}
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "title");
			node.setTitle(value);
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readSchema(ObjectNode json, Syn1Schema node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "$ref");
			node.set$ref(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "type");
			node.setType(value);
		}
		{
			JsonNode value = JsonUtil.consumeAnyProperty(json, "items");
			if (value != null) {
				if (JsonUtil.isObjectWithProperty(value, "type")) {
					ObjectNode object = JsonUtil.toObject(value);
					node.setItems(node.createSchema());
					readSchema(object, (Syn1Schema) node.getItems());
				} else if (JsonUtil.isArray(value)) {
					List<JsonNode> array = JsonUtil.toList(value);
					List<Syn1Schema> models = new ArrayList<>();
					array.forEach(item -> {
						ObjectNode object = JsonUtil.toObject(item);
						Syn1Schema model = (Syn1Schema) node.createSchema();
						((NodeImpl) model)._setParent(node);
						this.readSchema(object, model);
						models.add(model);
					});
					@SuppressWarnings({"unchecked", "rawtypes"})
					SchemaListUnionValue unionValue = new SchemaListUnionValueImpl((List) models);
					node.setItems(unionValue);
				} else if (JsonUtil.isBoolean(value)) {
					Boolean pValue = JsonUtil.toBoolean(value);
					BooleanUnionValue unionValue = new BooleanUnionValueImpl(pValue);
					node.setItems(unionValue);
				} else {
					node.addExtraProperty("items", value);
				}
			}
		}
		{
			ObjectNode mapObject = JsonUtil.consumeObjectProperty(json, "properties");
			if (mapObject != null) {
				List<String> keys = JsonUtil.keys(mapObject);
				keys.forEach(key -> {
					JsonNode value = JsonUtil.consumeAnyProperty(mapObject, key);
					if (value != null) {
						if (JsonUtil.isObject(value)) {
							ObjectNode object = JsonUtil.toObject(value);
							Syn1Schema model = (Syn1Schema) node.createSchema();
							node.addProperty(key, model);
							readSchema(object, model);
						} else if (JsonUtil.isBoolean(value)) {
							Boolean pValue = JsonUtil.toBoolean(value);
							BooleanUnionValue unionValue = new BooleanUnionValueImpl(pValue);
							node.addProperty(key, unionValue);
						}
					}
				});
			}
		}
		{
			List<JsonNode> array = JsonUtil.consumeAnyArrayProperty(json, "allOf");
			if (array != null) {
				array.forEach(value -> {
					if (JsonUtil.isObject(value)) {
						ObjectNode object = JsonUtil.toObject(value);
						Syn1Schema model = (Syn1Schema) node.createSchema();
						node.addAllOf(model);
						readSchema(object, model);
					} else if (JsonUtil.isBoolean(value)) {
						Boolean pValue = JsonUtil.toBoolean(value);
						BooleanUnionValue unionValue = new BooleanUnionValueImpl(pValue);
						node.addAllOf(unionValue);
					}
				});
			}
		}
		{
			ObjectNode mapObject = JsonUtil.consumeObjectProperty(json, "definitions");
			if (mapObject != null) {
				List<String> keys = JsonUtil.keys(mapObject);
				keys.forEach(key -> {
					JsonNode value = JsonUtil.consumeAnyProperty(mapObject, key);
					if (value != null) {
						if (JsonUtil.isObject(value)) {
							ObjectNode object = JsonUtil.toObject(value);
							Syn1Schema model = (Syn1Schema) node.createSchema();
							node.addDefinition(key, model);
							readSchema(object, model);
						} else if (JsonUtil.isBoolean(value)) {
							Boolean pValue = JsonUtil.toBoolean(value);
							BooleanUnionValue unionValue = new BooleanUnionValueImpl(pValue);
							node.addDefinition(key, unionValue);
						}
					}
				});
			}
		}
		{
			ObjectNode mapObj = JsonUtil.consumeObjectProperty(json, "nestedSchemas");
			if (mapObj != null) {
				JsonUtil.keys(mapObj).forEach(key -> {
					JsonNode value = JsonUtil.consumeAnyProperty(mapObj, key);
					if (value != null) {
						SchemaOrBoolean model = this.readSchemaOrBoolean(value);
						if (model != null)
							node.addNestedSchema(key, model);
					}
				});
			}
		}
		{
			List<JsonNode> array = JsonUtil.consumeAnyArrayProperty(json, "composedSchemas");
			if (array != null) {
				array.forEach(item -> {
					SchemaOrBoolean value = this.readSchemaOrBoolean(item);
					if (value != null)
						node.addComposedSchema(value);
				});
			}
		}
		{
			Integer value = JsonUtil.consumeIntegerProperty(json, "minLength");
			node.setMinLength(value);
		}
		{
			Integer value = JsonUtil.consumeIntegerProperty(json, "maxLength");
			node.setMaxLength(value);
		}
		{
			List<JsonNode> value = JsonUtil.consumeAnyArrayProperty(json, "enum");
			node.setEnum(value);
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readPaths(ObjectNode json, Syn1Paths node) {
		{
			List<String> propertyNames = JsonUtil.keys(json);
			propertyNames.forEach(name -> {
				ObjectNode object = JsonUtil.consumeObjectProperty(json, name);
				if (object != null) {
					Syn1PathItem model = (Syn1PathItem) node.createPathItem();
					node.addItem(name, model);
					this.readPathItem(object, model);
				}
			});
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readPathItem(ObjectNode json, Syn1PathItem node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "$ref");
			node.set$ref(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "summary");
			node.setSummary(value);
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "get");
			if (object != null) {
				node.setGet(node.createOperation());
				readOperation(object, (Syn1Operation) node.getGet());
			}
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "put");
			if (object != null) {
				node.setPut(node.createOperation());
				readOperation(object, (Syn1Operation) node.getPut());
			}
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "post");
			if (object != null) {
				node.setPost(node.createOperation());
				readOperation(object, (Syn1Operation) node.getPost());
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	public void readOperation(ObjectNode json, Syn1Operation node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "operationId");
			node.setOperationId(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "summary");
			node.setSummary(value);
		}
		{
			List<String> value = JsonUtil.consumeStringArrayProperty(json, "tags");
			node.setTags(value);
		}
		{
			List<ObjectNode> objects = JsonUtil.consumeObjectArrayProperty(json, "parameters");
			if (objects != null) {
				objects.forEach(object -> {
					Syn1Item model = (Syn1Item) node.createItem();
					node.addParameter(model);
					this.readItem(object, model);
				});
			}
		}
		{
			List<String> propertyNames = JsonUtil.matchingKeys("^x-.+$", json);
			propertyNames.forEach(name -> {
				JsonNode value = JsonUtil.consumeAnyProperty(json, name);
				node.addExtension(name, value);
			});
		}
		ReaderUtil.readExtraProperties(json, node);
	}

	private SchemaOrBoolean readSchemaOrBoolean(JsonNode json) {
		if (json == null)
			return null;
		if (json.isObject() && json.has("type")) {
			Syn1Schema node = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json));
		}
		return null;
	}

	private BooleanSchemaSchemaListUnion readBooleanSchemaSchemaListUnion(JsonNode json) {
		if (json == null)
			return null;
		if (json.isObject()) {
			Syn1Schema node = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isArray(json)) {
			List<JsonNode> array = JsonUtil.toList(json);
			List<Syn1Schema> models = new ArrayList<>();
			array.forEach(item -> {
				ObjectNode object = JsonUtil.toObject(item);
				Syn1Schema model = new Syn1SchemaImpl();
				this.readSchema(object, model);
				models.add(model);
			});
			@SuppressWarnings({"unchecked", "rawtypes"})
			SchemaListUnionValueImpl unionValue = new SchemaListUnionValueImpl((List) models);
			return unionValue;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json));
		}
		return null;
	}

	private BooleanSchemaUnion readBooleanSchemaUnion(JsonNode json) {
		if (json == null)
			return null;
		if (json.isObject()) {
			Syn1Schema node = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, node);
			return node;
		} else if (JsonUtil.isBoolean(json)) {
			return new BooleanUnionValueImpl(JsonUtil.toBoolean(json));
		}
		return null;
	}

	@Override
	public RootCapable readRoot(JsonNode json) {
		if (json.isObject() && json.has("type")) {
			Syn1Schema rootModel = new Syn1SchemaImpl();
			this.readSchema((ObjectNode) json, rootModel);
			return rootModel;
		} else if (JsonUtil.isBoolean(json)) {
			Boolean value = JsonUtil.toBoolean(json);
			return new BooleanUnionValueImpl(value, ModelType.SYN1);
		}
		return null;
	}
}