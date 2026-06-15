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

package io.apitomy.datamodels.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;

/**
 * @author eric.wittmann@gmail.com
 */
public class NodeUtilTest {

    private static final String SAMPLE_OPENAPI = "{\n" +
            "  \"openapi\": \"3.0.1\",\n" +
            "  \"info\": {\n" +
            "    \"title\": \"Test API\",\n" +
            "    \"version\": \"1.0.0\"\n" +
            "  },\n" +
            "  \"paths\": {}\n" +
            "}";

    @Test
    public void testGetNodeProperty_ValidGetter() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);

        Object info = NodeUtil.getNodeProperty(document, "info");

        Assertions.assertNotNull(info, "Should return Info node");
        Assertions.assertTrue(info instanceof Info, "Should be an Info instance");
    }

    @Test
    public void testGetNodeProperty_StringProperty() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);
        Info info = document.getInfo();

        Object title = NodeUtil.getNodeProperty(info, "title");

        Assertions.assertNotNull(title, "Should return title");
        Assertions.assertEquals("Test API", title, "Should return the correct title");
    }

    @Test
    public void testGetNodeProperty_NonExistentProperty() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);

        Object result = NodeUtil.getNodeProperty(document, "nonExistentProperty");

        Assertions.assertNull(result, "Should return null for non-existent property");
    }

    @Test
    public void testGetProperty_NodeProperty() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);

        Object info = NodeUtil.getProperty(document, "info");

        Assertions.assertNotNull(info, "Should return Info node via getProperty");
        Assertions.assertTrue(info instanceof Info, "Should be an Info instance");
    }

    @Test
    public void testIsNode() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(SAMPLE_OPENAPI);

        Assertions.assertTrue(NodeUtil.isNode(document), "Document should be a Node");
        Assertions.assertTrue(NodeUtil.isNode(document.getInfo()), "Info should be a Node");
        Assertions.assertFalse(NodeUtil.isNode(null), "null should not be a Node");
        Assertions.assertFalse(NodeUtil.isNode("a string"), "String should not be a Node");
    }
}
