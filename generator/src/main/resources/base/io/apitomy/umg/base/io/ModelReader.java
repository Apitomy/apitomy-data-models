package io.apitomy.umg.base.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.umg.base.RootCapable;

public interface ModelReader {

    public RootCapable readRoot(JsonNode json);

}
