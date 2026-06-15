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

package io.apitomy.datamodels.paths;

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
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * @author eric.wittmann@gmail.com
 */
public class NodePathResolveTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<NodePathResolveTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/paths/resolve-tests.json");
        List<NodePathResolveTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, NodePathResolveTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(NodePathResolveTestCase child) throws Throwable {
        String testCP = "fixtures/paths/" + child.getTest();
        URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
        Assertions.assertNotNull(testUrl);

        // Read the test source
        String original = loadResource(testUrl);
        Assertions.assertNotNull(original);
        // Parse into a Json object
        ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);

        // Parse into a data model
        Document doc = Library.readDocument(originalParsed);
        Assertions.assertNotNull(doc);

        // Parse the test path
        NodePath np = Library.parseNodePath(child.getPath());

        // Resolve the path to a node in the source
        Node resolvedNode = Library.resolveNodePath(np, doc);
        Assertions.assertNotNull(resolvedNode);

        // Compare source path to node path (test generating a node path from a node)
        NodePath createdPath = Library.createNodePath(resolvedNode);
        String expectedPath = child.getPath();
        String actualPath = createdPath.toString(true);
        Assertions.assertEquals(expectedPath, actualPath);

        // Verify that the resolved node is what we expected it to be
        Object actualObj = Library.writeNode(resolvedNode);
        String actual = mapper.writeValueAsString(actualObj);
        String expectedCP = "fixtures/paths/" + child.getTest() + ".expected.json";
        URL expectedUrl = Thread.currentThread().getContextClassLoader().getResource(expectedCP);
        Assertions.assertNotNull(testUrl);
        String expected = loadResource(expectedUrl);
        assertJsonEquals(expected, actual);
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
