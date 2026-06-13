package io.test.synthetic.v1.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.RootCapable;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynSchema;
import io.test.synthetic.io.ModelWriter;
import io.test.synthetic.union.BooleanSchemaSchemaListUnion;
import io.test.synthetic.union.BooleanSchemaUnion;
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
			BooleanSchemaUnion union = node.getDefaultValue();
			if (union != null) {
				if (union.isBoolean()) {
					JsonUtil.setBooleanProperty(json, "defaultValue", union.asBoolean());
				}
				if (union.isSchema()) {
					ObjectNode jsonValue = JsonUtil.objectNode();
					this.writeSchema((Syn1Schema) union.asSchema(), jsonValue);
					JsonUtil.setObjectProperty(json, "defaultValue", jsonValue);
				}
			}
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
			BooleanSchemaSchemaListUnion union = node.getItems();
			if (union != null) {
				if (union.isBoolean()) {
					JsonUtil.setBooleanProperty(json, "items", union.asBoolean());
				}
				if (union.isSchema()) {
					ObjectNode jsonValue = JsonUtil.objectNode();
					this.writeSchema((Syn1Schema) union.asSchema(), jsonValue);
					JsonUtil.setObjectProperty(json, "items", jsonValue);
				}
				if (union.isSchemaList()) {
					List<? extends SynSchema> models = union.asSchemaList();
					ArrayNode array = JsonUtil.arrayNode();
					models.forEach(model -> {
						ObjectNode object = JsonUtil.objectNode();
						this.writeSchema((Syn1Schema) model, object);
						JsonUtil.addToArray(array, object);
					});
					JsonUtil.setAnyProperty(json, "items", array);
				}
			}
		}
		{
			Map<String, BooleanSchemaUnion> unionMap = node.getProperties();
			if (unionMap != null && !unionMap.isEmpty()) {
				ObjectNode mapObject = JsonUtil.objectNode();
				unionMap.keySet().forEach(key -> {
					BooleanSchemaUnion union = unionMap.get(key);
					if (union.isBoolean()) {
						mapObject.put(key, union.asBoolean());
					}
					if (union.isSchema()) {
						ObjectNode object = JsonUtil.objectNode();
						this.writeSchema((Syn1Schema) union.asSchema(), object);
						JsonUtil.setObjectProperty(mapObject, key, object);
					}
				});
				JsonUtil.setObjectProperty(json, "properties", mapObject);
			}
		}
		{
			List<BooleanSchemaUnion> unionList = node.getAllOf();
			if (unionList != null && !unionList.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				unionList.forEach(union -> {
					if (union.isBoolean()) {
						array.add(union.asBoolean());
					}
					if (union.isSchema()) {
						ObjectNode object = JsonUtil.objectNode();
						this.writeSchema((Syn1Schema) union.asSchema(), object);
						JsonUtil.addToArray(array, object);
					}
				});
				JsonUtil.setAnyProperty(json, "allOf", array);
			}
		}
		{
			Map<String, BooleanSchemaUnion> unionMap = node.getDefinitions();
			if (unionMap != null && !unionMap.isEmpty()) {
				ObjectNode mapObject = JsonUtil.objectNode();
				unionMap.keySet().forEach(key -> {
					BooleanSchemaUnion union = unionMap.get(key);
					if (union.isBoolean()) {
						mapObject.put(key, union.asBoolean());
					}
					if (union.isSchema()) {
						ObjectNode object = JsonUtil.objectNode();
						this.writeSchema((Syn1Schema) union.asSchema(), object);
						JsonUtil.setObjectProperty(mapObject, key, object);
					}
				});
				JsonUtil.setObjectProperty(json, "definitions", mapObject);
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

	@Override
	public ObjectNode writeRoot(RootCapable node) {
		ObjectNode json = JsonUtil.objectNode();
		if (node instanceof Syn1Schema) {
			this.writeSchema((Syn1Schema) node, json);
		}
		return json;
	}
}