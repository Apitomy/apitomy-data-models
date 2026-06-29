package io.test.synthetic.v2.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.SchemaListUnionValue;
import io.test.synthetic.SchemaListUnionValueImpl;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.union.BooleanUnionValue;
import io.test.synthetic.union.BooleanUnionValueImpl;
import io.test.synthetic.util.JsonUtil;
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

public class Syn2ModelCloner {

	public void cloneDocument(Syn2Document source, Syn2Document target) {
		target.setVersion(source.getVersion());
		{
			if (source.getInfo() != null) {
				target.setInfo(target.createInfo());
				this.cloneInfo((Syn2Info) source.getInfo(), (Syn2Info) target.getInfo());
			}
		}
		{
			List<? extends SynItem> srcList = source.getItems();
			if (srcList != null && !srcList.isEmpty()) {
				for (int _idx = 0; _idx < srcList.size(); _idx++) {
					Syn2Item srcItem = (Syn2Item) srcList.get(_idx);
					Syn2Item tgtItem = (Syn2Item) target.createItem();
					this.cloneItem(srcItem, tgtItem);
					target.addItem(tgtItem);
				}
			}
		}
		{
			List<String> srcList = source.getTags();
			if (srcList != null) {
				target.setTags(new ArrayList<>(srcList));
			}
		}
		{
			Map<String, String> srcMap = source.getMetadata();
			if (srcMap != null && !srcMap.isEmpty()) {
				target.setMetadata(new LinkedHashMap<>(srcMap));
			}
		}
		{
			Map<String, ? extends SynPathItem> srcMap = source.getWebhooks();
			if (srcMap != null && !srcMap.isEmpty()) {
				for (String name : srcMap.keySet()) {
					Syn2PathItem tgtItem = (Syn2PathItem) target.createPathItem();
					this.clonePathItem((Syn2PathItem) srcMap.get(name), tgtItem);
					target.addWebhook(name, tgtItem);
				}
			}
		}
		{
			SchemaOrBoolean srcUnion = source.getAdditionalSchema();
			if (srcUnion != null) {
				if (srcUnion.isSchema()) {
					Syn2Schema tgtEntity = new Syn2SchemaImpl();
					this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtEntity);
					target.setAdditionalSchema(tgtEntity);
				}
				if (srcUnion.isBoolean()) {
					target.setAdditionalSchema(new BooleanUnionValueImpl(srcUnion.asBoolean()));
				}
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void cloneInfo(Syn2Info source, Syn2Info target) {
		target.setName(source.getName());
		{
			if (source.getContact() != null) {
				target.setContact(target.createContact());
				this.cloneContact((Syn2Contact) source.getContact(), (Syn2Contact) target.getContact());
			}
		}
		target.setVersion(source.getVersion());
		target.setLicense(source.getLicense());
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void cloneContact(Syn2Contact source, Syn2Contact target) {
		target.setName(source.getName());
		target.setEmail(source.getEmail());
		target.setUrl(source.getUrl());
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void cloneItem(Syn2Item source, Syn2Item target) {
		target.set$ref(source.get$ref());
		target.setDescription(source.getDescription());
		target.setRequired(source.isRequired());
		target.setOrder(source.getOrder());
		target.setWeight(source.getWeight());
		target.setExtra(source.getExtra());
		target.setRaw(source.getRaw());
		{
			if (source.getSchema() != null) {
				target.setSchema(target.createSchema());
				this.cloneSchema((Syn2Schema) source.getSchema(), (Syn2Schema) target.getSchema());
			}
		}
		{
			List<JsonNode> srcList = source.getExamples();
			if (srcList != null) {
				target.setExamples(new ArrayList<>(srcList));
			}
		}
		{
			BooleanSchemaUnion srcUnion = source.getDefaultValue();
			if (srcUnion != null) {
				if (srcUnion.isSchema()) {
					Syn2Schema tgtEntity = new Syn2SchemaImpl();
					this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtEntity);
					target.setDefaultValue(tgtEntity);
				}
				if (srcUnion.isBoolean()) {
					target.setDefaultValue(new BooleanUnionValueImpl(srcUnion.asBoolean()));
				}
			}
		}
		target.setTitle(source.getTitle());
		target.setDeprecated(source.isDeprecated());
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void cloneSchema(Syn2Schema source, Syn2Schema target) {
		target.set$ref(source.get$ref());
		target.setType(source.getType());
		{
			BooleanSchemaSchemaListUnion srcUnion = source.getItems();
			if (srcUnion != null) {
				if (srcUnion.isSchema()) {
					Syn2Schema tgtEntity = new Syn2SchemaImpl();
					this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtEntity);
					target.setItems(tgtEntity);
				}
				if (srcUnion.isSchemaList()) {
					List<Syn2Schema> clonedList = new ArrayList<>();
					for (int _idx = 0; _idx < srcUnion.asSchemaList().size(); _idx++) {
						Syn2Schema srcItem = (Syn2Schema) srcUnion.asSchemaList().get(_idx);
						Syn2Schema tgtItem = (Syn2Schema) target.createSchema();
						this.cloneSchema(srcItem, tgtItem);
						clonedList.add(tgtItem);
					}
					@SuppressWarnings({"unchecked", "rawtypes"})
					SchemaListUnionValueImpl unionValue = new SchemaListUnionValueImpl((List) clonedList);
					target.setItems(unionValue);
				}
				if (srcUnion.isBoolean()) {
					target.setItems(new BooleanUnionValueImpl(srcUnion.asBoolean()));
				}
			}
		}
		{
			Map<String, BooleanSchemaUnion> srcMap = source.getProperties();
			if (srcMap != null && !srcMap.isEmpty()) {
				for (String key : srcMap.keySet()) {
					BooleanSchemaUnion srcUnion = srcMap.get(key);
					if (srcUnion.isSchema()) {
						Syn2Schema tgtItem = new Syn2SchemaImpl();
						this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtItem);
						target.addProperty(key, tgtItem);
					}
					if (srcUnion.isBoolean()) {
						target.addProperty(key, new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
				}
			}
		}
		{
			List<BooleanSchemaUnion> srcList = source.getAllOf();
			if (srcList != null && !srcList.isEmpty()) {
				for (int _idx = 0; _idx < srcList.size(); _idx++) {
					BooleanSchemaUnion srcUnion = srcList.get(_idx);
					if (srcUnion.isSchema()) {
						Syn2Schema tgtItem = new Syn2SchemaImpl();
						this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtItem);
						target.addAllOf(tgtItem);
					}
					if (srcUnion.isBoolean()) {
						target.addAllOf(new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
				}
			}
		}
		{
			Map<String, BooleanSchemaUnion> srcMap = source.getDefinitions();
			if (srcMap != null && !srcMap.isEmpty()) {
				for (String key : srcMap.keySet()) {
					BooleanSchemaUnion srcUnion = srcMap.get(key);
					if (srcUnion.isSchema()) {
						Syn2Schema tgtItem = new Syn2SchemaImpl();
						this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtItem);
						target.addDefinition(key, tgtItem);
					}
					if (srcUnion.isBoolean()) {
						target.addDefinition(key, new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
				}
			}
		}
		{
			Map<String, SchemaOrBoolean> srcMap = source.getNestedSchemas();
			if (srcMap != null && !srcMap.isEmpty()) {
				for (String key : srcMap.keySet()) {
					SchemaOrBoolean srcUnion = srcMap.get(key);
					if (srcUnion.isSchema()) {
						Syn2Schema tgtItem = new Syn2SchemaImpl();
						this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtItem);
						target.addNestedSchema(key, tgtItem);
					}
					if (srcUnion.isBoolean()) {
						target.addNestedSchema(key, new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
				}
			}
		}
		{
			List<SchemaOrBoolean> srcList = source.getComposedSchemas();
			if (srcList != null && !srcList.isEmpty()) {
				for (int _idx = 0; _idx < srcList.size(); _idx++) {
					SchemaOrBoolean srcUnion = srcList.get(_idx);
					if (srcUnion.isSchema()) {
						Syn2Schema tgtItem = new Syn2SchemaImpl();
						this.cloneSchema((Syn2Schema) srcUnion.asSchema(), tgtItem);
						target.addComposedSchema(tgtItem);
					}
					if (srcUnion.isBoolean()) {
						target.addComposedSchema(new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
				}
			}
		}
		target.setMinLength(source.getMinLength());
		target.setMaxLength(source.getMaxLength());
		{
			List<JsonNode> srcList = source.getEnum();
			if (srcList != null) {
				target.setEnum(new ArrayList<>(srcList));
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void clonePaths(Syn2Paths source, Syn2Paths target) {
		{
			List<String> itemNames = source.getItemNames();
			if (itemNames != null) {
				for (int _idx = 0; _idx < itemNames.size(); _idx++) {
					String name = itemNames.get(_idx);
					Syn2PathItem srcItem = (Syn2PathItem) source.getItem(name);
					if (srcItem != null) {
						Syn2PathItem tgtItem = (Syn2PathItem) target.createPathItem();
						this.clonePathItem(srcItem, tgtItem);
						target.addItem(name, tgtItem);
					}
				}
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void clonePathItem(Syn2PathItem source, Syn2PathItem target) {
		target.set$ref(source.get$ref());
		target.setSummary(source.getSummary());
		{
			if (source.getGet() != null) {
				target.setGet(target.createOperation());
				this.cloneOperation((Syn2Operation) source.getGet(), (Syn2Operation) target.getGet());
			}
		}
		{
			if (source.getPut() != null) {
				target.setPut(target.createOperation());
				this.cloneOperation((Syn2Operation) source.getPut(), (Syn2Operation) target.getPut());
			}
		}
		{
			if (source.getPost() != null) {
				target.setPost(target.createOperation());
				this.cloneOperation((Syn2Operation) source.getPost(), (Syn2Operation) target.getPost());
			}
		}
		{
			if (source.getDelete() != null) {
				target.setDelete(target.createOperation());
				this.cloneOperation((Syn2Operation) source.getDelete(), (Syn2Operation) target.getDelete());
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}

	public void cloneOperation(Syn2Operation source, Syn2Operation target) {
		target.setOperationId(source.getOperationId());
		target.setSummary(source.getSummary());
		{
			List<String> srcList = source.getTags();
			if (srcList != null) {
				target.setTags(new ArrayList<>(srcList));
			}
		}
		{
			List<? extends SynItem> srcList = source.getParameters();
			if (srcList != null && !srcList.isEmpty()) {
				for (int _idx = 0; _idx < srcList.size(); _idx++) {
					Syn2Item srcItem = (Syn2Item) srcList.get(_idx);
					Syn2Item tgtItem = (Syn2Item) target.createItem();
					this.cloneItem(srcItem, tgtItem);
					target.addParameter(tgtItem);
				}
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				for (int _idx = 0; _idx < keys.size(); _idx++) {
					String name = keys.get(_idx);
					target.addExtension(name, srcMap.get(name));
				}
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				for (int _idx = 0; _idx < extraPropertyNames.size(); _idx++) {
					String name = extraPropertyNames.get(_idx);
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				}
			}
		}
	}
}