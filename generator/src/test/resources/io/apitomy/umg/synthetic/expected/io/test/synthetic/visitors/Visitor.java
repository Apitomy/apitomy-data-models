package io.test.synthetic.visitors;

import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;

public interface Visitor {

	public void visitPaths(SynPaths node);

	public boolean beforeVisitPaths(SynPaths node);

	public void afterVisitPaths(SynPaths node);

	public void visitOperation(SynOperation node);

	public boolean beforeVisitOperation(SynOperation node);

	public void afterVisitOperation(SynOperation node);

	public void visitSchema(SynSchema node);

	public boolean beforeVisitSchema(SynSchema node);

	public void afterVisitSchema(SynSchema node);

	public void visitInfo(SynInfo node);

	public boolean beforeVisitInfo(SynInfo node);

	public void afterVisitInfo(SynInfo node);

	public void visitPathItem(SynPathItem node);

	public boolean beforeVisitPathItem(SynPathItem node);

	public void afterVisitPathItem(SynPathItem node);

	public void visitDocument(SynDocument node);

	public boolean beforeVisitDocument(SynDocument node);

	public void afterVisitDocument(SynDocument node);

	public void visitContact(SynContact node);

	public boolean beforeVisitContact(SynContact node);

	public void afterVisitContact(SynContact node);

	public void visitItem(SynItem node);

	public boolean beforeVisitItem(SynItem node);

	public void afterVisitItem(SynItem node);
}