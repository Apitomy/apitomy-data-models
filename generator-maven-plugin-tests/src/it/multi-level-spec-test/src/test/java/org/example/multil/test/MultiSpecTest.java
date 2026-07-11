package org.example.multil.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.example.multil.Document;
import org.example.multil.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MultiSpecTest {

    @Test
    public void testFull_First10Generated() throws Exception {
        doFullTest("frst10-full.json", "first", "1.0");
    }

    @Test
    public void testFull_First20Generated() throws Exception {
        doFullTest("frst20-full.json", "first", "2.0");
    }

    @Test
    public void testFull_Second10Generated() throws Exception {
        doFullTest("scnd10-full.json", "second", "1.0");
    }

    @Test
    public void testFull_Second20Generated() throws Exception {
        doFullTest("scnd20-full.json", "second", "2.0");
    }

    private void doFullTest(String testFile, String spec, String version) throws Exception {
        String originalContent = loadTestResource(testFile);
        ObjectNode originalJson = (ObjectNode) JsonUtil.parseJSON(originalContent);
        JsonUtil.setStringProperty(originalJson, "spec", spec);
        JsonUtil.setStringProperty(originalJson, "version", version);
        originalContent = JsonUtil.stringify(originalJson);

        Document inputDocument = MultiLevelSpecTestLibrary.readDocument(originalJson);
        ObjectNode roundTripJson = MultiLevelSpecTestLibrary.writeDocument(inputDocument);

        originalJson = (ObjectNode) JsonUtil.parseJSON(originalContent);
        assertJsonEquals(originalJson, roundTripJson);
    }

    private String loadTestResource(String resourceName) throws Exception {
        String resourcePath = "fixtures/" + resourceName;
        try (InputStream res = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (res == null) {
                Assert.fail("Test resource not found: " + resourcePath);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(res.readAllBytes());
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void assertJsonEquals(ObjectNode originalJson, ObjectNode roundTripJson) throws Exception {
        try {
            Assert.assertEquals(originalJson, roundTripJson);
        } catch (AssertionError e) {
            Assert.assertEquals(JsonUtil.stringify(originalJson), JsonUtil.stringify(roundTripJson));
        }
    }

}
