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

package io.apitomy.datamodels.io;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.paths.NodePath;

/**
 * @author eric.wittmann@gmail.com
 */
public class IoTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<IoTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/io/tests.json");
        List<IoTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, IoTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(IoTestCase child) throws Throwable {
        String testCP = "fixtures/io/" + child.getTest();
        URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
        Assertions.assertNotNull(testUrl, "Test file not found on classpath: " + testCP);

        // Read the test source
        String original = loadResource(testUrl);
        Assertions.assertNotNull(original);
        // Parse into a Json object
        ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);

        // Parse into a data model
        Document doc = Library.readDocument(originalParsed);
        Assertions.assertNotNull(doc, "Document was null.");

        // Make sure we read the appropriate number of "extra" properties
        ExtraPropertyDetectionVisitor epv = new ExtraPropertyDetectionVisitor();
        Library.visitTree(doc, epv, TraverserDirection.down);
        int actualExtraProps = epv.getExtraPropertyCount();
        int expectedExtraProps = child.getExtraProperties();
        if (actualExtraProps != expectedExtraProps) {
            epv.extraProperties.forEach(ep -> {
                System.out.println("DETECTED EXTRA PROPERTY: " + ep);
            });
        }
        Assertions.assertEquals(expectedExtraProps, actualExtraProps,
                "Wrong number of extra properties found: " + epv.extraProperties);

        // Write the data model back to JSON
        ObjectNode roundTripJs = Library.writeDocument(doc);
        Assertions.assertNotNull(roundTripJs);

        // Stringify the round trip object
        String roundTrip = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(roundTripJs);
        Assertions.assertNotNull(roundTrip);
        assertJsonEquals(original, roundTrip);

        List<Node> allNodes = getAllNodes(doc);
        allNodes.forEach(node -> {
            Library.writeNode(node);
        });

        for (Node node : allNodes) {
            try {
                NodePath nodePath = Library.createNodePath(node);
                Assertions.assertNotNull(nodePath);
                String path = nodePath.toString();
                Assertions.assertNotNull(nodePath);
                nodePath = Library.parseNodePath(path);
                Node resolvedNode = Library.resolveNodePath(nodePath, doc);
                Assertions.assertNotNull(resolvedNode, "Failed to resolve node: " + nodePath.toString());
                Assertions.assertTrue(node == resolvedNode,
                        "Path failed to resolve [" + node.getClass().getSimpleName() + "] to the proper node: " + path);
            } catch (Throwable t) {
                System.err.println("Failure/error testing node path: " + Library.createNodePath(node).toString());
                throw t;
            }
        }
    }

    /**
     * Returns all nodes in the document.
     * @param doc the document to scan
     */
    private List<Node> getAllNodes(Document doc) {
        IoTestAllNodeFinder finder = new IoTestAllNodeFinder();
        Library.visitTree(doc, finder, TraverserDirection.down);
        return finder.allNodes;
    }

    /**
     * Loads a resource as a string (reads the content at the URL).
     * @param testResource the URL to read
     */
    private static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, "UTF-8");
    }

    /**
     * Compares two JSON strings.
     * @param expected the expected JSON
     * @param actual the actual JSON
     */
    private static void assertJsonEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, true);
    }

}
