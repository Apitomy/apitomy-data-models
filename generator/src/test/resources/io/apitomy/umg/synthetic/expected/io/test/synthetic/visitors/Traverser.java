package io.test.synthetic.visitors;

import io.test.synthetic.Node;

/**
 * All data model traversers must implement this interface.
 */
public interface Traverser {

	/**
	 * Traverse a single node in a data model.
	 * 
	 * @param node
	 */
	public void traverse(Node node);

}
