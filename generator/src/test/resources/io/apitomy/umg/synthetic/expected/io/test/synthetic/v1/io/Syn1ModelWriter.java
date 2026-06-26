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
		JsonUtil.setProperty(json, "version", JsonUtil.toJsonNode(node.getVersion()));
		{
			if (node.getInfo() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeInfo((Syn1Info) node.getInfo(), object);
				JsonUtil.setProperty(json, "info", object);
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
				JsonUtil.setProperty(json, "items", array);
			}
		}
		JsonUtil.setProperty(json, "tags", JsonUtil.toArrayNode(node.getTags()));
		JsonUtil.setProperty(json, "metadata", JsonUtil.toObjectNode(node.getMetadata()));
		{
			JsonNode value = this.writeSchemaOrBoolean(node.getAdditionalSchema());
			if (value != null)
				JsonUtil.setProperty(json, "additionalSchema", value);
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeInfo(Syn1Info node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "name", JsonUtil.toJsonNode(node.getName()));
		{
			if (node.getContact() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeContact((Syn1Contact) node.getContact(), object);
				JsonUtil.setProperty(json, "contact", object);
			}
		}
		JsonUtil.setProperty(json, "version", JsonUtil.toJsonNode(node.getVersion()));
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeContact(Syn1Contact node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "name", JsonUtil.toJsonNode(node.getName()));
		JsonUtil.setProperty(json, "email", JsonUtil.toJsonNode(node.getEmail()));
		JsonUtil.setProperty(json, "url", JsonUtil.toJsonNode(node.getUrl()));
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeItem(Syn1Item node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "$ref", JsonUtil.toJsonNode(node.get$ref()));
		JsonUtil.setProperty(json, "description", JsonUtil.toJsonNode(node.getDescription()));
		JsonUtil.setProperty(json, "required", JsonUtil.toJsonNode(node.isRequired()));
		JsonUtil.setProperty(json, "order", JsonUtil.toJsonNode(node.getOrder()));
		JsonUtil.setProperty(json, "weight", JsonUtil.toJsonNode(node.getWeight()));
		JsonUtil.setProperty(json, "extra", node.getExtra());
		JsonUtil.setProperty(json, "raw", node.getRaw());
		{
			if (node.getSchema() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeSchema((Syn1Schema) node.getSchema(), object);
				JsonUtil.setProperty(json, "schema", object);
			}
		}
		JsonUtil.setProperty(json, "examples", JsonUtil.toArrayNode(node.getExamples()));
		{
			JsonNode value = this.writeBooleanSchemaUnion(node.getDefaultValue());
			if (value != null)
				JsonUtil.setProperty(json, "defaultValue", value);
		}
		JsonUtil.setProperty(json, "title", JsonUtil.toJsonNode(node.getTitle()));
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeSchema(Syn1Schema node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "$ref", JsonUtil.toJsonNode(node.get$ref()));
		JsonUtil.setProperty(json, "type", JsonUtil.toJsonNode(node.getType()));
		{
			JsonNode value = this.writeBooleanSchemaSchemaListUnion(node.getItems());
			if (value != null)
				JsonUtil.setProperty(json, "items", value);
		}
		{
			Map<String, BooleanSchemaUnion> items = node.getProperties();
			if (items != null && !items.isEmpty()) {
				ObjectNode mapJson = JsonUtil.objectNode();
				Collection<String> keys = items.keySet();
				keys.forEach(key -> {
					JsonNode value = this.writeBooleanSchemaUnion(items.get(key));
					if (value != null)
						JsonUtil.setProperty(mapJson, key, value);
				});
				JsonUtil.setProperty(json, "properties", mapJson);
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
				JsonUtil.setProperty(json, "allOf", array);
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
						JsonUtil.setProperty(mapJson, key, value);
				});
				JsonUtil.setProperty(json, "definitions", mapJson);
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
						JsonUtil.setProperty(mapJson, key, value);
				});
				JsonUtil.setProperty(json, "nestedSchemas", mapJson);
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
				JsonUtil.setProperty(json, "composedSchemas", array);
			}
		}
		JsonUtil.setProperty(json, "minLength", JsonUtil.toJsonNode(node.getMinLength()));
		JsonUtil.setProperty(json, "maxLength", JsonUtil.toJsonNode(node.getMaxLength()));
		JsonUtil.setProperty(json, "enum", JsonUtil.toArrayNode(node.getEnum()));
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
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
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String propertyName = propertyNames.get(_i);
				ObjectNode object = JsonUtil.objectNode();
				this.writePathItem((Syn1PathItem) node.getItem(propertyName), object);
				JsonUtil.setProperty(json, propertyName, object);
			}
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writePathItem(Syn1PathItem node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "$ref", JsonUtil.toJsonNode(node.get$ref()));
		JsonUtil.setProperty(json, "summary", JsonUtil.toJsonNode(node.getSummary()));
		{
			if (node.getGet() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn1Operation) node.getGet(), object);
				JsonUtil.setProperty(json, "get", object);
			}
		}
		{
			if (node.getPut() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn1Operation) node.getPut(), object);
				JsonUtil.setProperty(json, "put", object);
			}
		}
		{
			if (node.getPost() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn1Operation) node.getPost(), object);
				JsonUtil.setProperty(json, "post", object);
			}
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
			}
		}
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeOperation(Syn1Operation node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "operationId", JsonUtil.toJsonNode(node.getOperationId()));
		JsonUtil.setProperty(json, "summary", JsonUtil.toJsonNode(node.getSummary()));
		JsonUtil.setProperty(json, "tags", JsonUtil.toArrayNode(node.getTags()));
		{
			List<? extends SynItem> models = node.getParameters();
			if (models != null && !models.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				models.forEach(model -> {
					ObjectNode object = JsonUtil.objectNode();
					this.writeItem((Syn1Item) model, object);
					JsonUtil.addToArray(array, object);
				});
				JsonUtil.setProperty(json, "parameters", array);
			}
		}
		{
			Map<String, JsonNode> values = node.getExtensions();
			if (values != null && !values.isEmpty()) {
				List<String> _keys = new java.util.ArrayList<>(values.keySet());
				for (int _i = 0; _i < _keys.size(); _i++) {
					String propertyName = _keys.get(_i);
					JsonNode value = values.get(propertyName);
					JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
				}
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