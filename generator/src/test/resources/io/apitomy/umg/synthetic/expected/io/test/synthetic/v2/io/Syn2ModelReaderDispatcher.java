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

public class Syn2ModelReaderDispatcher implements Syn2Visitor {

	private final ObjectNode json;
	private final Syn2ModelReader reader;

	public Syn2ModelReaderDispatcher(ObjectNode json, Syn2ModelReader reader) {
		this.json = json;
		this.reader = reader;
	}

	@Override
	public void visitPaths(SynPaths node) {
		this.reader.readPaths(this.json, (Syn2Paths) node);
	}

	@Override
	public void visitOperation(SynOperation node) {
		this.reader.readOperation(this.json, (Syn2Operation) node);
	}

	@Override
	public void visitSchema(SynSchema node) {
		this.reader.readSchema(this.json, (Syn2Schema) node);
	}

	@Override
	public void visitInfo(SynInfo node) {
		this.reader.readInfo(this.json, (Syn2Info) node);
	}

	@Override
	public void visitPathItem(SynPathItem node) {
		this.reader.readPathItem(this.json, (Syn2PathItem) node);
	}

	@Override
	public void visitDocument(SynDocument node) {
		this.reader.readDocument(this.json, (Syn2Document) node);
	}

	@Override
	public void visitContact(SynContact node) {
		this.reader.readContact(this.json, (Syn2Contact) node);
	}

	@Override
	public void visitItem(SynItem node) {
		this.reader.readItem(this.json, (Syn2Item) node);
	}
}