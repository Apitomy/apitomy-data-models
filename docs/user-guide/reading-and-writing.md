# Reading & Writing Documents

This page covers how to parse, create, serialize, and clone documents.

## Reading a Document

The library auto-detects the specification type and version by examining top-level properties
like `openapi`, `asyncapi`, `swagger`, `openrpc`, and `$schema`.

### From a JSON String

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;

    String json = "{ \"openapi\": \"3.0.3\", \"info\": { \"title\": \"My API\", \"version\": \"1.0\" } }";
    Document doc = Library.readDocumentFromJSONString(json);
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document } from '@apitomy/data-models';

    const jsonString = '{ "openapi": "3.0.3", "info": { "title": "My API", "version": "1.0" } }';
    const doc: Document = Library.readDocumentFromJSONString(jsonString);
    ```

### From a Parsed JSON Object

If you already have a parsed JSON object, pass it directly.

=== "Java"

    ```java
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.databind.node.ObjectNode;
    import io.apitomy.datamodels.Library;

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = (ObjectNode) mapper.readTree(jsonString);
    Document doc = Library.readDocument(json);
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document } from '@apitomy/data-models';

    const json = JSON.parse(jsonString);
    const doc: Document = Library.readDocument(json);
    ```

### Casting to a Specific Type

After reading, cast the document to the specific type for typed access to its properties.

=== "Java"

    ```java
    import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;

    OpenApi30Document openApiDoc = (OpenApi30Document) doc;
    String title = openApiDoc.getInfo().getTitle();
    String version = openApiDoc.getInfo().getVersion();
    ```

=== "TypeScript"

    ```typescript
    import { OpenApi30Document } from '@apitomy/data-models';

    const openApiDoc = doc as OpenApi30Document;
    const title = openApiDoc.getInfo().getTitle();
    const version = openApiDoc.getInfo().getVersion();
    ```

---

## Creating a New Document

Use `Library.createDocument()` with a `ModelType` to create a new, empty document. Then
populate it using typed setters and `createXxx()` factory methods.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.ModelType;
    import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;

    OpenApi30Document doc = (OpenApi30Document) Library.createDocument(ModelType.OPENAPI30);

    // Set the info section
    doc.setInfo(doc.createInfo());
    doc.getInfo().setTitle("Pet Store");
    doc.getInfo().setVersion("1.0.0");
    doc.getInfo().setDescription("A sample API for pets");

    // Add a path
    doc.setPaths(doc.createPaths());
    doc.getPaths().addItem("/pets", doc.getPaths().createPathItem());

    // Add a GET operation to the path
    var pathItem = doc.getPaths().getItem("/pets");
    pathItem.setGet(pathItem.createOperation());
    pathItem.getGet().setOperationId("listPets");
    pathItem.getGet().setSummary("List all pets");
    ```

=== "TypeScript"

    ```typescript
    import { Library, ModelType, OpenApi30Document } from '@apitomy/data-models';

    const doc = Library.createDocument(ModelType.OPENAPI30) as OpenApi30Document;

    // Set the info section
    doc.setInfo(doc.createInfo());
    doc.getInfo().setTitle('Pet Store');
    doc.getInfo().setVersion('1.0.0');
    doc.getInfo().setDescription('A sample API for pets');

    // Add a path
    doc.setPaths(doc.createPaths());
    doc.getPaths().addItem('/pets', doc.getPaths().createPathItem());

    // Add a GET operation to the path
    const pathItem = doc.getPaths().getItem('/pets');
    pathItem.setGet(pathItem.createOperation());
    pathItem.getGet().setOperationId('listPets');
    pathItem.getGet().setSummary('List all pets');
    ```

### Available Model Types

