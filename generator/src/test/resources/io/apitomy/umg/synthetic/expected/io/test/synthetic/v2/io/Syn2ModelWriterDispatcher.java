package io.test.synthetic.v2.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;
import io.test.synthetic.v2.Syn2Contact;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import io.test.synthetic.v2.visitors.Syn2Visitor;

public class Syn2ModelWriterDispatcher implements Syn2Visitor {

	private final ObjectNode json;
	private final Syn2ModelWriter writer;

	public Syn2ModelWriterDispatcher(ObjectNode json, Syn2ModelWriter writer) {
		this.json = json;
		this.writer = writer;
	}

	@Override
	public void visitPaths(SynPaths node) {
		this.writer.writePaths((Syn2Paths) node, this.json);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.writer.writeOperation((Syn2Operation) node, this.json);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.writer.writeSchema((Syn2Schema) node, this.json);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.writer.writeInfo((Syn2Info) node, this.json);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.writer.writePathItem((Syn2PathItem) node, this.json);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.writer.writeDocument((Syn2Document) node, this.json);
	}

	@Override
	public void visitContact(SynContact node) {
		this.writer.writeContact((Syn2Contact) node, this.json);
	}

	@Override
	public void visitItem(SynItem node) {
		this.writer.writeItem((Syn2Item) node, this.json);
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