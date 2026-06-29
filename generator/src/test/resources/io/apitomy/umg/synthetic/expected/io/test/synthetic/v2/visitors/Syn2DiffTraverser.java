package io.test.synthetic.v2.visitors;

import io.test.synthetic.Node;
import io.test.synthetic.v2.Syn2Contact;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import io.test.synthetic.visitors.diff.AbstractDiffTraverser;
import io.test.synthetic.visitors.diff.DiffVisitor;

public class Syn2DiffTraverser extends AbstractDiffTraverser {

	public Syn2DiffTraverser(DiffVisitor visitor) {
		super(visitor);
	}

	@Override
	protected void traverseNode(Node original, Node updated) {
		Node target = original != null ? original : updated;
		if (target instanceof Syn2Document) {
			this.traverseDocument((Syn2Document) original, (Syn2Document) updated);
		} else if (target instanceof Syn2Info) {
			this.traverseInfo((Syn2Info) original, (Syn2Info) updated);
		} else if (target instanceof Syn2Contact) {
			this.traverseContact((Syn2Contact) original, (Syn2Contact) updated);
		} else if (target instanceof Syn2Item) {
			this.traverseItem((Syn2Item) original, (Syn2Item) updated);
		} else if (target instanceof Syn2Schema) {
			this.traverseSchema((Syn2Schema) original, (Syn2Schema) updated);
		} else if (target instanceof Syn2Paths) {
			this.traversePaths((Syn2Paths) original, (Syn2Paths) updated);
		} else if (target instanceof Syn2PathItem) {
			this.traversePathItem((Syn2PathItem) original, (Syn2PathItem) updated);
		} else if (target instanceof Syn2Operation) {
			this.traverseOperation((Syn2Operation) original, (Syn2Operation) updated);
		}
	}

	public void traverseDocument(Syn2Document original, Syn2Document updated) {
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
		this.diffMap("webhooks", original.getWebhooks(), updated.getWebhooks());
		this.diffUnionField("additionalSchema", original.getAdditionalSchema(), updated.getAdditionalSchema());
	}

	public void traverseInfo(Syn2Info original, Syn2Info updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPrimitive("name", original.getName(), updated.getName());
		this.diffEntityField("contact", original.getContact(), updated.getContact());
		visitor.diffPrimitive("version", original.getVersion(), updated.getVersion());
		visitor.diffPrimitive("license", original.getLicense(), updated.getLicense());
	}

	public void traverseContact(Syn2Contact original, Syn2Contact updated) {
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

	public void traverseItem(Syn2Item original, Syn2Item updated) {
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
		visitor.diffPrimitive("deprecated", original.isDeprecated(), updated.isDeprecated());
	}

	public void traverseSchema(Syn2Schema original, Syn2Schema updated) {
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

	public void traversePaths(Syn2Paths original, Syn2Paths updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitEntityPair(original, updated))
			return;
		if (original == null || updated == null)
			return;
	}

	public void traversePathItem(Syn2PathItem original, Syn2PathItem updated) {
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
		this.diffEntityField("delete", original.getDelete(), updated.getDelete());
	}

	public void traverseOperation(Syn2Operation original, Syn2Operation updated) {
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