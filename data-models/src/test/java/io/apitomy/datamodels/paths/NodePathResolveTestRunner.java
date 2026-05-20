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

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
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
public class NodePathResolveTestRunner extends ParentRunner<NodePathResolveTestCase> {

    private Class<?> testClass;
    private List<NodePathResolveTestCase> children;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
     */
    public NodePathResolveTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.children = loadTests();
    }

    private List<NodePathResolveTestCase> loadTests() throws InitializationError {
        try {
            URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/paths/resolve-tests.json");
            List<NodePathResolveTestCase> allTests = mapper.readValue(testsJsonUrl, mapper.getTypeFactory().constructCollectionType(List.class, NodePathResolveTestCase.class));
            return allTests;
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<NodePathResolveTestCase> getChildren() {
        return children;
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(NodePathResolveTestCase child) {
        return Description.createTestDescription(this.testClass, child.getName());
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(NodePathResolveTestCase child, RunNotifier notifier) {
        Description description = this.describeChild(child);
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String testCP = "fixtures/paths/" + child.getTest();
                URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
                Assert.assertNotNull(testUrl);

                // Read the test source
                String original = loadResource(testUrl);
                Assert.assertNotNull(original);
                // Parse into a Json object
                ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);

                // Parse into a data model
                Document doc = Library.readDocument(originalParsed);
                Assert.assertNotNull(doc);

                // Parse the test path
                NodePath np = Library.parseNodePath(child.getPath());

                // Resolve the path to a node in the source
                Node resolvedNode = Library.resolveNodePath(np, doc);
                Assert.assertNotNull(resolvedNode);

                // Compare source path to node path (test generating a node path from a node)
                NodePath createdPath = Library.createNodePath(resolvedNode);
                String expectedPath = child.getPath();
                String actualPath = createdPath.toString(true);
                Assert.assertEquals(expectedPath, actualPath);

                // Verify that the resolved node is what we expected it to be
                Object actualObj = Library.writeNode(resolvedNode);
                String actual = mapper.writeValueAsString(actualObj);
                String expectedCP = "fixtures/paths/" + child.getTest() + ".expected.json";
                URL expectedUrl = Thread.currentThread().getContextClassLoader().getResource(expectedCP);
                Assert.assertNotNull(testUrl);
                String expected = loadResource(expectedUrl);
                assertJsonEquals(expected, actual);
            }
        };
        runLeaf(statement, description, notifier);
    }

    /**
     * Loads a resource as a string (reads the content at the URL).
     * @param testResource
     * @throws IOException
     */
    static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, "UTF-8");
    }

    /**
     * Compares two JSON strings.
     * @param expected
     * @param actual
     * @throws JSONException
     */
    static void assertJsonEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, true);
    }

}
