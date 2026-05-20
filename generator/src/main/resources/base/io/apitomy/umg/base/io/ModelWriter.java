package io.apitomy.umg.base.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.umg.base.RootNode;

public interface ModelWriter {

    public ObjectNode writeRoot(RootNode node);

}
