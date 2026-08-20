package io.test.synthetic.v2.visitors;

import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import io.test.synthetic.visitors.AbstractTraverser;
import io.test.synthetic.visitors.Visitor;

public class Syn2Traverser extends AbstractTraverser implements Syn2Visitor {

	public Syn2Traverser(Visitor visitor) {
		super(visitor);
	}

	@Override
	public void visitPaths(SynPaths node) {
		if (((Syn2Visitor) this.visitor).beforeVisitPaths(node)) {
			node.accept(this.visitor);
			Syn2Paths model = (Syn2Paths) node;
			this.traverseMappedNode(model);
		}
		((Syn2Visitor) this.visitor).afterVisitPaths(node);
	}

	@Override
	public void visitOperation(SynOperation node) {
		if (((Syn2Visitor) this.visitor).beforeVisitOperation(node)) {
			node.accept(this.visitor);
			Syn2Operation model = (Syn2Operation) node;
			this.traverseList("parameters", model.getParameters());
		}
		((Syn2Visitor) this.visitor).afterVisitOperation(node);
	}

	@Override
	public void visitSchema(SynSchema node) {
		if (((Syn2Visitor) this.visitor).beforeVisitSchema(node)) {
			node.accept(this.visitor);
			Syn2Schema model = (Syn2Schema) node;
			this.traverseUnion("items", model.getItems());
			this.traverseMap("properties", model.getProperties());
			this.traverseList("allOf", model.getAllOf());
			this.traverseMap("definitions", model.getDefinitions());
			this.traverseMap("nestedSchemas", model.getNestedSchemas());
			this.traverseList("composedSchemas", model.getComposedSchemas());
		}
		((Syn2Visitor) this.visitor).afterVisitSchema(node);
	}

	@Override
	public void visitInfo(SynInfo node) {
		if (((Syn2Visitor) this.visitor).beforeVisitInfo(node)) {
			node.accept(this.visitor);
			Syn2Info model = (Syn2Info) node;
			this.traverseNode("contact", model.getContact());
		}
		((Syn2Visitor) this.visitor).afterVisitInfo(node);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		if (((Syn2Visitor) this.visitor).beforeVisitPathItem(node)) {
			node.accept(this.visitor);
			Syn2PathItem model = (Syn2PathItem) node;
			this.traverseNode("get", model.getGet());
			this.traverseNode("put", model.getPut());
			this.traverseNode("post", model.getPost());
			this.traverseNode("delete", model.getDelete());
		}
		((Syn2Visitor) this.visitor).afterVisitPathItem(node);
	}

	@Override
	public void visitDocument(SynDocument node) {
		if (((Syn2Visitor) this.visitor).beforeVisitDocument(node)) {
			node.accept(this.visitor);
			Syn2Document model = (Syn2Document) node;
			this.traverseNode("info", model.getInfo());
			this.traverseList("items", model.getItems());
			this.traverseMap("webhooks", model.getWebhooks());
			this.traverseUnion("additionalSchema", model.getAdditionalSchema());
		}
		((Syn2Visitor) this.visitor).afterVisitDocument(node);
	}

	@Override
	public void visitContact(SynContact node) {
		if (((Syn2Visitor) this.visitor).beforeVisitContact(node)) {
			node.accept(this.visitor);
		}
		((Syn2Visitor) this.visitor).afterVisitContact(node);
	}

	@Override
	public void visitItem(SynItem node) {
		if (((Syn2Visitor) this.visitor).beforeVisitItem(node)) {
			node.accept(this.visitor);
			Syn2Item model = (Syn2Item) node;
			this.traverseNode("schema", model.getSchema());
			this.traverseUnion("defaultValue", model.getDefaultValue());
		}
		((Syn2Visitor) this.visitor).afterVisitItem(node);
	}

	@Override
	public boolean beforeVisitPaths(SynPaths node) {
		return true;
	}

	@Override
	public void afterVisitPaths(SynPaths node) {
	}

	@Override
	public boolean beforeVisitOperation(SynOperation node) {
		return true;
	}

	@Override
	public void afterVisitOperation(SynOperation node) {
	}

	@Override
	public boolean beforeVisitSchema(SynSchema node) {
		return true;
	}

	@Override
	public void afterVisitSchema(SynSchema node) {
	}

	@Override
	public boolean beforeVisitInfo(SynInfo node) {
		return true;
	}

	@Override
	public void afterVisitInfo(SynInfo node) {
	}

	@Override
	public boolean beforeVisitPathItem(SynPathItem node) {
		return true;
	}

	@Override
	public void afterVisitPathItem(SynPathItem node) {
	}

	@Override
	public boolean beforeVisitDocument(SynDocument node) {
		return true;
	}

	@Override
	public void afterVisitDocument(SynDocument node) {
	}

	@Override
	public boolean beforeVisitContact(SynContact node) {
		return true;
	}

	@Override
	public void afterVisitContact(SynContact node) {
	}

	@Override
	public boolean beforeVisitItem(SynItem node) {
		return true;
	}

	@Override
	public void afterVisitItem(SynItem node) {
	}
}