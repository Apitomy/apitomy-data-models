/*
 * Copyright 2019 JBoss Inc
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

package io.apitomy.datamodels.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
import io.apitomy.datamodels.refs.IReferenceResolver;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.refs.ResolvedReference;

/**
 * @author eric.wittmann@gmail.com
 */
public class ValidationTestRunner extends ParentRunner<ValidationTestCase> implements IReferenceResolver {

    private Class<?> testClass;
    private List<ValidationTestCase> children;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
     */
    public ValidationTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.children = loadTests();
    }

    /**
     * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier notifier) {
        Library.addReferenceResolver(this);
        try {
            super.run(notifier);
        } finally {
            Library.removeReferenceResolver(this);
        }
    }

    /**
     * Loads the tests.
     * @throws InitializationError
     */
    private List<ValidationTestCase> loadTests() throws InitializationError {
        try {
            URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/validation/tests.json");
            List<ValidationTestCase> allTests = mapper.readValue(testsJsonUrl, mapper.getTypeFactory().constructCollectionType(List.class, ValidationTestCase.class));
            return allTests;
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<ValidationTestCase> getChildren() {
        return children;
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(ValidationTestCase child) {
        return Description.createTestDescription(this.testClass, child.getName());
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(ValidationTestCase child, RunNotifier notifier) {
        Description description = this.describeChild(child);
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String testCP = "fixtures/validation/" + child.getTest();
                URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
                Assert.assertNotNull("Could not load test resource: " + testCP, testUrl);

                // Read the test source
                String original = loadResource(testUrl);
                Assert.assertNotNull(original);
                // Parse into a Json object
                ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);

                // Parse into a data model
                Document doc = Library.readDocument(originalParsed);

                // Validate the document
                IValidationSeverityRegistry severityRegistry = null;
                if (child.getSeverity() != null) {
                    final ValidationProblemSeverity severity = ValidationProblemSeverity.valueOf(child.getSeverity());
                    severityRegistry = new IValidationSeverityRegistry() {
                        @Override
                        public ValidationProblemSeverity lookupSeverity(ValidationRuleMetaData rule) {
                            return severity;
                        }
                    };
                }
                List<ValidationProblem> problems = Library.validate(doc, severityRegistry);

                // Now compare with expected
                String actual = formatProblems(problems);
                String expectedCP = testCP + ".expected";
                URL expectedUrl = Thread.currentThread().getContextClassLoader().getResource(expectedCP);
                Assert.assertNotNull("Could not load test resource: " + expectedCP, expectedUrl);
                String expected = loadResource(expectedUrl);

                Assert.assertEquals(normalizeValidationOutput(expected), normalizeValidationOutput(actual));
            }
        };
        runLeaf(statement, description, notifier);
    }

    /**
     * @param expected
     */
    protected String normalizeValidationOutput(String value) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(value));
        String line = reader.readLine();
        List<String> lines = new ArrayList<>();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        lines.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        return lines.stream().sorted().collect(Collectors.joining("\n"));
    }

    /**
     * Format the list of problems as a string.
     * @param problems
     */
    protected String formatProblems(List<ValidationProblem> problems) {
        StringBuilder builder = new StringBuilder();
        problems.forEach(problem -> {
            builder.append("[");
            builder.append(problem.errorCode);
            builder.append("] |");
            builder.append(problem.severity);
            builder.append("| {");
            builder.append(problem.nodePath.toString(true));
            builder.append("->");
            builder.append(problem.property);
            builder.append("} :: ");
            builder.append(problem.message);
            builder.append("\n");
        });
        return builder.toString();
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

    /**
     * @see io.apitomy.datamodels.core.util.IReferenceResolver#resolveRef(java.lang.String, io.apitomy.datamodels.core.models.Node)
     */
    @Override
    public ResolvedReference resolveRef(String reference, Node from) {
        try {
            if (reference != null && reference.startsWith("test:")) {
                int colonIdx = reference.indexOf(":");
                int hashIdx = reference.indexOf("#");
                String resourceName = reference.substring(colonIdx + 1, hashIdx);
                String fragment = reference.substring(hashIdx + 1);
                String resourceCP = "fixtures/validation/shared/" + resourceName;
                URL testUrl = Thread.currentThread().getContextClassLoader().getResource(resourceCP);
                Assert.assertNotNull("Could not find test resource: " + resourceName, testUrl);
                String resourceContent = loadResource(testUrl);
                Assert.assertNotNull("Failed to load test resource: " + resourceName, resourceContent);
                ObjectNode content = (ObjectNode) JsonUtil.parseJSON(resourceContent);
                Assert.assertNotNull("Could not parse test resource: " + resourceName, content);
                ObjectNode resolvedContent = ReferenceUtil.resolveFragmentFromJS(content, fragment);
                Assert.assertNotNull("Failed to resolve fragment: " + fragment, resolvedContent);
                Node emptyClone = from.emptyClone();
                emptyClone.attach(from.parent());
                Node node = Library.readNode(resolvedContent, emptyClone);
                return ResolvedReference.fromNode(node);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
