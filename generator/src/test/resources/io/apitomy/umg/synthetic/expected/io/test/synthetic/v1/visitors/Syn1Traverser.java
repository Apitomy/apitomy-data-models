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
		node.accept(this.visitor);
		Syn1Paths model = (Syn1Paths) node;
		this.traverseMappedNode(model);
	}

	@Override
	public void visitOperation(SynOperation node) {
		node.accept(this.visitor);
		Syn1Operation model = (Syn1Operation) node;
		this.traverseList("parameters", model.getParameters());
	}

	@Override
	public void visitSchema(SynSchema node) {
		node.accept(this.visitor);
		Syn1Schema model = (Syn1Schema) node;
		this.traverseUnion("items", model.getItems());
	}

	@Override
	public void visitInfo(SynInfo node) {
		node.accept(this.visitor);
		Syn1Info model = (Syn1Info) node;
		this.traverseNode("contact", model.getContact());
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		node.accept(this.visitor);
		Syn1PathItem model = (Syn1PathItem) node;
		this.traverseNode("get", model.getGet());
		this.traverseNode("put", model.getPut());
		this.traverseNode("post", model.getPost());
	}

	@Override
	public void visitDocument(SynDocument node) {
		node.accept(this.visitor);
		Syn1Document model = (Syn1Document) node;
		this.traverseNode("info", model.getInfo());
		this.traverseList("items", model.getItems());
		this.traverseNode("additionalSchema", model.getAdditionalSchema());
	}

	@Override
	public void visitContact(SynContact node) {
		node.accept(this.visitor);
	}

	@Override
	public void visitItem(SynItem node) {
		node.accept(this.visitor);
		Syn1Item model = (Syn1Item) node;
		this.traverseNode("schema", model.getSchema());
		this.traverseUnion("defaultValue", model.getDefaultValue());
	}
}