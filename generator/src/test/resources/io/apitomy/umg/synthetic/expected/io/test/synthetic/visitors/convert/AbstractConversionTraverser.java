package io.test.synthetic.visitors.convert;

import io.test.synthetic.Any;
import io.test.synthetic.visitors.TraversalContext;
import io.test.synthetic.visitors.TraversalContextImpl;

/**
 * Base class for generated conversion traversers. Converts a source tree into a
 * target tree by iterating source fields and delegating value mapping to a
 * ConversionVisitor.
 *
 * @param <V>
 *            the spec-version-specific ConversionVisitor subclass
 */
public class AbstractConversionTraverser<V> {

	protected final V visitor;
	protected final TraversalContextImpl context;

	protected AbstractConversionTraverser(V visitor) {
		this.visitor = visitor;
		this.context = new TraversalContextImpl();
	}

	public TraversalContext getContext() {
		return context;
	}

	protected void pushProperty(String propertyName) {
		context.pushProperty(propertyName);
	}

	protected void pushListIndex(int index) {
		context.pushListIndex(index);
	}

	protected void pushMapKey(String key) {
		context.pushMapIndex(key);
	}

	protected void pop() {
		context.pop();
	}

	/**
	 * Public entry point. Generated subclasses override with type-based dispatch.
	 */
	public Any convert(Any source) {
		return null;
	}
}
