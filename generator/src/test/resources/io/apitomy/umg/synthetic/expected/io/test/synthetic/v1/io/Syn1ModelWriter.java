package io.test.synthetic.v1.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.RootCapable;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.SynItem;
import io.test.synthetic.io.ModelWriter;
import io.test.synthetic.util.JsonUtil;
import io.test.synthetic.util.WriterUtil;
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Syn1ModelWriter implements ModelWriter {

	public void writeDocument(Syn1Document node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "version", node.getVersion());
		{
			if (node.getInfo() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeInfo((Syn1Info) node.getInfo(), object);
				JsonUtil.setObjectProperty(json, "info", object);
			}
		}
		{
			List<? extends SynItem> models = node.getItems();
			if (models != null && !models.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				models.forEach(model -> {
					ObjectNode object = JsonUtil.objectNode();
					this.writeItem((Syn1Item) model, object);
					JsonUtil.addToArray(array, object);
				});
				JsonUtil.setAnyProperty(json, "items", array);
			}
		}
		JsonUtil.setStringArrayProperty(json, "tags", node.getTags());
		JsonUtil.setStringMapProperty(json, "metadata", node.getMetadata());
		{
			JsonNode value = this.writeSchemaOrBoolean(node.getAdditionalSchema());
			if (value != null)
				JsonUtil.setAnyProperty(json, "additionalSchema", value);
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeInfo(Syn1Info node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "name", node.getName());
		{
			if (node.getContact() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeContact((Syn1Contact) node.getContact(), object);
				JsonUtil.setObjectProperty(json, "contact", object);
			}
		}
		JsonUtil.setStringProperty(json, "version", node.getVersion());
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeContact(Syn1Contact node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "name", node.getName());
		JsonUtil.setStringProperty(json, "email", node.getEmail());
		JsonUtil.setStringProperty(json, "url", node.getUrl());
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeItem(Syn1Item node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "$ref", node.get$ref());
		JsonUtil.setStringProperty(json, "description", node.getDescription());
		JsonUtil.setBooleanProperty(json, "required", node.isRequired());
		JsonUtil.setIntegerProperty(json, "order", node.getOrder());
		JsonUtil.setNumberProperty(json, "weight", node.getWeight());
		JsonUtil.setAnyProperty(json, "extra", node.getExtra());
		JsonUtil.setObjectProperty(json, "raw", node.getRaw());
		{
			if (node.getSchema() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeSchema((Syn1Schema) node.getSchema(), object);
				JsonUtil.setObjectProperty(json, "schema", object);
			}
		}
		JsonUtil.setAnyArrayProperty(json, "examples", node.getExamples());
		{
			JsonNode value = this.writeBooleanSchemaUnion(node.getDefaultValue());
			if (value != null)
				JsonUtil.setAnyProperty(json, "defaultValue", value);
		}
		JsonUtil.setStringProperty(json, "title", node.getTitle());
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeSchema(Syn1Schema node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "$ref", node.get$ref());
		JsonUtil.setStringProperty(json, "type", node.getType());
		{
			JsonNode value = this.writeBooleanSchemaSchemaListUnion(node.getItems());
			if (value != null)
				JsonUtil.setAnyProperty(json, "items", value);
		}
		{
			Map<String, BooleanSchemaUnion> items = node.getProperties();
			if (items != null && !items.isEmpty()) {
				ObjectNode mapJson = JsonUtil.objectNode();
				Collection<String> keys = items.keySet();
				keys.forEach(key -> {
					JsonNode value = this.writeBooleanSchemaUnion(items.get(key));
					if (value != null)
						JsonUtil.setAnyProperty(mapJson, key, value);
				});
				JsonUtil.setObjectProperty(json, "properties", mapJson);
			}
		}
		{
			List<BooleanSchemaUnion> items = node.getAllOf();
			if (items != null && !items.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				items.forEach(item -> {
					JsonNode value = this.writeBooleanSchemaUnion(item);
					if (value != null)
						array.add(value);
				});
				JsonUtil.setAnyProperty(json, "allOf", array);
			}
		}
		{
			Map<String, BooleanSchemaUnion> items = node.getDefinitions();
			if (items != null && !items.isEmpty()) {
				ObjectNode mapJson = JsonUtil.objectNode();
				Collection<String> keys = items.keySet();
				keys.forEach(key -> {
					JsonNode value = this.writeBooleanSchemaUnion(items.get(key));
					if (value != null)
						JsonUtil.setAnyProperty(mapJson, key, value);
				});
				JsonUtil.setObjectProperty(json, "definitions", mapJson);
			}
		}
		{
			Map<String, SchemaOrBoolean> items = node.getNestedSchemas();
			if (items != null && !items.isEmpty()) {
				ObjectNode mapJson = JsonUtil.objectNode();
				Collection<String> keys = items.keySet();
				keys.forEach(key -> {
					JsonNode value = this.writeSchemaOrBoolean(items.get(key));
					if (value != null)
						JsonUtil.setAnyProperty(mapJson, key, value);
				});
				JsonUtil.setObjectProperty(json, "nestedSchemas", mapJson);
			}
		}
		{
			List<SchemaOrBoolean> items = node.getComposedSchemas();
			if (items != null && !items.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				items.forEach(item -> {
					JsonNode value = this.writeSchemaOrBoolean(item);
					if (value != null)
						array.add(value);
				});
				JsonUtil.setAnyProperty(json, "composedSchemas", array);
			}
		}
		JsonUtil.setIntegerProperty(json, "minLength", node.getMinLength());
		JsonUtil.setIntegerProperty(json, "maxLength", node.getMaxLength());
		JsonUtil.setAnyArrayProperty(json, "enum", node.getEnum());
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writePaths(Syn1Paths node, ObjectNode json) {
		if (node == null) {
			return;
		}
		{
			List<String> propertyNames = node.getItemNames();
			propertyNames.forEach(propertyName -> {
				ObjectNode object = JsonUtil.objectNode();
				this.writePathItem((Syn1PathItem) node.getItem(propertyName), object);
				JsonUtil.setObjectProperty(json, propertyName, object);
			});
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writePathItem(Syn1PathItem node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "$ref", node.get$ref());
		JsonUtil.setStringProperty(json, "summary", node.getSummary());
		{
			if (node.getGet() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn1Operation) node.getGet(), object);
				JsonUtil.setObjectProperty(json, "get", object);
			}
		}
		{
			if (node.getPut() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn1Operation) node.getPut(), object);
				JsonUtil.setObjectProperty(json, "put", object);
			}
		}
		{
			if (node.getPost() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn1Operation) node.getPost(), object);
				JsonUtil.setObjectProperty(json, "post", object);
			}
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeOperation(Syn1Operation node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setStringProperty(json, "operationId", node.getOperationId());
		JsonUtil.setStringProperty(json, "summary", node.getSummary());
		JsonUtil.setStringArrayProperty(json, "tags", node.getTags());
		{
			List<? extends SynItem> models = node.getParameters();
			if (models != null && !models.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				models.forEach(model -> {
					ObjectNode object = JsonUtil.objectNode();
					this.writeItem((Syn1Item) model, object);
					JsonUtil.addToArray(array, object);
				});
				JsonUtil.setAnyProperty(json, "parameters", array);
			}
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				values.keySet().forEach(propertyName -> {
					JsonNode value = values.get(propertyName);
					JsonUtil.setAnyProperty(json, propertyName, value);
				});
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	private JsonNode writeSchemaOrBoolean(SchemaOrBoolean union) {
		if (union == null)
			return null;
		if (union.isSchema()) {
			ObjectNode jsonValue = JsonUtil.objectNode();
			this.writeSchema((Syn1Schema) union.asSchema(), jsonValue);
			return jsonValue;
		}
		if (union.isBoolean()) {
			return JsonUtil.booleanToJsonNode(union.asBoolean());
		}
		return null;
	}

	private JsonNode writeBooleanSchemaSchemaListUnion(BooleanSchemaSchemaListUnion union) {
		if (union == null)
			return null;
		if (union.isSchema()) {
			ObjectNode jsonValue = JsonUtil.objectNode();
			this.writeSchema((Syn1Schema) union.asSchema(), jsonValue);
			return jsonValue;
		}
		if (union.isSchemaList()) {
			ArrayNode array = JsonUtil.arrayNode();
			for (Object item : (java.util.List<?>) union.asSchemaList()) {
				ObjectNode itemNode = JsonUtil.objectNode();
				this.writeSchema((Syn1Schema) item, itemNode);
				array.add(itemNode);
			}
			return array;
		}
		if (union.isBoolean()) {
			return JsonUtil.booleanToJsonNode(union.asBoolean());
		}
		return null;
	}

	private JsonNode writeBooleanSchemaUnion(BooleanSchemaUnion union) {
		if (union == null)
			return null;
		if (union.isSchema()) {
			ObjectNode jsonValue = JsonUtil.objectNode();
			this.writeSchema((Syn1Schema) union.asSchema(), jsonValue);
			return jsonValue;
		}
		if (union.isBoolean()) {
			return JsonUtil.booleanToJsonNode(union.asBoolean());
		}
		return null;
	}

	@Override
	public ObjectNode writeRoot(RootCapable node) {
		JsonNode result = this.writeSchemaOrBoolean((SchemaOrBoolean) node);
		if (result != null && JsonUtil.isObjectNode(result)) {
			return (ObjectNode) result;
		}
		return null;
	}
}