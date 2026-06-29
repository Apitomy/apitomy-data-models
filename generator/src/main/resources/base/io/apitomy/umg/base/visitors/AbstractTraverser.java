package io.apitomy.umg.base.visitors;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.apitomy.umg.base.Any;
import io.apitomy.umg.base.MappedNode;
import io.apitomy.umg.base.Node;


import io.apitomy.umg.base.union.EntityListUnionValue;
import io.apitomy.umg.base.union.EntityMapUnionValue;
import io.apitomy.umg.base.union.Union;
import io.apitomy.umg.base.util.JsonUtil;

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
    protected void doTraverseNode(Node node) {
        node.accept(this);
    }

    /**
     * Traverse into the given node, unless it's null.
     *
     * @param propertyName
     * @param node
     */
    protected void traverseNode(String propertyName, Node node) {
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
    protected void traverseList(String propertyName, Collection<? extends Any> items) {
        if (items != null) {
            int index = 0;
            traversalContext.pushProperty(propertyName);
            List<? extends Any> clonedItems = JsonUtil.collectionToList(items);
            for (Any item : clonedItems) {
                if (item != null && item.isNode()) {
                    traversalContext.pushListIndex(index);
                    doTraverseNode((Node) item);
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
    protected void traverseMap(String propertyName, Map<String, ? extends Any> items) {
        if (items != null) {
            traversalContext.pushProperty(propertyName);
            List<String> keys = JsonUtil.collectionToList(items.keySet());
            for (int _idx = 0; _idx < keys.size(); _idx++) {
                String key = keys.get(_idx);
                Any value = items.get(key);
                if (value != null && value.isNode()) {
                    this.traversalContext.pushMapIndex(key);
                    this.doTraverseNode((Node) value);
                    this.traversalContext.pop();
                }
            }
            this.traversalContext.pop();
        }
    }

    /**
     * Traverse the items of the given mapped node.
     *
     * @param items
     */
    protected void traverseMappedNode(MappedNode<? extends Node> mappedNode) {
        if (mappedNode != null) {
            List<String> names = JsonUtil.collectionToList(mappedNode.getItemNames());
            for (int _idx = 0; _idx < names.size(); _idx++) {
                String name = names.get(_idx);
                Node value = mappedNode.getItem(name);
                if (value != null) {
                    this.traversalContext.pushMapIndex(name);
                    this.doTraverseNode(value);
                    this.traversalContext.pop();
                }
            }
        }
    }

    /**
     * Traverse a union property.  Traversal of a union property only needs to happen if
     * the value of the union is an entity or an entity collection.
     * @param propertyName
     * @param union
     */
    @SuppressWarnings("unchecked")
    protected void traverseUnion(String propertyName, Union union) {
        if (union != null) {
            if (union.isEntity()) {
                this.traverseNode(propertyName, (Node) union);
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
     * Called to traverse the data model starting at the given value and traversing
     * down until this value and all child nodes have been visited.
     * Accepts both {@link Node} (entity) and {@link Union} (union value) roots.
     *
     * @param value
     */
    @Override
    public void traverse(Any value) {
        if (value instanceof Node) {
            doTraverseNode((Node) value);
        } else if (value instanceof Union) {
            traverseUnion(null, (Union) value);
        }
    }

}
