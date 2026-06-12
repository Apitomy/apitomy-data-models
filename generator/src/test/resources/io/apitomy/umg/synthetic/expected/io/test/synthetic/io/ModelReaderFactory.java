package io.test.synthetic.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.ModelType;
import io.test.synthetic.v1.io.Syn1ModelReader;
import io.test.synthetic.v1.io.Syn1ModelReaderDispatcher;
import io.test.synthetic.v2.io.Syn2ModelReader;
import io.test.synthetic.v2.io.Syn2ModelReaderDispatcher;
import io.test.synthetic.visitors.Visitor;

public class ModelReaderFactory {

	public static ModelReader createModelReader(ModelType modelType) {
		ModelReader reader = null;
		switch (modelType) {
			case SYN1 :
				reader = new Syn1ModelReader();
				break;
			case SYN2 :
				reader = new Syn2ModelReader();
				break;
		}
		return reader;
	}

	public static Visitor createModelReaderDispatcher(ModelType modelType, ObjectNode json) {
		ModelReader reader = ModelReaderFactory.createModelReader(modelType);
		Visitor visitor = null;
		switch (modelType) {
			case SYN1 :
				visitor = new Syn1ModelReaderDispatcher(json, (Syn1ModelReader) reader);
				break;
			case SYN2 :
				visitor = new Syn2ModelReaderDispatcher(json, (Syn2ModelReader) reader);
				break;
		}
		return visitor;
	}
}