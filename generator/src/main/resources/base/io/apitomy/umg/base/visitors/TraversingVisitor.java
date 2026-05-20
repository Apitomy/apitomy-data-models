package io.apitomy.umg.base.visitors;

/**
 * Any visitor that cares about traversal context can implement this interface
 * (in addition to the standard Visitor interface).  When this interface is 
 * implemented, the standard traverser will share the traverser context with
 * the visitor.  This will allow the visitor to have some context about where
 * it is in the traversal of the model.
 */
public interface TraversingVisitor {
    
    public void setTraversalContext(TraversalContext context);

}
