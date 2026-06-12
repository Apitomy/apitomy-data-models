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

	public void visitOperation(SynOperation node);

	public void visitSchema(SynSchema node);

	public void visitInfo(SynInfo node);

	public void visitPathItem(SynPathItem node);

	public void visitDocument(SynDocument node);

	public void visitContact(SynContact node);

	public void visitItem(SynItem node);
}