package io.apitomy.datamodels.paths;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.union.ListUnionValue;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.List;

public class NodePathCreationVisitor extends AllNodeVisitor {
    protected NodePath path = new NodePath();

    @Override
    public void visitDocument(Document node) {
    }

    @Override
    protected void visitNode(Node node) {
        switch (node.parentPropertyType()) {
            case standard:
                path.prepend(new NodePathSegment(node.parentPropertyName(), false));
                return;
            case map:
                path.prepend(new NodePathSegment(node.mapPropertyName(), true));
                if (node.parentPropertyName() != null) {
                    path.prepend(new NodePathSegment(node.parentPropertyName(), false));
                }
                return;
            case array:
                Object parentPropertyValue = NodeUtil.getNodeProperty(node.parent(), node.parentPropertyName());
                if (parentPropertyValue != null) {
                    if (parentPropertyValue instanceof List) {
                        List<?> nodes = (List<?>) parentPropertyValue;
                        int idx = nodes.indexOf(node);
                        path.prepend(new NodePathSegment("" + idx, true));
                        path.prepend(new NodePathSegment(node.parentPropertyName(), false));
                    } else if (parentPropertyValue instanceof ListUnionValue) {
                        List<?> nodes = (List<?>) ((ListUnionValue) parentPropertyValue).getValue();
                        int idx = nodes.indexOf(node);
                        path.prepend(new NodePathSegment("" + idx, true));
                        path.prepend(new NodePathSegment(node.parentPropertyName(), false));
                    }
                }
        }
    }

}
