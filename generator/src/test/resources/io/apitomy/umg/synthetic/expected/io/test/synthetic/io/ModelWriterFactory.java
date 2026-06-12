package io.test.synthetic.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.ModelType;
import io.test.synthetic.v1.io.Syn1ModelWriter;
import io.test.synthetic.v1.io.Syn1ModelWriterDispatcher;
import io.test.synthetic.v2.io.Syn2ModelWriter;
import io.test.synthetic.v2.io.Syn2ModelWriterDispatcher;
import io.test.synthetic.visitors.Visitor;

public class ModelWriterFactory {

	public static ModelWriter createModelWriter(ModelType modelType) {
		ModelWriter writer = null;
		switch (modelType) {
			case SYN1 :
				writer = new Syn1ModelWriter();
				break;
			case SYN2 :
				writer = new Syn2ModelWriter();
				break;
		}
		return writer;
	}

	public static Visitor createModelWriterDispatcher(ModelType modelType, ObjectNode json) {
		ModelWriter writer = ModelWriterFactory.createModelWriter(modelType);
		Visitor visitor = null;
		switch (modelType) {
			case SYN1 :
				visitor = new Syn1ModelWriterDispatcher(json, (Syn1ModelWriter) writer);
				break;
			case SYN2 :
				visitor = new Syn2ModelWriterDispatcher(json, (Syn2ModelWriter) writer);
				break;
		}
		return visitor;
	}
}