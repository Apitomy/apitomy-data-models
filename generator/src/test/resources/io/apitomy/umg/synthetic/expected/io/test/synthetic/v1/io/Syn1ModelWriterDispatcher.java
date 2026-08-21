package io.test.synthetic.v1.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.SynContact;
import io.test.synthetic.SynDocument;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.SynPaths;
import io.test.synthetic.SynSchema;
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import io.test.synthetic.v1.visitors.Syn1Visitor;

public class Syn1ModelWriterDispatcher implements Syn1Visitor {

	private final ObjectNode json;
	private final Syn1ModelWriter writer;

	public Syn1ModelWriterDispatcher(ObjectNode json, Syn1ModelWriter writer) {
		this.json = json;
		this.writer = writer;
	}

	@Override
	public void visitPaths(SynPaths node) {
		this.writer.writePaths((Syn1Paths) node, this.json);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.writer.writeOperation((Syn1Operation) node, this.json);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.writer.writeSchema((Syn1Schema) node, this.json);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.writer.writeInfo((Syn1Info) node, this.json);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.writer.writePathItem((Syn1PathItem) node, this.json);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.writer.writeDocument((Syn1Document) node, this.json);
	}

	@Override
	public void visitContact(SynContact node) {
		this.writer.writeContact((Syn1Contact) node, this.json);
	}

	@Override
	public void visitItem(SynItem node) {
		this.writer.writeItem((Syn1Item) node, this.json);
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