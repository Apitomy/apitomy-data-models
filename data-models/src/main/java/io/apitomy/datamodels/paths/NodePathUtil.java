package io.apitomy.datamodels.paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.union.Union;
import io.apitomy.datamodels.models.visitors.TraversalContext;
import io.apitomy.datamodels.models.visitors.TraversalStepType;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.util.RegexUtil;

public class NodePathUtil {

    public static NodePath createNodePath(Node node) {
        NodePathCreationVisitor visitor = new NodePathCreationVisitor();
        VisitorUtil.visitTree(node, visitor, TraverserDirection.up);
        return visitor.path;
    }

    public static NodePath parseNodePath(String path) {
        return NodePath.parse(path);
    }

    @SuppressWarnings("rawtypes")
    public static Node resolveNodePath(NodePath nodePath, Node from) {
        List<NodePathSegment> segments = nodePath.getSegments();
        Object current = from;
        for (NodePathSegment segment : segments) {
            if (!segment.isIndex()) {
                current = NodeUtil.getProperty(current, segment.getValue());
            } else {
                if (NodeUtil.isUnion(current)) {
                    Union union = (Union) current;
                    current = union.unionValue();
                }

                if (NodeUtil.isNode(current)) {
                    MappedNode mappedNode = (MappedNode) current;
                    current = mappedNode.getItem(segment.getValue());
                } else if (NodeUtil.isList(current)) {
                    int index = NodeUtil.toInteger(segment.getValue());
                    List list = (List) current;
                    current = list.get(index);
                } else if (NodeUtil.isMap(current)) {
                    Map map = (Map) current;
                    current = NodeUtil.getMapItem(map, segment.getValue());
                }
            }
        }
        if (NodeUtil.isNode(current)) {
            return (Node) current;
        } else {
            return null;
        }
    }

    public static List<String> detectPathParamNames(String path) {
        List<String> paramNames = new ArrayList<>();
        List<String[]> matches = RegexUtil.findMatches(path, "\\{([^\\}]+)\\}");
        for (String[] match : matches) {
            String name = match[1];
            paramNames.add(name.trim());
        }
        return paramNames;
    }

}
