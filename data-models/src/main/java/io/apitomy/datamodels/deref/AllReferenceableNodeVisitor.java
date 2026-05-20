package io.apitomy.datamodels.deref;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;

/**
 * Simple visitor used to visit {@link Referenceable} nodes.
 * @author eric.wittmann@gmail.com
 */
public abstract class AllReferenceableNodeVisitor extends AllNodeVisitor {

    @Override
    protected void visitNode(Node node) {
        if (node instanceof Referenceable) {
            Referenceable refNode = (Referenceable) node;
            String ref = refNode.get$ref();
            if (ref != null) {
                visitReferenceableNode(refNode);
            }
        }
    }

    // TODO how to include asyncapi message payloads here?
    // TODO support openapi operationRef, which is a $ref with a different name, on the openapi Link object.

    protected abstract void visitReferenceableNode(Referenceable refNode);

}
