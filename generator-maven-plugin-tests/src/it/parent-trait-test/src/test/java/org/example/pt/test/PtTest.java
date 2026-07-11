package org.example.pt.test;

import org.example.pt.PtFoo;
import org.example.pt.PtFooParent;
import org.example.pt.v1x.v10.Pt10Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PtTest {

    @Test
    public void testSimple() throws Exception {
        String testContent = loadTestResource("simple.json");
        Pt10Document document = PtTestLibrary.readDocument(testContent);
        Assert.assertNotNull(document);

        PtFoo foo = getFoo(document);
        System.out.println("Foo found: " + foo.getName());

        foo = getFoo((PtFooParent) document.getDefinitions());
        System.out.println("Foo found: " + foo.getName());
    }

    private PtFoo getFoo(PtFooParent parent) {
        return parent.getFoo();
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
}
