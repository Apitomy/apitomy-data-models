package io.test.synthetic.v1.io;

import io.test.synthetic.Node;
import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;
import io.test.synthetic.io.ModelCloner;
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import io.test.synthetic.v1.visitors.Syn1Visitor;

public class Syn1ModelClonerDispatcher implements Syn1Visitor, ModelCloner {

	private final Syn1ModelCloner cloner;
	private Node clonedNode;

	public Syn1ModelClonerDispatcher(Syn1ModelCloner cloner) {
		this.cloner = cloner;
	}

	@Override
	public Node cloneNode(Node source) {
		this.clonedNode = source.emptyClone();
		source.accept(this);
		return this.clonedNode;
	}

	@Override
	public void visitPaths(SynPaths node) {
		this.cloner.clonePaths((Syn1Paths) node, (Syn1Paths) this.clonedNode);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.cloner.cloneOperation((Syn1Operation) node, (Syn1Operation) this.clonedNode);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.cloner.cloneSchema((Syn1Schema) node, (Syn1Schema) this.clonedNode);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.cloner.cloneInfo((Syn1Info) node, (Syn1Info) this.clonedNode);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.cloner.clonePathItem((Syn1PathItem) node, (Syn1PathItem) this.clonedNode);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.cloner.cloneDocument((Syn1Document) node, (Syn1Document) this.clonedNode);
	}

	@Override
	public void visitContact(SynContact node) {
		this.cloner.cloneContact((Syn1Contact) node, (Syn1Contact) this.clonedNode);
	}

	@Override
	public void visitItem(SynItem node) {
		this.cloner.cloneItem((Syn1Item) node, (Syn1Item) this.clonedNode);
	}
}