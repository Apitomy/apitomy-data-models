package io.test.synthetic.v1.visitors;

import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import io.test.synthetic.visitors.AbstractTraverser;
import io.test.synthetic.visitors.Visitor;

public class Syn1Traverser extends AbstractTraverser implements Syn1Visitor {

	public Syn1Traverser(Visitor visitor) {
		super(visitor);
	}

	@Override
	public void visitPaths(SynPaths node) {
		if (((Syn1Visitor) this.visitor).beforeVisitPaths(node)) {
			node.accept(this.visitor);
			Syn1Paths model = (Syn1Paths) node;
			this.traverseMappedNode(model);
		}
		((Syn1Visitor) this.visitor).afterVisitPaths(node);
	}

	@Override
	public void visitOperation(SynOperation node) {
		if (((Syn1Visitor) this.visitor).beforeVisitOperation(node)) {
			node.accept(this.visitor);
			Syn1Operation model = (Syn1Operation) node;
			this.traverseList("parameters", model.getParameters());
		}
		((Syn1Visitor) this.visitor).afterVisitOperation(node);
	}

	@Override
	public void visitSchema(SynSchema node) {
		if (((Syn1Visitor) this.visitor).beforeVisitSchema(node)) {
			node.accept(this.visitor);
			Syn1Schema model = (Syn1Schema) node;
			this.traverseUnion("items", model.getItems());
			this.traverseMap("properties", model.getProperties());
			this.traverseList("allOf", model.getAllOf());
			this.traverseMap("definitions", model.getDefinitions());
			this.traverseMap("nestedSchemas", model.getNestedSchemas());
			this.traverseList("composedSchemas", model.getComposedSchemas());
		}
		((Syn1Visitor) this.visitor).afterVisitSchema(node);
	}

	@Override
	public void visitInfo(SynInfo node) {
		if (((Syn1Visitor) this.visitor).beforeVisitInfo(node)) {
			node.accept(this.visitor);
			Syn1Info model = (Syn1Info) node;
			this.traverseNode("contact", model.getContact());
		}
		((Syn1Visitor) this.visitor).afterVisitInfo(node);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		if (((Syn1Visitor) this.visitor).beforeVisitPathItem(node)) {
			node.accept(this.visitor);
			Syn1PathItem model = (Syn1PathItem) node;
			this.traverseNode("get", model.getGet());
			this.traverseNode("put", model.getPut());
			this.traverseNode("post", model.getPost());
		}
		((Syn1Visitor) this.visitor).afterVisitPathItem(node);
	}

	@Override
	public void visitDocument(SynDocument node) {
		if (((Syn1Visitor) this.visitor).beforeVisitDocument(node)) {
			node.accept(this.visitor);
			Syn1Document model = (Syn1Document) node;
			this.traverseNode("info", model.getInfo());
			this.traverseList("items", model.getItems());
			this.traverseUnion("additionalSchema", model.getAdditionalSchema());
		}
		((Syn1Visitor) this.visitor).afterVisitDocument(node);
	}

	@Override
	public void visitContact(SynContact node) {
		if (((Syn1Visitor) this.visitor).beforeVisitContact(node)) {
			node.accept(this.visitor);
		}
		((Syn1Visitor) this.visitor).afterVisitContact(node);
	}

	@Override
	public void visitItem(SynItem node) {
		if (((Syn1Visitor) this.visitor).beforeVisitItem(node)) {
			node.accept(this.visitor);
			Syn1Item model = (Syn1Item) node;
			this.traverseNode("schema", model.getSchema());
			this.traverseUnion("defaultValue", model.getDefaultValue());
		}
		((Syn1Visitor) this.visitor).afterVisitItem(node);
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