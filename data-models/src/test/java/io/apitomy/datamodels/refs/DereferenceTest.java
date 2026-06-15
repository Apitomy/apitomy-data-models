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
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;

/**
 * @author eric.wittmann@gmail.com
 */
public class DereferenceTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        loadRefs();
        Library.addReferenceResolver(new DereferenceTestReferenceResolver());
    }

    private static void loadRefs() throws IOException {
        URL refsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/dereference/tests.refs.json");
        Assertions.assertNotNull(refsJsonUrl);
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
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<DereferenceTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/dereference/tests.json");
        Assertions.assertNotNull(testsJsonUrl);
        List<DereferenceTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, DereferenceTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(DereferenceTestCase child) throws Throwable {
        String testCP = "fixtures/dereference/" + child.getInput();
        URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
        Assertions.assertNotNull(testUrl, "Could not load test resource: " + testCP);

        // Read the test source
        String original = loadResource(testUrl);
        Assertions.assertNotNull(original);
        // Parse into a Json object
        ObjectNode originalParsed = (ObjectNode) mapper.reader().readTree(original);

        // Read into a data model
        Document srcDoc = Library.readDocument(originalParsed);
        Assertions.assertNotNull(srcDoc);
        Assertions.assertNotNull(srcDoc.root());
        Assertions.assertNotNull(srcDoc.root().modelType());

        // Dereference the document
        try {
            Document dereferencedDoc = Library.dereferenceDocument(srcDoc, child.isStrict());
            Assertions.assertNotNull(dereferencedDoc);
            Assertions.assertNotSame(srcDoc, dereferencedDoc);

            // Now compare with expected
            String actual = Library.writeDocumentToJSONString(dereferencedDoc);
            String expectedCP = "fixtures/dereference/" + child.getExpected();
            URL expectedUrl = Thread.currentThread().getContextClassLoader().getResource(expectedCP);
            Assertions.assertNotNull(expectedUrl, "Could not load test resource: " + expectedCP);
            String expected = loadResource(expectedUrl);

            assertJsonEquals(expected, actual);
        } catch (RuntimeException re) {
            re.printStackTrace();
            Assertions.fail(re.toString());
        }
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
