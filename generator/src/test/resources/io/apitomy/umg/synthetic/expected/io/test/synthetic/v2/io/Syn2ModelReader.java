package io.test.synthetic.v2.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.ModelType;
import io.test.synthetic.RootCapable;
import io.test.synthetic.io.ModelReader;
import io.test.synthetic.union.BooleanSchemaSchemaListUnion;
import io.test.synthetic.union.BooleanSchemaUnion;
import io.test.synthetic.union.BooleanUnionValueImpl;
import io.test.synthetic.union.SchemaListUnionValueImpl;
import io.test.synthetic.union.SchemaOrBoolean;
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
import java.util.List;
import java.util.Map;

public class Syn2ModelReader implements ModelReader {

	public void readDocument(ObjectNode json, Syn2Document node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "version");
			node.setVersion(value);
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "info");
			if (object != null) {
				node.setInfo(node.createInfo());
				readInfo(object, (Syn2Info) node.getInfo());
			}
		}
		{
			List<ObjectNode> objects = JsonUtil.consumeObjectArrayProperty(json, "items");
			if (objects != null) {
				objects.forEach(object -> {
					Syn2Item model = (Syn2Item) node.createItem();
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
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "webhooks");
			JsonUtil.keys(object).forEach(name -> {
				ObjectNode mapValue = JsonUtil.consumeObjectProperty(object, name);
				if (mapValue != null) {
					Syn2PathItem model = (Syn2PathItem) node.createPathItem();
					node.addWebhook(name, model);
					this.readPathItem(mapValue, model);
				}
			});
		}
		{
			JsonNode value = JsonUtil.consumeAnyProperty(json, "additionalSchema");
			if (value != null) {
				node.setAdditionalSchema(this.readSchemaOrBoolean(value, null));
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

	public void readInfo(ObjectNode json, Syn2Info node) {
		{
			String value = JsonUtil.consumeStringProperty(json, "name");
			node.setName(value);
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "contact");
			if (object != null) {
				node.setContact(node.createContact());
				readContact(object, (Syn2Contact) node.getContact());
			}
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "version");
			node.setVersion(value);
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "license");
			node.setLicense(value);
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

	public void readContact(ObjectNode json, Syn2Contact node) {
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

	public void readItem(ObjectNode json, Syn2Item node) {
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
				readSchema(object, (Syn2Schema) node.getSchema());
			}
		}
		{
			List<JsonNode> value = JsonUtil.consumeAnyArrayProperty(json, "examples");
			node.setExamples(value);
		}
		{
			JsonNode value = JsonUtil.consumeAnyProperty(json, "defaultValue");
			if (value != null) {
				node.setDefaultValue(this.readBooleanSchemaUnion(value, null));
			}
		}
		{
			String value = JsonUtil.consumeStringProperty(json, "title");
			node.setTitle(value);
		}
		{
			Boolean value = JsonUtil.consumeBooleanProperty(json, "deprecated");
			node.setDeprecated(value);
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

	public void readSchema(ObjectNode json, Syn2Schema node) {
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
				node.setItems(this.readBooleanSchemaSchemaListUnion(value, null));
			}
		}
		{
			ObjectNode mapObj = JsonUtil.consumeObjectProperty(json, "properties");
			if (mapObj != null) {
				JsonUtil.keys(mapObj).forEach(key -> {
					JsonNode value = JsonUtil.consumeAnyProperty(mapObj, key);
					if (value != null) {
						BooleanSchemaUnion model = this.readBooleanSchemaUnion(value, null);
						if (model != null)
							node.addProperty(key, model);
					}
				});
			}
		}
		{
			List<JsonNode> array = JsonUtil.consumeAnyArrayProperty(json, "allOf");
			if (array != null) {
				array.forEach(item -> {
					BooleanSchemaUnion value = this.readBooleanSchemaUnion(item, null);
					if (value != null)
						node.addAllOf(value);
				});
			}
		}
		{
			ObjectNode mapObj = JsonUtil.consumeObjectProperty(json, "definitions");
			if (mapObj != null) {
				JsonUtil.keys(mapObj).forEach(key -> {
					JsonNode value = JsonUtil.consumeAnyProperty(mapObj, key);
					if (value != null) {
						BooleanSchemaUnion model = this.readBooleanSchemaUnion(value, null);
						if (model != null)
							node.addDefinition(key, model);
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
						SchemaOrBoolean model = this.readSchemaOrBoolean(value, null);
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
					SchemaOrBoolean value = this.readSchemaOrBoolean(item, null);
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

	public void readPaths(ObjectNode json, Syn2Paths node) {
		{
			List<String> propertyNames = JsonUtil.keys(json);
			propertyNames.forEach(name -> {
				ObjectNode object = JsonUtil.consumeObjectProperty(json, name);
				if (object != null) {
					Syn2PathItem model = (Syn2PathItem) node.createPathItem();
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

	public void readPathItem(ObjectNode json, Syn2PathItem node) {
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
				readOperation(object, (Syn2Operation) node.getGet());
			}
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "put");
			if (object != null) {
				node.setPut(node.createOperation());
				readOperation(object, (Syn2Operation) node.getPut());
			}
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "post");
			if (object != null) {
				node.setPost(node.createOperation());
				readOperation(object, (Syn2Operation) node.getPost());
			}
		}
		{
			ObjectNode object = JsonUtil.consumeObjectProperty(json, "delete");
			if (object != null) {
				node.setDelete(node.createOperation());
				readOperation(object, (Syn2Operation) node.getDelete());
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

	public void readOperation(ObjectNode json, Syn2Operation node) {
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
					Syn2Item model = (Syn2Item) node.createItem();
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