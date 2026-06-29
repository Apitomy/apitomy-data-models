package io.test.synthetic.v1.visitors;

import io.test.synthetic.Node;
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import io.test.synthetic.visitors.diff.AbstractDiffTraverser;
import io.test.synthetic.visitors.diff.DiffVisitor;

public class Syn1DiffTraverser extends AbstractDiffTraverser {

	public Syn1DiffTraverser(DiffVisitor visitor) {
		super(visitor);
	}

	@Override
	protected void traverseNode(Node original, Node updated) {
		Node target = original != null ? original : updated;
		if (target instanceof Syn1Document) {
			this.traverseDocument((Syn1Document) original, (Syn1Document) updated);
		} else if (target instanceof Syn1Info) {
			this.traverseInfo((Syn1Info) original, (Syn1Info) updated);
		} else if (target instanceof Syn1Contact) {
			this.traverseContact((Syn1Contact) original, (Syn1Contact) updated);
		} else if (target instanceof Syn1Item) {
			this.traverseItem((Syn1Item) original, (Syn1Item) updated);
		} else if (target instanceof Syn1Schema) {
			this.traverseSchema((Syn1Schema) original, (Syn1Schema) updated);
		} else if (target instanceof Syn1Paths) {
			this.traversePaths((Syn1Paths) original, (Syn1Paths) updated);
		} else if (target instanceof Syn1PathItem) {
			this.traversePathItem((Syn1PathItem) original, (Syn1PathItem) updated);
		} else if (target instanceof Syn1Operation) {
			this.traverseOperation((Syn1Operation) original, (Syn1Operation) updated);
		}
	}

	public void traverseDocument(Syn1Document original, Syn1Document updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("version", original.getVersion(), updated.getVersion());
		this.diffEntityField("info", original.getInfo(), updated.getInfo());
		this.diffList("items", original.getItems(), updated.getItems());
		visitor.diffPrimitive("tags", original.getTags(), updated.getTags());
		visitor.diffPrimitive("metadata", original.getMetadata(), updated.getMetadata());
		this.diffUnionField("additionalSchema", original.getAdditionalSchema(), updated.getAdditionalSchema());
	}

	public void traverseInfo(Syn1Info original, Syn1Info updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("name", original.getName(), updated.getName());
		this.diffEntityField("contact", original.getContact(), updated.getContact());
		visitor.diffPrimitive("version", original.getVersion(), updated.getVersion());
	}

	public void traverseContact(Syn1Contact original, Syn1Contact updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("name", original.getName(), updated.getName());
		visitor.diffPrimitive("email", original.getEmail(), updated.getEmail());
		visitor.diffPrimitive("url", original.getUrl(), updated.getUrl());
	}

	public void traverseItem(Syn1Item original, Syn1Item updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("$ref", original.get$ref(), updated.get$ref());
		visitor.diffPrimitive("description", original.getDescription(), updated.getDescription());
		visitor.diffPrimitive("required", original.isRequired(), updated.isRequired());
		visitor.diffPrimitive("order", original.getOrder(), updated.getOrder());
		visitor.diffPrimitive("weight", original.getWeight(), updated.getWeight());
		visitor.diffPrimitive("extra", original.getExtra(), updated.getExtra());
		visitor.diffPrimitive("raw", original.getRaw(), updated.getRaw());
		this.diffEntityField("schema", original.getSchema(), updated.getSchema());
		visitor.diffPrimitive("examples", original.getExamples(), updated.getExamples());
		this.diffUnionField("defaultValue", original.getDefaultValue(), updated.getDefaultValue());
		visitor.diffPrimitive("title", original.getTitle(), updated.getTitle());
	}

	public void traverseSchema(Syn1Schema original, Syn1Schema updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("$ref", original.get$ref(), updated.get$ref());
		visitor.diffPrimitive("type", original.getType(), updated.getType());
		this.diffUnionField("items", original.getItems(), updated.getItems());
		this.diffMap("properties", original.getProperties(), updated.getProperties());
		this.diffList("allOf", original.getAllOf(), updated.getAllOf());
		this.diffMap("definitions", original.getDefinitions(), updated.getDefinitions());
		this.diffMap("nestedSchemas", original.getNestedSchemas(), updated.getNestedSchemas());
		this.diffList("composedSchemas", original.getComposedSchemas(), updated.getComposedSchemas());
		visitor.diffPrimitive("minLength", original.getMinLength(), updated.getMinLength());
		visitor.diffPrimitive("maxLength", original.getMaxLength(), updated.getMaxLength());
		visitor.diffPrimitive("enum", original.getEnum(), updated.getEnum());
	}

	public void traversePaths(Syn1Paths original, Syn1Paths updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
	}

	public void traversePathItem(Syn1PathItem original, Syn1PathItem updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("$ref", original.get$ref(), updated.get$ref());
		visitor.diffPrimitive("summary", original.getSummary(), updated.getSummary());
		this.diffEntityField("get", original.getGet(), updated.getGet());
		this.diffEntityField("put", original.getPut(), updated.getPut());
		this.diffEntityField("post", original.getPost(), updated.getPost());
	}

	public void traverseOperation(Syn1Operation original, Syn1Operation updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("operationId", original.getOperationId(), updated.getOperationId());
		visitor.diffPrimitive("summary", original.getSummary(), updated.getSummary());
		visitor.diffPrimitive("tags", original.getTags(), updated.getTags());
		this.diffList("parameters", original.getParameters(), updated.getParameters());
	}
}