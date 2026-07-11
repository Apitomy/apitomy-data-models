/*
 * Copyright 2020 JBoss Inc
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

package io.apitomy.datamodels.refs;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;

/**
 * @author eric.wittmann@gmail.com
 */
public class DereferenceTestRunner extends ParentRunner<DereferenceTestCase> {

    private Class<?> testClass;
    private List<DereferenceTestCase> children;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
     * @throws IOException
     */
    public DereferenceTestRunner(Class<?> testClass) throws InitializationError, IOException {
        super(testClass);
        this.testClass = testClass;
        this.children = loadTests();
        loadRefs();
        Library.addReferenceResolver(new DereferenceTestReferenceResolver());
    }

    private List<DereferenceTestCase> loadTests() throws InitializationError {
        try {
            URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/dereference/tests.json");
            Assert.assertNotNull(testsJsonUrl);
            return mapper.readValue(testsJsonUrl, mapper.getTypeFactory().constructCollectionType(List.class, DereferenceTestCase.class));
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    private void loadRefs() throws IOException {
        URL refsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/dereference/tests.refs.json");
        Assert.assertNotNull(refsJsonUrl);
        JsonNode tree = mapper.readTree(refsJsonUrl);
        ObjectNode root = (ObjectNode) tree;
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String fname = fieldNames.next();
            JsonNode val = root.get(fname);
            DereferenceTestReferenceResolver.refs.put(fname, val);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<DereferenceTestCase> getChildren() {
        return children;
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(DereferenceTestCase child) {
        return Description.createTestDescription(this.testClass, child.getName());
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(DereferenceTestCase child, RunNotifier notifier) {
        Description description = this.describeChild(child);
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String testCP = "fixtures/dereference/" + child.getInput();
                URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
                Assert.assertNotNull("Could not load test resource: " + testCP, testUrl);

                // Read the test source
                String original = loadResource(testUrl);
                Assert.assertNotNull(original);
                // Parse into a Json object
                ObjectNode originalParsed = (ObjectNode) mapper.reader().readTree(original);

                // Read into a data model
                Document srcDoc = Library.readDocument(originalParsed);
                Assert.assertNotNull(srcDoc);
                Assert.assertNotNull(srcDoc.root());
                Assert.assertNotNull(srcDoc.root().modelType());

                // Dereference the document
                try {
                    Document dereferencedDoc = Library.dereferenceDocument(srcDoc, child.isStrict());
                    Assert.assertNotNull(dereferencedDoc);
                    Assert.assertNotSame(srcDoc, dereferencedDoc);


                    // Now compare with expected
                    String actual = Library.writeDocumentToJSONString(dereferencedDoc);
                    String expectedCP = "fixtures/dereference/" + child.getExpected();
                    URL expectedUrl = Thread.currentThread().getContextClassLoader().getResource(expectedCP);
                    Assert.assertNotNull("Could not load test resource: " + expectedCP, expectedUrl);
                    String expected = loadResource(expectedUrl);

                    assertJsonEquals(expected, actual);
                } catch (RuntimeException re) {
                    re.printStackTrace();
                    Assert.fail(re.toString());
                }
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
