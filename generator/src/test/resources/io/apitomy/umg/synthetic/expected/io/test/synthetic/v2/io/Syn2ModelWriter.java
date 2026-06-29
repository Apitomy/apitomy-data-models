package io.test.synthetic.v2.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.RootCapable;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.io.ModelWriter;
import io.test.synthetic.util.JsonUtil;
import io.test.synthetic.util.WriterUtil;
import io.test.synthetic.v2.Syn2Contact;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Syn2ModelWriter implements ModelWriter {

	public void writeDocument(Syn2Document node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "version", JsonUtil.toJsonNode(node.getVersion()));
		{
			if (node.getInfo() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeInfo((Syn2Info) node.getInfo(), object);
				JsonUtil.setProperty(json, "info", object);
			}
		}
		{
			List<? extends SynItem> models = node.getItems();
			if (models != null && !models.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				for (int _idx = 0; _idx < models.size(); _idx++) {
					SynItem model = models.get(_idx);
					ObjectNode object = JsonUtil.objectNode();
					this.writeItem((Syn2Item) model, object);
					JsonUtil.addToArray(array, object);
				}
				JsonUtil.setProperty(json, "items", array);
			}
		}
		JsonUtil.setProperty(json, "tags", JsonUtil.toArrayNode(node.getTags()));
		JsonUtil.setProperty(json, "metadata", JsonUtil.toObjectNode(node.getMetadata()));
		{
			Map<String, ? extends SynPathItem> models = node.getWebhooks();
			if (models != null && !models.isEmpty()) {
				ObjectNode object = JsonUtil.objectNode();
				for (String jsonName : models.keySet()) {
					ObjectNode jsonValue = JsonUtil.objectNode();
					this.writePathItem((Syn2PathItem) models.get(jsonName), jsonValue);
					JsonUtil.setProperty(object, jsonName, jsonValue);
				}
				JsonUtil.setProperty(json, "webhooks", object);
			}
		}
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

	public void writeInfo(Syn2Info node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "name", JsonUtil.toJsonNode(node.getName()));
		{
			if (node.getContact() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeContact((Syn2Contact) node.getContact(), object);
				JsonUtil.setProperty(json, "contact", object);
			}
		}
		JsonUtil.setProperty(json, "version", JsonUtil.toJsonNode(node.getVersion()));
		JsonUtil.setProperty(json, "license", JsonUtil.toJsonNode(node.getLicense()));
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

	public void writeContact(Syn2Contact node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "name", JsonUtil.toJsonNode(node.getName()));
		JsonUtil.setProperty(json, "email", JsonUtil.toJsonNode(node.getEmail()));
		JsonUtil.setProperty(json, "url", JsonUtil.toJsonNode(node.getUrl()));
		WriterUtil.writeExtraProperties(node, json);
	}

	public void writeItem(Syn2Item node, ObjectNode json) {
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
				this.writeSchema((Syn2Schema) node.getSchema(), object);
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
		JsonUtil.setProperty(json, "deprecated", JsonUtil.toJsonNode(node.isDeprecated()));
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

	public void writeSchema(Syn2Schema node, ObjectNode json) {
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
				for (String key : keys) {
					JsonNode value = this.writeBooleanSchemaUnion(items.get(key));
					if (value != null)
						JsonUtil.setProperty(mapJson, key, value);
				}
				JsonUtil.setProperty(json, "properties", mapJson);
			}
		}
		{
			List<BooleanSchemaUnion> items = node.getAllOf();
			if (items != null && !items.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				for (int _idx = 0; _idx < items.size(); _idx++) {
					BooleanSchemaUnion item = items.get(_idx);
					JsonNode value = this.writeBooleanSchemaUnion(item);
					if (value != null)
						array.add(value);
				}
				JsonUtil.setProperty(json, "allOf", array);
			}
		}
		{
			Map<String, BooleanSchemaUnion> items = node.getDefinitions();
			if (items != null && !items.isEmpty()) {
				ObjectNode mapJson = JsonUtil.objectNode();
				Collection<String> keys = items.keySet();
				for (String key : keys) {
					JsonNode value = this.writeBooleanSchemaUnion(items.get(key));
					if (value != null)
						JsonUtil.setProperty(mapJson, key, value);
				}
				JsonUtil.setProperty(json, "definitions", mapJson);
			}
		}
		{
			Map<String, SchemaOrBoolean> items = node.getNestedSchemas();
			if (items != null && !items.isEmpty()) {
				ObjectNode mapJson = JsonUtil.objectNode();
				Collection<String> keys = items.keySet();
				for (String key : keys) {
					JsonNode value = this.writeSchemaOrBoolean(items.get(key));
					if (value != null)
						JsonUtil.setProperty(mapJson, key, value);
				}
				JsonUtil.setProperty(json, "nestedSchemas", mapJson);
			}
		}
		{
			List<SchemaOrBoolean> items = node.getComposedSchemas();
			if (items != null && !items.isEmpty()) {
				ArrayNode array = JsonUtil.arrayNode();
				for (int _idx = 0; _idx < items.size(); _idx++) {
					SchemaOrBoolean item = items.get(_idx);
					JsonNode value = this.writeSchemaOrBoolean(item);
					if (value != null)
						array.add(value);
				}
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

	public void writePaths(Syn2Paths node, ObjectNode json) {
		if (node == null) {
			return;
		}
		{
			List<String> propertyNames = node.getItemNames();
			for (int _i = 0; _i < propertyNames.size(); _i++) {
				String propertyName = propertyNames.get(_i);
				ObjectNode object = JsonUtil.objectNode();
				this.writePathItem((Syn2PathItem) node.getItem(propertyName), object);
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

	public void writePathItem(Syn2PathItem node, ObjectNode json) {
		if (node == null) {
			return;
		}
		JsonUtil.setProperty(json, "$ref", JsonUtil.toJsonNode(node.get$ref()));
		JsonUtil.setProperty(json, "summary", JsonUtil.toJsonNode(node.getSummary()));
		{
			if (node.getGet() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn2Operation) node.getGet(), object);
				JsonUtil.setProperty(json, "get", object);
			}
		}
		{
			if (node.getPut() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn2Operation) node.getPut(), object);
				JsonUtil.setProperty(json, "put", object);
			}
		}
		{
			if (node.getPost() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn2Operation) node.getPost(), object);
				JsonUtil.setProperty(json, "post", object);
			}
		}
		{
			if (node.getDelete() != null) {
				ObjectNode object = JsonUtil.objectNode();
				this.writeOperation((Syn2Operation) node.getDelete(), object);
				JsonUtil.setProperty(json, "delete", object);
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

	public void writeOperation(Syn2Operation node, ObjectNode json) {
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
				for (int _idx = 0; _idx < models.size(); _idx++) {
					SynItem model = models.get(_idx);
					ObjectNode object = JsonUtil.objectNode();
					this.writeItem((Syn2Item) model, object);
					JsonUtil.addToArray(array, object);
				}
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
			this.writeSchema((Syn2Schema) union.asSchema(), jsonValue);
			return jsonValue;
		}
		if (union.isBoolean()) {
			return JsonUtil.toJsonNode(union.asBoolean());
		}
		return null;
	}

	private JsonNode writeBooleanSchemaSchemaListUnion(BooleanSchemaSchemaListUnion union) {
		if (union == null)
			return null;
		if (union.isSchema()) {
			ObjectNode jsonValue = JsonUtil.objectNode();
			this.writeSchema((Syn2Schema) union.asSchema(), jsonValue);
			return jsonValue;
		}
		if (union.isSchemaList()) {
			ArrayNode array = JsonUtil.arrayNode();
			for (Object item : union.asSchemaList()) {
				ObjectNode itemNode = JsonUtil.objectNode();
				this.writeSchema((Syn2Schema) item, itemNode);
				array.add(itemNode);
			}
			return array;
		}
		if (union.isBoolean()) {
			return JsonUtil.toJsonNode(union.asBoolean());
		}
		return null;
	}

	private JsonNode writeBooleanSchemaUnion(BooleanSchemaUnion union) {
		if (union == null)
			return null;
		if (union.isSchema()) {
			ObjectNode jsonValue = JsonUtil.objectNode();
			this.writeSchema((Syn2Schema) union.asSchema(), jsonValue);
			return jsonValue;
		}
		if (union.isBoolean()) {
			return JsonUtil.toJsonNode(union.asBoolean());
		}
		return null;
	}

	@Override
	public JsonNode writeRoot(RootCapable node) {
		return this.writeSchemaOrBoolean((SchemaOrBoolean) node);
	}
}