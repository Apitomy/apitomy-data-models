package org.example.multi.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.example.multi.MltiDocument;
import org.example.multi.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MultiSpecTest {

    @Test
    public void testFullSimplest10() throws Exception {
        doFullTest("simplest10.json", "1.0");
    }

    @Test
    public void testFullSimplest20() throws Exception {
        doFullTest("simplest20.json", "2.0");
    }

    @Test
    public void testFull10Generated() throws Exception {
        doFullTest("mlti10-full.json", "1.0");
    }

    @Test
    public void testFull20Generated() throws Exception {
        doFullTest("mlti20-full.json", "2.0");
    }

    private void doFullTest(String testFile, String version) throws Exception {
        String originalContent = loadTestResource(testFile);
        ObjectNode originalJson = (ObjectNode) JsonUtil.parseJSON(originalContent);
        JsonUtil.setStringProperty(originalJson, "version", version);
        originalContent = JsonUtil.stringify(originalJson);

        MltiDocument inputDocument = MultiSpecTestLibrary.readDocument(originalJson);
        ObjectNode roundTripJson = MultiSpecTestLibrary.writeDocument(inputDocument);

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
