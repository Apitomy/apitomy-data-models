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
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import io.apitomy.datamodels.refs.IReferenceResolver;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.refs.ResolvedReference;

/**
 * @author eric.wittmann@gmail.com
 */
public class ValidationTest implements IReferenceResolver {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ValidationTest INSTANCE = new ValidationTest();

    @BeforeAll
    static void setUp() {
        Library.addReferenceResolver(INSTANCE);
    }

    @AfterAll
    static void tearDown() {
        Library.removeReferenceResolver(INSTANCE);
    }

    /**
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<ValidationTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/validation/tests.json");
        List<ValidationTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, ValidationTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(ValidationTestCase child) throws Throwable {
        String testCP = "fixtures/validation/" + child.getTest();
        URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
        Assertions.assertNotNull(testUrl, "Could not load test resource: " + testCP);

        // Read the test source
        String original = loadResource(testUrl);
        Assertions.assertNotNull(original);
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
        Assertions.assertNotNull(expectedUrl, "Could not load test resource: " + expectedCP);
        String expected = loadResource(expectedUrl);

        Assertions.assertEquals(normalizeValidationOutput(expected), normalizeValidationOutput(actual));
    }

    /**
     * Normalizes validation output for comparison.
     * @param value the validation output string
     */
    private String normalizeValidationOutput(String value) throws Exception {
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
     * @param problems the validation problems
     */
    private String formatProblems(List<ValidationProblem> problems) {
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

    /** {@inheritDoc} */
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
                Assertions.assertNotNull(testUrl, "Could not find test resource: " + resourceName);
                String resourceContent = loadResource(testUrl);
                Assertions.assertNotNull(resourceContent, "Failed to load test resource: " + resourceName);
                ObjectNode content = (ObjectNode) JsonUtil.parseJSON(resourceContent);
                Assertions.assertNotNull(content, "Could not parse test resource: " + resourceName);
                ObjectNode resolvedContent = ReferenceUtil.resolveFragmentFromJS(content, fragment);
                Assertions.assertNotNull(resolvedContent, "Failed to resolve fragment: " + fragment);
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
