package io.apitomy.datamodels;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.jsonschema.modern.v202012.JM202012FullSchema;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.union.StringUnionValueImpl;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openrpc.v1x.v14.OpenRpc14Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LibraryTest {

    private static final String EMPTY_OPENAPI = "{"
            + "  \"openapi\": \"3.0.1\""
            + "}";

    @Test
    public void testReadDocumentFromJSONString() {
        OpenApi30Document document = (OpenApi30Document) Library.readDocumentFromJSONString(EMPTY_OPENAPI);
        Assertions.assertEquals("3.0.1", document.getOpenapi());
    }

    /**
     * A JSON Schema document may be the literal true or false. It reads as a root that is not a
     * Node, knows its own model type and root, and writes back unchanged.
     */
    @Test
    public void testBooleanJsonSchemaRoot() {
        for (String json : new String[] {"true", "false"}) {
            RootCapable root = Library.readRootFromJSONString(json);
            Assertions.assertInstanceOf(JsonSchema.class, root);
            Assertions.assertFalse(root.isNode());
            Assertions.assertEquals(ModelType.JD7, root.modelType());
            Assertions.assertSame(root, root.root());
            Assertions.assertEquals(json, Library.writeRootToJSONString(root));
        }
        Assertions.assertThrows(UnsupportedModelTypeException.class, () -> Library.readRootFromJSONString("42"));
    }

    @Test
    public void testJsonSchema202012() {
        JM202012FullSchema document = (JM202012FullSchema) Library.createRoot(ModelType.JM202012);
        document.set$id("http://example.com/draft2020-12/my-schema.json");
        document.setType(new StringUnionValueImpl("string"));

        String expected = """
{
    "$id": "http://example.com/draft2020-12/my-schema.json",
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "type": "string"
}""";

        String actual = Library.writeNodeToString((io.apitomy.datamodels.models.Node) document);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testOpenApi3() {
        OpenApiDocument document = (OpenApiDocument) Library.createDocument(ModelType.OPENAPI30);
        document.setInfo(document.createInfo());
        document.getInfo().setTitle("My API");
        document.getInfo().setVersion("1.0");

        String expected = """
{
    "openapi": "3.0.3",
    "info": {
        "title": "My API",
        "version": "1.0"
    }
}""";

        String actual = Library.writeDocumentToJSONString(document);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testCreateOpenApi30() {
        OpenApi30Document document = (OpenApi30Document) Library.createDocument(ModelType.OPENAPI30);
        document.setInfo(document.createInfo());
        document.getInfo().setTitle("My API");
        document.getInfo().setVersion("1.0");

        Assertions.assertEquals("3.0.3", document.getOpenapi());
        Assertions.assertEquals("My API", document.getInfo().getTitle());
        Assertions.assertEquals("1.0", document.getInfo().getVersion());
    }

    @Test
    public void testCreateOpenRpc14() {
        OpenRpc14Document document = (OpenRpc14Document) Library.createDocument(ModelType.OPENRPC14);
        document.setInfo(document.createInfo());
        document.getInfo().setTitle("My RPC API");
        document.getInfo().setVersion("1.0");

        Assertions.assertEquals("1.4.0", document.getOpenrpc());
        Assertions.assertEquals("My RPC API", document.getInfo().getTitle());
        Assertions.assertEquals("1.0", document.getInfo().getVersion());
    }

}
