package io.apitomy.umg.base.visitors;

import io.apitomy.umg.base.Any;

/**
 * All data model traversers must implement this interface.
 */
public interface Traverser {

    /**
     * Traverse a data model starting at the given value (entity or union).
     * @param value
     */
    public void traverse(Any value);

}
