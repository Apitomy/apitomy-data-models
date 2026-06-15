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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;
import io.apitomy.datamodels.paths.NodePath;

/**
 * @author eric.wittmann@gmail.com
 */
public class VisitorUtilTest {

    private static final String SAMPLE_OPENAPI = "{\n" +
            "  \"openapi\": \"3.0.1\",\n" +
            "  \"info\": {\n" +
            "    \"title\": \"Test API\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"contact\": {\n" +
            "      \"name\": \"Test Contact\",\n" +
            "      \"email\": \"test@example.com\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"paths\": {\n" +
            "    \"/pets\": {\n" +
            "      \"get\": {\n" +
            "        \"summary\": \"List all pets\",\n" +
            "        \"operationId\": \"listPets\",\n" +
            "        \"responses\": {\n" +
            "          \"200\": {\n" +
            "            \"description\": \"A list of pets\"\n" +
            "          },\n" +
            "          \"500\": {\n" +
            "            \"description\": \"Internal server error\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"/users\": {\n" +
            "      \"get\": {\n" +
            "        \"summary\": \"List all users\",\n" +
            "        \"operationId\": \"listUsers\",\n" +
            "        \"responses\": {\n" +
            "          \"200\": {\n" +
            "            \"description\": \"A list of users\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"tags\": [\n" +
            "    {\n" +
            "      \"name\": \"pets\",\n" +
            "      \"description\": \"Pet operations\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"users\",\n" +
            "      \"description\": \"User operations\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    /**
     * Test visitor that collects all visited nodes.
     */
    private static class NodeCollectorVisitor extends AllNodeVisitor {
        List<Node> visitedNodes = new ArrayList<>();

        @Override
        protected void visitNode(Node node) {
            visitedNodes.add(node);
        }
    }

    @Test
    public void testVisitTreeDown() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitTree(document, visitor, TraverserDirection.down);

        Assertions.assertNotNull(visitor.visitedNodes);
        Assertions.assertTrue(visitor.visitedNodes.size() > 10, "Should visit multiple nodes");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be the document");
    }

    @Test
    public void testVisitTreeUp() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        Info info = document.getInfo();
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitTree(info, visitor, TraverserDirection.up);

        Assertions.assertNotNull(visitor.visitedNodes);
        Assertions.assertTrue(visitor.visitedNodes.size() >= 2, "Should visit at least 2 nodes");
    }

    @Test
    public void testVisitPath_RootOnly() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(1, visitor.visitedNodes.size(), "Should visit only the document");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "Should visit the document");
    }

    @Test
    public void testVisitPath_SimpleProperty() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/info");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(2, visitor.visitedNodes.size(), "Should visit document and info");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
        Assertions.assertTrue(visitor.visitedNodes.get(1) instanceof Info, "Second node should be Info");
    }

    @Test
    public void testVisitPath_NestedProperty() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/info/contact");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(3, visitor.visitedNodes.size(), "Should visit document, info, and contact");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
    }

    @Test
    public void testVisitPath_WithMapIndex() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/paths[/pets]/get");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertTrue(visitor.visitedNodes.size() >= 3, "Should visit at least 3 nodes");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
        Node lastNode = visitor.visitedNodes.get(visitor.visitedNodes.size() - 1);
        Assertions.assertTrue(lastNode instanceof Operation, "Last node should be an Operation");
    }

    @Test
    public void testVisitPath_WithNestedMapIndices() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/paths[/pets]/get/responses[200]");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertTrue(visitor.visitedNodes.size() >= 4, "Should visit at least 4 nodes");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
        Node lastNode = visitor.visitedNodes.get(visitor.visitedNodes.size() - 1);
        Assertions.assertTrue(lastNode instanceof OpenApiResponse, "Last node should be a Response");
    }

    @Test
    public void testVisitPath_WithListIndex() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/tags[0]");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(2, visitor.visitedNodes.size(), "Should visit document and first tag");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
    }

    @Test
    public void testVisitPath_WithSecondListIndex() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/tags[1]");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(2, visitor.visitedNodes.size(), "Should visit document and second tag");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
    }

    @Test
    public void testVisitPath_NonExistentProperty() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/nonExistentProperty");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(1, visitor.visitedNodes.size(), "Should visit only the document");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "Should visit the document");
    }

    @Test
    public void testVisitPath_NonExistentMapKey() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/paths[/nonexistent]/get");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        // Should visit document and paths, but stop before the non-existent path item
        Assertions.assertTrue(visitor.visitedNodes.size() >= 1, "Should visit at least the document");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
    }

    @Test
    public void testVisitPath_OutOfBoundsListIndex() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/tags[999]");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        // Should visit only the document, stopping at the out-of-bounds index
        Assertions.assertEquals(1, visitor.visitedNodes.size(), "Should visit only the document");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "Should visit the document");
    }

    @Test
    public void testVisitPath_PartiallyValidPath() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/info/contact/invalidProperty");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        // Should visit document, info, and contact, but stop at invalid property
        Assertions.assertEquals(3, visitor.visitedNodes.size(), "Should visit document, info, and contact");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
    }

    @Test
    public void testVisitPath_NonExistentResponse() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = NodePath.parse("/paths[/pets]/get/responses[404]");
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        // Should visit nodes up to responses, but not the non-existent 404 response
        Assertions.assertTrue(visitor.visitedNodes.size() >= 3, "Should visit at least 3 nodes");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "First node should be document");
    }

    @Test
    public void testVisitPath_EmptyPath() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        NodePath path = new NodePath(); // Empty path
        NodeCollectorVisitor visitor = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path, visitor);

        Assertions.assertEquals(1, visitor.visitedNodes.size(), "Should visit only the document");
        Assertions.assertEquals(document, visitor.visitedNodes.get(0), "Should visit the document");
    }

    @Test
    public void testVisitPath_MultiplePaths() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);

        // Test different paths on the same document
        NodePath path1 = NodePath.parse("/paths[/pets]/get");
        NodePath path2 = NodePath.parse("/paths[/users]/get");

        NodeCollectorVisitor visitor1 = new NodeCollectorVisitor();
        NodeCollectorVisitor visitor2 = new NodeCollectorVisitor();

        VisitorUtil.visitPath(document, path1, visitor1);
        VisitorUtil.visitPath(document, path2, visitor2);

        Assertions.assertTrue(visitor1.visitedNodes.size() > 0, "First path should visit nodes");
        Assertions.assertTrue(visitor2.visitedNodes.size() > 0, "Second path should visit nodes");
        Assertions.assertEquals(visitor1.visitedNodes.get(0), visitor2.visitedNodes.get(0),
            "Both should start with document");
    }
}
