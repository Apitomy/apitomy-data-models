package io.apitomy.datamodels.deref;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.refs.ReferenceContext;

public class SetContextVisitor extends AllReferenceableNodeVisitor {

    private final ReferenceContext context;

    public SetContextVisitor(ReferenceContext context) {
        this.context = context;
    }

    @Override
    protected void visitReferenceableNode(Referenceable refNode) {
        Node node = (Node) refNode;
        node.setNodeAttribute(DereferenceConstants.KEY_REFERENCE_CONTEXT, context);
    }

}
