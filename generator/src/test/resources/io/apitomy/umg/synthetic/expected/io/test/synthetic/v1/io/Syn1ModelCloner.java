package io.test.synthetic.v1.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.SynItem;
import io.test.synthetic.union.BooleanSchemaSchemaListUnion;
import io.test.synthetic.union.BooleanSchemaUnion;
import io.test.synthetic.union.BooleanUnionValue;
import io.test.synthetic.union.BooleanUnionValueImpl;
import io.test.synthetic.union.SchemaListUnionValue;
import io.test.synthetic.union.SchemaListUnionValueImpl;
import io.test.synthetic.util.JsonUtil;
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn1ModelCloner {

	public void cloneDocument(Syn1Document source, Syn1Document target) {
		target.setVersion(source.getVersion());
		{
			if (source.getInfo() != null) {
				target.setInfo(target.createInfo());
				this.cloneInfo((Syn1Info) source.getInfo(), (Syn1Info) target.getInfo());
			}
		}
		{
			List<? extends SynItem> srcList = source.getItems();
			if (srcList != null && !srcList.isEmpty()) {
				srcList.forEach(srcItem -> {
					Syn1Item tgtItem = (Syn1Item) target.createItem();
					this.cloneItem((Syn1Item) srcItem, tgtItem);
					target.addItem(tgtItem);
				});
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
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void cloneInfo(Syn1Info source, Syn1Info target) {
		target.setName(source.getName());
		{
			if (source.getContact() != null) {
				target.setContact(target.createContact());
				this.cloneContact((Syn1Contact) source.getContact(), (Syn1Contact) target.getContact());
			}
		}
		target.setVersion(source.getVersion());
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void cloneContact(Syn1Contact source, Syn1Contact target) {
		target.setName(source.getName());
		target.setEmail(source.getEmail());
		target.setUrl(source.getUrl());
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void cloneItem(Syn1Item source, Syn1Item target) {
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
				this.cloneSchema((Syn1Schema) source.getSchema(), (Syn1Schema) target.getSchema());
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
				if (srcUnion.isBoolean()) {
					target.setDefaultValue(new BooleanUnionValueImpl(srcUnion.asBoolean()));
				}
				if (srcUnion.isSchema()) {
					target.setDefaultValue(target.createSchema());
					this.cloneSchema((Syn1Schema) srcUnion.asSchema(), (Syn1Schema) target.getDefaultValue());
				}
			}
		}
		target.setTitle(source.getTitle());
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void cloneSchema(Syn1Schema source, Syn1Schema target) {
		target.set$ref(source.get$ref());
		target.setType(source.getType());
		{
			BooleanSchemaSchemaListUnion srcUnion = source.getItems();
			if (srcUnion != null) {
				if (srcUnion.isBoolean()) {
					target.setItems(new BooleanUnionValueImpl(srcUnion.asBoolean()));
				}
				if (srcUnion.isSchema()) {
					target.setItems(target.createSchema());
					this.cloneSchema((Syn1Schema) srcUnion.asSchema(), (Syn1Schema) target.getItems());
				}
				if (srcUnion.isSchemaList()) {
					List<Syn1Schema> clonedList = new ArrayList<>();
					srcUnion.asSchemaList().forEach(srcItem -> {
						Syn1Schema tgtItem = (Syn1Schema) target.createSchema();
						this.cloneSchema((Syn1Schema) srcItem, tgtItem);
						clonedList.add(tgtItem);
					});
					@SuppressWarnings({"unchecked", "rawtypes"})
					SchemaListUnionValueImpl unionValue = new SchemaListUnionValueImpl((List) clonedList);
					target.setItems(unionValue);
				}
			}
		}
		{
			Map<String, BooleanSchemaUnion> srcMap = source.getProperties();
			if (srcMap != null && !srcMap.isEmpty()) {
				srcMap.keySet().forEach(key -> {
					BooleanSchemaUnion srcUnion = srcMap.get(key);
					if (srcUnion.isBoolean()) {
						target.addProperty(key, new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
					if (srcUnion.isSchema()) {
						Syn1Schema tgtItem = (Syn1Schema) target.createSchema();
						this.cloneSchema((Syn1Schema) srcUnion.asSchema(), tgtItem);
						target.addProperty(key, tgtItem);
					}
				});
			}
		}
		{
			List<BooleanSchemaUnion> srcList = source.getAllOf();
			if (srcList != null && !srcList.isEmpty()) {
				srcList.forEach(srcUnion -> {
					if (srcUnion.isBoolean()) {
						target.addAllOf(new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
					if (srcUnion.isSchema()) {
						Syn1Schema tgtItem = (Syn1Schema) target.createSchema();
						this.cloneSchema((Syn1Schema) srcUnion.asSchema(), tgtItem);
						target.addAllOf(tgtItem);
					}
				});
			}
		}
		{
			Map<String, BooleanSchemaUnion> srcMap = source.getDefinitions();
			if (srcMap != null && !srcMap.isEmpty()) {
				srcMap.keySet().forEach(key -> {
					BooleanSchemaUnion srcUnion = srcMap.get(key);
					if (srcUnion.isBoolean()) {
						target.addDefinition(key, new BooleanUnionValueImpl(srcUnion.asBoolean()));
					}
					if (srcUnion.isSchema()) {
						Syn1Schema tgtItem = (Syn1Schema) target.createSchema();
						this.cloneSchema((Syn1Schema) srcUnion.asSchema(), tgtItem);
						target.addDefinition(key, tgtItem);
					}
				});
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
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void clonePaths(Syn1Paths source, Syn1Paths target) {
		{
			List<String> itemNames = source.getItemNames();
			if (itemNames != null) {
				itemNames.forEach(name -> {
					Syn1PathItem srcItem = (Syn1PathItem) source.getItem(name);
					if (srcItem != null) {
						Syn1PathItem tgtItem = (Syn1PathItem) target.createPathItem();
						this.clonePathItem(srcItem, tgtItem);
						target.addItem(name, tgtItem);
					}
				});
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void clonePathItem(Syn1PathItem source, Syn1PathItem target) {
		target.set$ref(source.get$ref());
		target.setSummary(source.getSummary());
		{
			if (source.getGet() != null) {
				target.setGet(target.createOperation());
				this.cloneOperation((Syn1Operation) source.getGet(), (Syn1Operation) target.getGet());
			}
		}
		{
			if (source.getPut() != null) {
				target.setPut(target.createOperation());
				this.cloneOperation((Syn1Operation) source.getPut(), (Syn1Operation) target.getPut());
			}
		}
		{
			if (source.getPost() != null) {
				target.setPost(target.createOperation());
				this.cloneOperation((Syn1Operation) source.getPost(), (Syn1Operation) target.getPost());
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}

	public void cloneOperation(Syn1Operation source, Syn1Operation target) {
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
				srcList.forEach(srcItem -> {
					Syn1Item tgtItem = (Syn1Item) target.createItem();
					this.cloneItem((Syn1Item) srcItem, tgtItem);
					target.addParameter(tgtItem);
				});
			}
		}
		{
			Map<String, JsonNode> srcMap = source.getExtensions();
			if (srcMap != null && !srcMap.isEmpty()) {
				List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
				keys.forEach(name -> {
					target.addExtension(name, srcMap.get(name));
				});
			}
		}
		{
			List<String> extraPropertyNames = source.getExtraPropertyNames();
			if (extraPropertyNames != null) {
				extraPropertyNames.forEach(name -> {
					JsonNode value = source.getExtraProperty(name);
					if (value != null) {
						target.addExtraProperty(name, JsonUtil.clone(value));
					}
				});
			}
		}
	}
}