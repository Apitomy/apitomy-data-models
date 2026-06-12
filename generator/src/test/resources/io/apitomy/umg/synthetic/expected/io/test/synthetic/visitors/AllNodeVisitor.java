package io.test.synthetic.visitors;

import io.test.synthetic.Node;
import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;

public abstract class AllNodeVisitor implements CombinedVisitor {

	protected abstract void visitNode(Node node);

	@Override
	public void visitPaths(SynPaths node) {
		this.visitNode(node);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.visitNode(node);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.visitNode(node);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.visitNode(node);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.visitNode(node);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.visitNode(node);
	}

	@Override
	public void visitContact(SynContact node) {
		this.visitNode(node);
	}

	@Override
	public void visitItem(SynItem node) {
		this.visitNode(node);
	}
}