package io.test.synthetic.visitors;

/**
 * Action that a visitor can request during traversal via the
 * {@link TraversalContext}. The traverser checks the action after calling each
 * visit method and acts accordingly.
 */
public enum TraversalAction {
	/**
	 * Continue with normal traversal — visit children recursively. This is the
	 * default if the visitor does not request an action.
	 */
	CONTINUE,

	/**
	 * Skip traversal of children for the current node.
	 */
	SKIP
}
