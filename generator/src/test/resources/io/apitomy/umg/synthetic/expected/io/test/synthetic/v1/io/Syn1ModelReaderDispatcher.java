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

public class Syn1ModelReaderDispatcher implements Syn1Visitor {

	private final ObjectNode json;
	private final Syn1ModelReader reader;

	public Syn1ModelReaderDispatcher(ObjectNode json, Syn1ModelReader reader) {
		this.json = json;
		this.reader = reader;
	}

	@Override
	public void visitPaths(SynPaths node) {
		this.reader.readPaths(this.json, (Syn1Paths) node);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.reader.readOperation(this.json, (Syn1Operation) node);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.reader.readSchema(this.json, (Syn1Schema) node);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.reader.readInfo(this.json, (Syn1Info) node);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.reader.readPathItem(this.json, (Syn1PathItem) node);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.reader.readDocument(this.json, (Syn1Document) node);
	}

	@Override
	public void visitContact(SynContact node) {
		this.reader.readContact(this.json, (Syn1Contact) node);
	}

	@Override
	public void visitItem(SynItem node) {
		this.reader.readItem(this.json, (Syn1Item) node);
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