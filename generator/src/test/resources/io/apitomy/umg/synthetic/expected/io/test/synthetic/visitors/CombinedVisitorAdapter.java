package io.test.synthetic.visitors;

import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;
import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.v2.visitors.Syn2Visitor;

public class CombinedVisitorAdapter implements Syn2Visitor, Syn1Visitor {

	@Override
	public void visitPaths(SynPaths node) {
	}

	@Override
	public boolean beforeVisitPaths(SynPaths node) {
		return true;
	}

	@Override
	public void afterVisitPaths(SynPaths node) {
	}

	@Override
	public void visitOperation(SynOperation node) {
	}

	@Override
	public boolean beforeVisitOperation(SynOperation node) {
		return true;
	}

	@Override
	public void afterVisitOperation(SynOperation node) {
	}

	@Override
	public void visitSchema(SynSchema node) {
	}

	@Override
	public boolean beforeVisitSchema(SynSchema node) {
		return true;
	}

	@Override
	public void afterVisitSchema(SynSchema node) {
	}

	@Override
	public void visitInfo(SynInfo node) {
	}

	@Override
	public boolean beforeVisitInfo(SynInfo node) {
		return true;
	}

	@Override
	public void afterVisitInfo(SynInfo node) {
	}

	@Override
	public void visitPathItem(SynPathItem node) {
	}

	@Override
	public boolean beforeVisitPathItem(SynPathItem node) {
		return true;
	}

	@Override
	public void afterVisitPathItem(SynPathItem node) {
	}

	@Override
	public void visitDocument(SynDocument node) {
	}

	@Override
	public boolean beforeVisitDocument(SynDocument node) {
		return true;
	}

	@Override
	public void afterVisitDocument(SynDocument node) {
	}

	@Override
	public void visitContact(SynContact node) {
	}

	@Override
	public boolean beforeVisitContact(SynContact node) {
		return true;
	}

	@Override
	public void afterVisitContact(SynContact node) {
	}

	@Override
	public void visitItem(SynItem node) {
	}

	@Override
	public boolean beforeVisitItem(SynItem node) {
		return true;
	}

	@Override
	public void afterVisitItem(SynItem node) {
	}
}