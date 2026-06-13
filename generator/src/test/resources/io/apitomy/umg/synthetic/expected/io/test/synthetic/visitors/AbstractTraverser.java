package io.test.synthetic.visitors;

import io.test.synthetic.Any;
import io.test.synthetic.MappedNode;
import io.test.synthetic.Node;
import io.test.synthetic.Visitable;
import io.test.synthetic.union.EntityListUnionValue;
import io.test.synthetic.union.EntityMapUnionValue;
import io.test.synthetic.union.Union;
import io.test.synthetic.util.JsonUtil;
import java.util.Collection;
import java.util.Map;

/**
 * Base class for all traversers.
 */
public abstract class AbstractTraverser implements Traverser, Visitor {

	protected final Visitor visitor;
	protected final TraversalContextImpl traversalContext = new TraversalContextImpl();

	/**
	 * C'tor.
	 *
	 * @param visitor
	 */
	public AbstractTraverser(Visitor visitor) {
		this.visitor = visitor;
		if (visitor instanceof TraversingVisitor) {
			((TraversingVisitor) visitor).setTraversalContext(this.traversalContext);
		}
	}

	/**
	 * Traverse the given node. Guaranteed to not be null here.
	 *
	 * @param node
	 */
	protected void doTraverseNode(Visitable node) {
		node.accept(this);
	}

	/**
	 * Traverse into the given node, unless it's null.
	 *
	 * @param propertyName
	 * @param node
	 */
	protected void traverseNode(String propertyName, Visitable node) {
		if (node != null) {
			traversalContext.pushProperty(propertyName);
			doTraverseNode(node);
			traversalContext.pop();
		}
	}

	/**
	 * Traverse the items of the given array.
	 *
	 * @param propertyName
	 * @param items
	 */
	@SuppressWarnings("unchecked")
	protected void traverseList(String propertyName, Collection<? extends Any> items) {
		if (items != null) {
			int index = 0;
			traversalContext.pushProperty(propertyName);
			Collection<? extends Any> clonedItems = (Collection<? extends Any>) JsonUtil.cloneCollection(items);
			for (Any item : clonedItems) {
				if (item != null && item.isNode()) {
					traversalContext.pushListIndex(index);
					doTraverseNode((Visitable) item);
					traversalContext.pop();
				}
				index++;
			}
			traversalContext.pop();
		}
	}

	/**
	 * Traverse the items of the given map.
	 *
	 * @param propertyName
	 * @param items
	 */
	@SuppressWarnings("unchecked")
	protected void traverseMap(String propertyName, Map<String, ? extends Any> items) {
		if (items != null) {
			traversalContext.pushProperty(propertyName);
			Collection<String> keys = (Collection<String>) JsonUtil.cloneCollection(items.keySet());
			keys.forEach(key -> {
				Any value = items.get(key);
				if (value != null && value.isNode()) {
					this.traversalContext.pushMapIndex(key);
					this.doTraverseNode((Visitable) value);
					this.traversalContext.pop();
				}
			});
			this.traversalContext.pop();
		}
	}

	/**
	 * Traverse the items of the given mapped node.
	 *
	 * @param items
	 */
	@SuppressWarnings("unchecked")
	protected void traverseMappedNode(MappedNode<? extends Node> mappedNode) {
		if (mappedNode != null) {
			Collection<String> names = (Collection<String>) JsonUtil.cloneCollection(mappedNode.getItemNames());
			names.forEach(name -> {
				Node value = mappedNode.getItem(name);
				if (value != null) {
					this.traversalContext.pushMapIndex(name);
					this.doTraverseNode(value);
					this.traversalContext.pop();
				}
			});
		}
	}

	/**
	 * Traverse a union property. Traversal of a union property only needs to happen
	 * if the value of the union is an entity or an entity collection.
	 * 
	 * @param propertyName
	 * @param union
	 */
	@SuppressWarnings("unchecked")
	protected void traverseUnion(String propertyName, Union union) {
		if (union != null) {
			if (union.isEntity()) {
				this.traverseNode(propertyName, union);
			} else if (union.isEntityList()) {
				EntityListUnionValue<? extends Node> value = (EntityListUnionValue<? extends Node>) union;
				this.traverseList(propertyName, value.getValue());
			} else if (union.isEntityMap()) {
				EntityMapUnionValue<? extends Node> value = (EntityMapUnionValue<? extends Node>) union;
				this.traverseMap(propertyName, value.getValue());
			}
		}
	}

	/**
	 * Called to traverse the data model starting at the given node and traversing
	 * down until this node and all child nodes have been visited.
	 *
	 * @param node
	 */
	@Override
	public void traverse(Node node) {
		node.accept(this);
	}

}
