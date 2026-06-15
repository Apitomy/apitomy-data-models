package org.example.io.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.example.io.v10.Iot10Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IoTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testReaderSimplest() throws Exception {
        String testContent = loadTestResource("simplest.json");
        Iot10Document document = IoTestLibrary.readDocument(testContent);
        Assertions.assertNotNull(document);
        Assertions.assertEquals("simplest", document.getId());
    }

    @Test
    public void testReaderSimple() throws Exception {
        String testContent = loadTestResource("simple.json");
        Iot10Document document = IoTestLibrary.readDocument(testContent);
        Assertions.assertNotNull(document);
        Assertions.assertEquals("simple", document.getId());
        Assertions.assertNotNull(document.getPrimitives());
        Assertions.assertEquals("hello-world", document.getPrimitives().getStringProperty());
        Assertions.assertEquals(true, document.getPrimitives().isBooleanProperty());
        Assertions.assertEquals(Integer.valueOf(17), document.getPrimitives().getIntegerProperty());
        Assertions.assertEquals(Double.valueOf(117.5), document.getPrimitives().getNumberProperty());
        Assertions.assertNotNull(document.getPrimitives().getObjectProperty());
        Assertions.assertNotNull(document.getPrimitives().getAnyProperty());
    }

    @Test
    public void testFullSimplest() throws Exception {
        doFullTest("simplest.json");
    }

    @Test
    public void testFullSimple() throws Exception {
        doFullTest("simple.json");
    }

    @Test
    public void testFullGenerated() throws Exception {
        doFullTest("iot10-full.json");
    }

    private void doFullTest(String testFile) throws Exception {
        String originalContent = loadTestResource(testFile);
        Iot10Document inputDocument = IoTestLibrary.readDocument(originalContent);
        String roundTripContent = IoTestLibrary.writeDocument(inputDocument);
        assertJsonEquals(originalContent, roundTripContent);
    }

    private String loadTestResource(String resourceName) throws Exception {
        String resourcePath = "fixtures/" + resourceName;
        try (InputStream res = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (res == null) {
                Assertions.fail("Test resource not found: " + resourcePath);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(res.readAllBytes());
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void assertJsonEquals(String expectedJson, String actualJson) throws Exception {
        Assertions.assertEquals(mapper.readTree(expectedJson), mapper.readTree(actualJson));
    }

}
