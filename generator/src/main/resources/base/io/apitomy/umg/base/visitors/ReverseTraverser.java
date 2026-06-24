package io.apitomy.umg.base.visitors;

import io.apitomy.umg.base.Any;
import io.apitomy.umg.base.Node;
import io.apitomy.umg.base.Visitable;

public class ReverseTraverser extends AllNodeVisitor implements Traverser {

    protected Visitor visitor;

    public ReverseTraverser(Visitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void traverse(Any value) {
        if (value instanceof Visitable) {
            ((Visitable) value).accept(this);
        }
    }

    @Override
    protected void visitNode(Node node) {
        node.accept(this.visitor);

        if (node.parent() != null) {
            this.traverse(node.parent());
        }
    }
    
}
