package io.test.synthetic.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.RootCapable;

public interface ModelWriter {

	public ObjectNode writeRoot(RootCapable node);

}