| ModelType | Specification |
|-----------|--------------|
| `OPENAPI20` | OpenAPI 2.0 (Swagger) |
| `OPENAPI30` | OpenAPI 3.0.x |
| `OPENAPI31` | OpenAPI 3.1.x |
| `OPENAPI32` | OpenAPI 3.2.x |
| `ASYNCAPI20` – `ASYNCAPI26` | AsyncAPI 2.0 through 2.6 |
| `ASYNCAPI30`, `ASYNCAPI31` | AsyncAPI 3.0, 3.1 |
| `OPENRPC13`, `OPENRPC14` | OpenRPC 1.3, 1.4 |
| `JSDRAFT4`, `JSDRAFT6`, `JSDRAFT7` | JSON Schema Draft 4, 6, 7 |
| `JS201909`, `JS202012` | JSON Schema 2019-09, 2020-12 |

---

## Writing a Document

### To a JSON Object

=== "Java"

    ```java
    import com.fasterxml.jackson.databind.node.ObjectNode;

    ObjectNode json = Library.writeDocument(doc);
    ```

=== "TypeScript"

    ```typescript
    const json = Library.writeNode(doc);
    ```

### To a JSON String

=== "Java"

    ```java
    String jsonString = Library.writeDocumentToJSONString(doc);
    ```

=== "TypeScript"

    ```typescript
    const jsonString = JSON.stringify(Library.writeNode(doc), null, 2);
    ```

### Writing a Single Node

You can also serialize any individual node in the tree, not just the root document.

=== "Java"

    ```java
    import com.fasterxml.jackson.databind.node.ObjectNode;

    ObjectNode infoJson = Library.writeNode(doc.getInfo());
    ```

=== "TypeScript"

    ```typescript
    const infoJson = Library.writeNode(doc.getInfo());
    ```

---

## Cloning a Document

Create a deep copy of a document. The clone is fully independent of the original.

=== "Java"

    ```java
    Document clone = Library.cloneDocument(doc);
    ```

=== "TypeScript"

    ```typescript
    const clone: Document = Library.cloneDocument(doc);
    ```

---

## Error Handling

The library uses a typed exception hierarchy rooted at `DataModelsException` (which extends
`RuntimeException` for backward compatibility). When reading a document with an unrecognized
specification version, the library throws `UnsupportedModelTypeException`.

=== "Java"

    ```java
    import io.apitomy.datamodels.UnsupportedModelTypeException;

    try {
        Document doc = Library.readDocumentFromJSONString(json);
    } catch (UnsupportedModelTypeException e) {
        System.err.println("Unsupported spec version: " + e.getMessage());
    }
    ```

=== "TypeScript"

    ```typescript
    try {
        const doc = Library.readDocumentFromJSONString(json);
    } catch (e) {
        console.error('Unsupported spec version:', e.message);
    }
    ```

| Exception | Thrown When |
|-----------|------------|
| `DataModelsException` | Base class for all library exceptions |
| `UnsupportedModelTypeException` | Unknown or unsupported specification version |
| `TransformationException` | Unsupported document transformation (see [Document Transformation](document-transformation.md)) |
| `CommandException` | Command marshalling or unmarshalling failure (see [Commands](commands.md)) |

---

## Extra Properties

API specifications allow vendor extensions (properties prefixed with `x-`). These are
preserved as "extra properties" on each node.

=== "Java"

    ```java
    import com.fasterxml.jackson.databind.node.TextNode;

    // Add a custom extension
    doc.getInfo().addExtraProperty("x-api-owner", new TextNode("platform-team"));

    // Read it back
    String owner = doc.getInfo().getExtraProperty("x-api-owner").asText();

    // List all extensions on a node
    List<String> extensions = doc.getInfo().getExtraPropertyNames();
    ```

=== "TypeScript"

    ```typescript
    // Add a custom extension
    doc.getInfo().addExtraProperty('x-api-owner', 'platform-team');

    // Read it back
    const owner = doc.getInfo().getExtraProperty('x-api-owner');

    // List all extensions on a node
    const extensions: string[] = doc.getInfo().getExtraPropertyNames();
    ```
