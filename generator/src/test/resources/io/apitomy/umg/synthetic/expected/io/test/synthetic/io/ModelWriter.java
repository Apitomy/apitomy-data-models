package io.test.synthetic.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.RootNode;

public interface ModelWriter {

	public ObjectNode writeRoot(RootNode node);

}
