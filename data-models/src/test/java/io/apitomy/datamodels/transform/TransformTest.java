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

package io.apitomy.datamodels.transform;

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
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * @author eric.wittmann@gmail.com
 */
public class TransformTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<TransformTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/transformation/tests.json");
        List<TransformTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, TransformTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(TransformTestCase child) throws Throwable {
        String testCP = "fixtures/transformation/" + child.getInput();
        URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
        Assertions.assertNotNull(testUrl, "Could not load test resource: " + testCP);

        if (child.getFromType() == null) {
            child.setFromType(ModelType.OPENAPI20.name());
        }
        if (child.getToType() == null) {
            child.setToType(ModelType.OPENAPI30.name());
        }

        ModelType fromType = ModelTypeUtil.fromString(child.getFromType());
        ModelType toType = ModelTypeUtil.fromString(child.getToType());

        // Read the test source
        String original = loadResource(testUrl);
        Assertions.assertNotNull(original);
        // Parse into a Json object
        ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);

        // Read into a data model
        Document fromDoc = Library.readDocument(originalParsed);
        Assertions.assertEquals(fromType, fromDoc.root().modelType());

        // Transform the document
        Document toDoc = Library.transformDocument(fromDoc, toType);
        Assertions.assertNotNull(toDoc);
        Assertions.assertEquals(toType, toDoc.root().modelType());

        // Now compare with expected
        String actual = Library.writeDocumentToJSONString(toDoc);
        String expectedCP = "fixtures/transformation/" + child.getExpected();
        URL expectedUrl = Thread.currentThread().getContextClassLoader().getResource(expectedCP);
        Assertions.assertNotNull(expectedUrl, "Could not load test resource: " + expectedCP);
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
