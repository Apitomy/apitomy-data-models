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
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.paths.NodePath;

/**
 * @author eric.wittmann@gmail.com
 */
public class IoTestRunner extends ParentRunner<IoTestCase> {

    private Class<?> testClass;
    private List<IoTestCase> children;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
     */
    public IoTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.children = loadTests();
    }

    private List<IoTestCase> loadTests() throws InitializationError {
        try {
            URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/io/tests.json");
            List<IoTestCase> allTests = mapper.readValue(testsJsonUrl, mapper.getTypeFactory().constructCollectionType(List.class, IoTestCase.class));
            return allTests;
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<IoTestCase> getChildren() {
        return children;
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(IoTestCase child) {
        return Description.createTestDescription(this.testClass, child.getName());
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(IoTestCase child, RunNotifier notifier) {
        Description description = this.describeChild(child);
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String testCP = "fixtures/io/" + child.getTest();
                URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
                Assert.assertNotNull("Test file not found on classpath: " + testCP, testUrl);

                // Read the test source
                String original = loadResource(testUrl);
                Assert.assertNotNull(original);
                // Parse into a Json object
                ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);

                // Parse into a data model
                Document doc = Library.readDocument(originalParsed);
                Assert.assertNotNull("Document was null.", doc);

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
                Assert.assertEquals("Wrong number of extra properties found: " + epv.extraProperties, expectedExtraProps, actualExtraProps);

                // Write the data model back to JSON
                ObjectNode roundTripJs = Library.writeDocument(doc);
                Assert.assertNotNull(roundTripJs);

                // Stringify the round trip object
                String roundTrip = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(roundTripJs);
                Assert.assertNotNull(roundTrip);
                assertJsonEquals(original, roundTrip);

                List<Node> allNodes = getAllNodes(doc);
                allNodes.forEach(node -> {
                    Library.writeNode(node);
                });

                for (Node node : allNodes) {
                    try {
                        NodePath nodePath = Library.createNodePath(node);
                        Assert.assertNotNull(nodePath);
                        String path = nodePath.toString();
                        Assert.assertNotNull(nodePath);
                        nodePath = Library.parseNodePath(path);
                        Node resolvedNode = Library.resolveNodePath(nodePath, doc);
                        Assert.assertNotNull("Failed to resolve node: " + nodePath.toString(), resolvedNode);
                        Assert.assertTrue("Path failed to resolve [" + node.getClass().getSimpleName() + "] to the proper node: " + path, node == resolvedNode);
                    } catch (Throwable t) {
                        System.err.println("Failure/error testing node path: " + Library.createNodePath(node).toString());
                        throw t;
                    }
                }
                //
                //                // Now do a partial read/write test?  This isn't great because there's not a great way to
                //                // assert the results.  But at least it runs everything through the reader dispatchers.
                //                for (Node node : allNodes) {
                //                    // Skip extensions and expressions.
                //                    if (node instanceof Extension || node instanceof IOas30Expression) {
                //                        continue;
                //                    }
                //                    // TODO :: check the extra-property counts when doing partial i/o testing
                //                    try {
                //                        Object partialNodeJson = Library.writeNode(node);
                //                        String partialNodeJsonStr = JsonCompat.stringify(partialNodeJson);
                //
                //                        Node clonedNode = cloneNode(node);
                //                        // Skip any node that we can't clone.
                //                        if (clonedNode == null) {
                //                            continue;
                //                        }
                //                        Library.readNode(partialNodeJson, clonedNode);
                //
                //                        Object finalJson = Library.writeNode(clonedNode);
                //                        String finalJsonStr = JsonCompat.stringify(finalJson);
                //
                ////                        LoggerCompat.info("PARTIAL: %s", partialNodeJson);
                ////                        LoggerCompat.info("FINAL:   %s", finalJsonStr);
                //
                //                        assertJsonEquals(partialNodeJsonStr, finalJsonStr);
                //                    } catch (Throwable t) {
                //                        System.err.println("Failure/error testing partial I/O for node: " + Library.createNodePath(node).toString());
                //                        throw t;
                //                    }
                //                }
            }
        };
        runLeaf(statement, description, notifier);
    }

    /**
     * Returns all nodes in the document.
     * @param doc
     */
    protected List<Node> getAllNodes(Document doc) {
        IoTestAllNodeFinder finder = new IoTestAllNodeFinder();
        Library.visitTree(doc, finder, TraverserDirection.down);
        return finder.allNodes;
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
    //
    //    static Node cloneNode(Node node) {
    //        Constructor<? extends Node> constructor;
    //        Node clonedNode = null;
    //        try {
    //            if (node instanceof INamed) {
    //                constructor = node.getClass().getConstructor(String.class);
    //                clonedNode = constructor.newInstance(((INamed) node).getName());
    //            } else if (node instanceof OasPathItem) {
    //                constructor = node.getClass().getConstructor(String.class);
    //                clonedNode = constructor.newInstance(((OasPathItem) node).getPath());
    //            } else if (node instanceof Operation) {
    //                constructor = node.getClass().getConstructor(String.class);
    //                clonedNode = constructor.newInstance(((Operation) node).getType());
    //            } else if (node instanceof Oas20PropertySchema) {
    //                constructor = node.getClass().getConstructor(String.class);
    //                clonedNode = constructor.newInstance(((Oas20PropertySchema) node).getPropertyName());
    //            } else if (node instanceof Oas30PropertySchema) {
    //                constructor = node.getClass().getConstructor(String.class);
    //                clonedNode = constructor.newInstance(((Oas30PropertySchema) node).getPropertyName());
    //            } else if (node instanceof ServerVariable) {
    //                constructor = node.getClass().getConstructor(String.class);
    //                clonedNode = constructor.newInstance(((ServerVariable) node).getName());
    //            } else {
    //                constructor = node.getClass().getConstructor();
    //                clonedNode = constructor.newInstance();
    //            }
    //        } catch (SecurityException | InstantiationException | IllegalAccessException
    //                | IllegalArgumentException | InvocationTargetException e) {
    //            e.printStackTrace();
    //        } catch (NoSuchMethodException e) {
    //            System.err.println("Skipping partial I/O for: " + node.getClass().getSimpleName());
    //        }
    //        if (clonedNode != null) {
    //            clonedNode._parent = node._parent;
    //            clonedNode._ownerDocument = node.ownerDocument();
    //            if (clonedNode instanceof Document) {
    //                clonedNode._ownerDocument = (Document) clonedNode;
    //            }
    //        }
    //        return clonedNode;
    //    }

}
