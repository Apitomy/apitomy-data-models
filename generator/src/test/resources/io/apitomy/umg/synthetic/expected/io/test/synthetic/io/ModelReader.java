package io.test.synthetic.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.RootCapable;

public interface ModelReader {

	public RootCapable readRoot(JsonNode json);

}
