package io.apitomy.datamodels.deref;

import java.util.LinkedList;
import java.util.List;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;

/**
 * Visitor used to find nodes that have a $ref that has not yet been resolved.
 *
 * @author eric.wittmann@gmail.com
 */
public class UnresolvedReferenceNodeVisitor extends AllReferenceableNodeVisitor {

    private List<Node> nodesWithUnresolvedRefs = new LinkedList<>();

    @Override
    protected void visitReferenceableNode(Referenceable refNode) {
        if (!isResolved((Node) refNode)) {
            nodesWithUnresolvedRefs.add((Node) refNode);
        }
    }

    private boolean isResolved(Node refNode) {
        Object resolved = refNode.getNodeAttribute(DereferenceConstants.KEY_RESOLUTION);
        return DereferenceConstants.VALUE_RESOLVED.equals(resolved) ||
                DereferenceConstants.VALUE_UNRESOLVEABLE.equals(resolved);
    }

    public List<Node> getNodesWithUnresolvedRefs() {
        return this.nodesWithUnresolvedRefs;
    }

}
