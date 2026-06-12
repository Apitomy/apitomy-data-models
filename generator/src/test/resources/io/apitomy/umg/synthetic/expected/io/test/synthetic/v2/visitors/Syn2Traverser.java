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
		node.accept(this.visitor);
		Syn2Paths model = (Syn2Paths) node;
		this.traverseMappedNode(model);
	}

	@Override
	public void visitOperation(SynOperation node) {
		node.accept(this.visitor);
		Syn2Operation model = (Syn2Operation) node;
		this.traverseList("parameters", model.getParameters());
	}

	@Override
	public void visitSchema(SynSchema node) {
		node.accept(this.visitor);
		Syn2Schema model = (Syn2Schema) node;
		this.traverseUnion("items", model.getItems());
	}

	@Override
	public void visitInfo(SynInfo node) {
		node.accept(this.visitor);
		Syn2Info model = (Syn2Info) node;
		this.traverseNode("contact", model.getContact());
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		node.accept(this.visitor);
		Syn2PathItem model = (Syn2PathItem) node;
		this.traverseNode("get", model.getGet());
		this.traverseNode("put", model.getPut());
		this.traverseNode("post", model.getPost());
		this.traverseNode("delete", model.getDelete());
	}

	@Override
	public void visitDocument(SynDocument node) {
		node.accept(this.visitor);
		Syn2Document model = (Syn2Document) node;
		this.traverseNode("info", model.getInfo());
		this.traverseList("items", model.getItems());
		this.traverseMap("webhooks", model.getWebhooks());
		this.traverseNode("additionalSchema", model.getAdditionalSchema());
	}

	@Override
	public void visitContact(SynContact node) {
		node.accept(this.visitor);
	}

	@Override
	public void visitItem(SynItem node) {
		node.accept(this.visitor);
		Syn2Item model = (Syn2Item) node;
		this.traverseNode("schema", model.getSchema());
		this.traverseUnion("defaultValue", model.getDefaultValue());
	}
}