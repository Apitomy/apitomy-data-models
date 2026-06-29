package io.apitomy.umg.base.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.umg.base.RootCapable;

public interface ModelWriter {

    public JsonNode writeRoot(RootCapable node);

}
