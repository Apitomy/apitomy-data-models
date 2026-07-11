package io.apitomy.umg.base.visitors;

import io.apitomy.umg.base.Node;

/**
 * All data model traversers must implement this interface.
 */
public interface Traverser {
    
    /**
     * Traverse a single node in a data model.
     * @param node
     */
    public void traverse(Node node);

}
