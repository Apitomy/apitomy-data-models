/*
 * Copyright 2022 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.datamodels;

import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.asyncapi.v2x.v20.visitors.AsyncApi20Traverser;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.visitors.AsyncApi21Traverser;
import io.apitomy.datamodels.models.asyncapi.v2x.v22.visitors.AsyncApi22Traverser;
import io.apitomy.datamodels.models.asyncapi.v2x.v23.visitors.AsyncApi23Traverser;
import io.apitomy.datamodels.models.asyncapi.v2x.v24.visitors.AsyncApi24Traverser;
import io.apitomy.datamodels.models.asyncapi.v2x.v25.visitors.AsyncApi25Traverser;
import io.apitomy.datamodels.models.asyncapi.v2x.v26.visitors.AsyncApi26Traverser;
import io.apitomy.datamodels.models.asyncapi.v3x.v30.visitors.AsyncApi30Traverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.visitors.JSDraft4Traverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.visitors.JSDraft6Traverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.visitors.JSDraft7Traverser;
import io.apitomy.datamodels.models.jsonschema.modern.v201909.visitors.JS201909Traverser;
import io.apitomy.datamodels.models.jsonschema.modern.v202012.visitors.JS202012Traverser;
import io.apitomy.datamodels.models.openapi.v2x.v20.visitors.OpenApi20Traverser;
import io.apitomy.datamodels.models.openapi.v3x.v30.visitors.OpenApi30Traverser;
import io.apitomy.datamodels.models.openapi.v3x.v31.visitors.OpenApi31Traverser;
import io.apitomy.datamodels.models.openapi.v3x.v32.visitors.OpenApi32Traverser;
import io.apitomy.datamodels.models.openrpc.v1x.v13.visitors.OpenRpc13Traverser;
import io.apitomy.datamodels.models.openrpc.v1x.v14.visitors.OpenRpc14Traverser;
import io.apitomy.datamodels.models.union.Union;
import io.apitomy.datamodels.models.visitors.ReverseTraverser;
import io.apitomy.datamodels.models.visitors.Traverser;
import io.apitomy.datamodels.models.visitors.Visitor;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathSegment;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * @author eric.wittmann@gmail.com
 */
public class VisitorUtil {

    public static void visitTree(Node node, Visitor visitor, TraverserDirection direction) {
        ModelType type = node.root().modelType();
        Traverser traverser = null;
        if (direction == TraverserDirection.up) {
            traverser = new ReverseTraverser(visitor);
        } else {
            switch (type) {
                case ASYNCAPI20:
                    traverser = new AsyncApi20Traverser(visitor);
                    break;
                case ASYNCAPI21:
                    traverser = new AsyncApi21Traverser(visitor);
                    break;
                case ASYNCAPI22:
                    traverser = new AsyncApi22Traverser(visitor);
                    break;
                case ASYNCAPI23:
                    traverser = new AsyncApi23Traverser(visitor);
                    break;
                case ASYNCAPI24:
                    traverser = new AsyncApi24Traverser(visitor);
                    break;
                case ASYNCAPI25:
                    traverser = new AsyncApi25Traverser(visitor);
                    break;
                case ASYNCAPI26:
                    traverser = new AsyncApi26Traverser(visitor);
                    break;
                case ASYNCAPI30:
                    traverser = new AsyncApi30Traverser(visitor);
                    break;
                case OPENAPI20:
                    traverser = new OpenApi20Traverser(visitor);
                    break;
                case OPENAPI30:
                    traverser = new OpenApi30Traverser(visitor);
                    break;
                case OPENAPI31:
                    traverser = new OpenApi31Traverser(visitor);
                    break;
                case OPENAPI32:
                    traverser = new OpenApi32Traverser(visitor);
                    break;
                case OPENRPC13:
                    traverser = new OpenRpc13Traverser(visitor);
                    break;
                case OPENRPC14:
                    traverser = new OpenRpc14Traverser(visitor);
                    break;
                case JSDRAFT4:
                    traverser = new JSDraft4Traverser(visitor);
                    break;
                case JSDRAFT6:
                    traverser = new JSDraft6Traverser(visitor);
                    break;
                case JSDRAFT7:
                    traverser = new JSDraft7Traverser(visitor);
                    break;
                case JS201909:
                    traverser = new JS201909Traverser(visitor);
                    break;
                case JS202012:
                    traverser = new JS202012Traverser(visitor);
                    break;
            }
        }
        if (traverser == null) {
            throw new RuntimeException("Traverser not found for type: " + type);
        }
        traverser.traverse(node);
    }

    /**
     * Visits nodes along a specific path in the data model tree. Starting from the
     * given node, this method traverses the tree following the path segments defined
     * in the NodePath, visiting each node along the way with the provided visitor.
     * If a node along the path does not exist, traversal stops at the last reachable node.
     *
     * @param node The starting node (typically the root document)
     * @param nodePath The path to traverse through the data model
     * @param visitor The visitor to apply to each node along the path
     */
    @SuppressWarnings("rawtypes")
    public static void visitPath(Node node, NodePath nodePath, Visitor visitor) {
        List<NodePathSegment> segments = nodePath.getSegments();
        Object current = node;

        // Visit the starting node
        if (NodeUtil.isNode(current)) {
            ((Node) current).accept(visitor);
        }

        // Traverse each segment of the path
        for (NodePathSegment segment : segments) {
            if (current == null) {
                // Can't traverse further if current is null
                break;
            }

            try {
                if (!segment.isIndex()) {
                    // Property access (e.g., /info, /paths)
                    current = NodeUtil.getProperty(current, segment.getValue());
                } else {
                    // Index access (e.g., [0], [petId])
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
                        if (index >= 0 && index < list.size()) {
                            current = list.get(index);
                        } else {
                            // Index out of bounds, stop traversal
                            break;
                        }
                    } else if (NodeUtil.isMap(current)) {
                        Map map = (Map) current;
                        current = NodeUtil.getMapItem(map, segment.getValue());
                    }
                }

                // If we couldn't resolve this segment, stop traversal
                if (current == null) {
                    break;
                }

                // Visit the node at this segment if it's a Node
                if (NodeUtil.isNode(current)) {
                    ((Node) current).accept(visitor);
                }
            } catch (Exception e) {
                // If any error occurs during traversal, stop gracefully
                break;
            }
        }
    }

}
