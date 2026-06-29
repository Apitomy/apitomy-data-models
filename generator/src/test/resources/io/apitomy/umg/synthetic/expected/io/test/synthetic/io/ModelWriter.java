package io.test.synthetic.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.RootCapable;

public interface ModelWriter {

	public JsonNode writeRoot(RootCapable node);

}
