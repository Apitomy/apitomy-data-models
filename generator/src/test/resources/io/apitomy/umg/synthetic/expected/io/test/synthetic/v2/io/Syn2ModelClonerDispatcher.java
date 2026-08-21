package io.test.synthetic.v2.io;

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
import io.test.synthetic.v2.Syn2Contact;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import io.test.synthetic.v2.visitors.Syn2Visitor;

public class Syn2ModelClonerDispatcher implements Syn2Visitor, ModelCloner {

	private final Syn2ModelCloner cloner;
	private Node clonedNode;

	public Syn2ModelClonerDispatcher(Syn2ModelCloner cloner) {
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
		this.cloner.clonePaths((Syn2Paths) node, (Syn2Paths) this.clonedNode);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.cloner.cloneOperation((Syn2Operation) node, (Syn2Operation) this.clonedNode);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.cloner.cloneSchema((Syn2Schema) node, (Syn2Schema) this.clonedNode);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.cloner.cloneInfo((Syn2Info) node, (Syn2Info) this.clonedNode);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.cloner.clonePathItem((Syn2PathItem) node, (Syn2PathItem) this.clonedNode);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.cloner.cloneDocument((Syn2Document) node, (Syn2Document) this.clonedNode);
	}

	@Override
	public void visitContact(SynContact node) {
		this.cloner.cloneContact((Syn2Contact) node, (Syn2Contact) this.clonedNode);
	}

	@Override
	public void visitItem(SynItem node) {
		this.cloner.cloneItem((Syn2Item) node, (Syn2Item) this.clonedNode);
	}

	@Override
	public void afterVisitPaths(SynPaths node) {
	}

	@Override
	public void afterVisitOperation(SynOperation node) {
	}

	@Override
	public void afterVisitSchema(SynSchema node) {
	}

	@Override
	public void afterVisitInfo(SynInfo node) {
	}

	@Override
	public void afterVisitPathItem(SynPathItem node) {
	}

	@Override
	public void afterVisitDocument(SynDocument node) {
	}

	@Override
	public void afterVisitContact(SynContact node) {
	}

	@Override
	public void afterVisitItem(SynItem node) {
	}
}