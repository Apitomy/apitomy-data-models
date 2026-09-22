# Reading & Writing Documents

This page covers how to parse, create, serialize, and clone documents.

## Reading a Document

The library auto-detects the specification type and version by examining top-level properties
like `openapi`, `asyncapi`, `swagger`, `openrpc`, and `$schema`.

`readRoot` and `readRootFromJSONString` return a `RootCapable` — the supertype of everything
that can sit at the root of a document. For OpenAPI, AsyncAPI and OpenRPC that is a `Document`;
for JSON Schema it is a schema, which is not a `Document`.

### From a JSON String

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.RootCapable;

    String json = "{ \"openapi\": \"3.0.3\", \"info\": { \"title\": \"My API\", \"version\": \"1.0\" } }";
    RootCapable root = Library.readRootFromJSONString(json);
    ```

=== "TypeScript"

    ```typescript
    import { Library, RootCapable } from '@apitomy/data-models';

    const jsonString = '{ "openapi": "3.0.3", "info": { "title": "My API", "version": "1.0" } }';
    const root: RootCapable = Library.readRootFromJSONString(jsonString);
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
    RootCapable root = Library.readRoot(json);
    ```

=== "TypeScript"

    ```typescript
    import { Library, RootCapable } from '@apitomy/data-models';

    const json = JSON.parse(jsonString);
    const root: RootCapable = Library.readRoot(json);
    ```

### Casting to a Specific Type

After reading, cast to the specific type for typed access to its properties.

=== "Java"

    ```java
    import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;

    OpenApi30Document openApiDoc = (OpenApi30Document) root;
    String title = openApiDoc.getInfo().getTitle();
    String version = openApiDoc.getInfo().getVersion();
    ```

=== "TypeScript"

    ```typescript
    import { OpenApi30Document } from '@apitomy/data-models';

    const openApiDoc = root as OpenApi30Document;
    const title = openApiDoc.getInfo().getTitle();
    const version = openApiDoc.getInfo().getVersion();
    ```

A JSON Schema document is read the same way, but its root is a schema rather than a document:

=== "Java"

    ```java
    import io.apitomy.datamodels.models.jsonschema.JFullSchema;

    String schemaJson = "{ \"$schema\": \"https://json-schema.org/draft/2020-12/schema\","
            + " \"type\": \"object\" }";

    JFullSchema schema = (JFullSchema) Library.readRootFromJSONString(schemaJson);
    ```

=== "TypeScript"

    ```typescript
    import { JFullSchema } from '@apitomy/data-models';

    const schemaJson = '{ "$schema": "https://json-schema.org/draft/2020-12/schema", "type": "object" }';

    const schema = Library.readRootFromJSONString(schemaJson) as JFullSchema;
    ```

!!! warning "The `Document`-typed read methods are deprecated"
    `readDocument` and `readDocumentFromJSONString` still work for OpenAPI, AsyncAPI and
    OpenRPC, but **throw `UnsupportedModelTypeException` for JSON Schema**, whose root is not a
    `Document`. Prefer `readRoot` / `readRootFromJSONString`, which work for every
    specification. See the
    [migration guide](../migration/3.x-to-4.0.md#root-document-helpers).

---

## Creating a New Document

Use `Library.createRoot()` with a `ModelType` to create a new, empty document. Then populate it
using typed setters and `createXxx()` factory methods.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.ModelType;
    import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
    import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;

    OpenApi30Document doc = (OpenApi30Document) Library.createRoot(ModelType.OPENAPI30);

    // Set the info section
    doc.setInfo(doc.createInfo());
    doc.getInfo().setTitle("Pet Store");
    doc.getInfo().setVersion("1.0.0");
    doc.getInfo().setDescription("A sample API for pets");

    // Add a path
    doc.setPaths(doc.createPaths());
    doc.getPaths().addItem("/pets", doc.getPaths().createPathItem());

    // Add a GET operation to the path
    OpenApiPathItem pathItem = doc.getPaths().getItem("/pets");
    pathItem.setGet(pathItem.createOperation());
    pathItem.getGet().setOperationId("listPets");
    pathItem.getGet().setSummary("List all pets");
    ```

=== "TypeScript"

    ```typescript
    import { Library, ModelType, OpenApi30Document } from '@apitomy/data-models';

    const doc = Library.createRoot(ModelType.OPENAPI30) as OpenApi30Document;

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

!!! note
    `createDocument` is deprecated for the same reason as `readDocument` — it throws for JSON
    Schema model types.

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
| `JD4`, `JD6`, `JD7` | JSON Schema Draft 4, 6, 7 |
| `JM201909`, `JM202012` | JSON Schema 2019-09, 2020-12 |
| `JC` | JSON Schema compound (all drafts merged — see [Schema Compatibility](schema-compatibility.md)) |

---

## Writing a Document

`writeNode` serializes any node, including a document or schema root, so it is the one method
that works across every specification.

### To a JSON Object

=== "Java"

    ```java
    import com.fasterxml.jackson.databind.node.ObjectNode;

    ObjectNode json = Library.writeNode(doc);
    ```

=== "TypeScript"

    ```typescript
    const json = Library.writeNode(doc);
    ```

### To a JSON String

=== "Java"

    ```java
    String jsonString = Library.writeNodeToString(doc);
    ```

=== "TypeScript"

    ```typescript
    const jsonString = JSON.stringify(Library.writeNode(doc), null, 2);
    ```

### Writing a Single Node

The same method serializes any individual node in the tree, not just the root.

=== "Java"

    ```java
    import com.fasterxml.jackson.databind.node.ObjectNode;

    ObjectNode infoJson = Library.writeNode(doc.getInfo());
    ```

=== "TypeScript"

    ```typescript
    const infoJson = Library.writeNode(doc.getInfo());
    ```

!!! note
    `writeDocument` and `writeDocumentToJSONString` are deprecated. They accept only a
    `Document`, so they cannot serialize a JSON Schema root. `writeNode` and `writeNodeToString`
    replace them.

---

## Cloning a Document

Create a deep copy. The clone is fully independent of the original.

=== "Java"

    ```java
    Document clone = Library.cloneDocument(doc);
    ```

=== "TypeScript"

    ```typescript
    const clone: Document = Library.cloneDocument(doc);
    ```

`cloneDocument` takes a `Document`, so for a JSON Schema root use a `ModelCloner`, which clones
any node:

=== "Java"

    ```java
    import io.apitomy.datamodels.models.io.ModelCloner;
    import io.apitomy.datamodels.models.io.ModelClonerFactory;

    ModelCloner cloner = ModelClonerFactory.createModelCloner(schema.root().modelType());
    JFullSchema clone = (JFullSchema) cloner.cloneNode(schema);
    ```

=== "TypeScript"

    ```typescript
    import { ModelClonerFactory } from '@apitomy/data-models';

    const cloner = ModelClonerFactory.createModelCloner(schema.root().modelType());
    const clone = cloner.cloneNode(schema) as JFullSchema;
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
        RootCapable root = Library.readRootFromJSONString(json);
    } catch (UnsupportedModelTypeException e) {
        System.err.println("Unsupported spec version: " + e.getMessage());
    }
    ```

=== "TypeScript"

    ```typescript
    try {
        const root = Library.readRootFromJSONString(json);
    } catch (e) {
        console.error('Unsupported spec version:', e.message);
    }
    ```

| Exception | Thrown When |
|-----------|------------|
| `DataModelsException` | Base class for all library exceptions |
| `UnsupportedModelTypeException` | Unknown or unsupported specification version, or a deprecated `Document`-typed method called with a JSON Schema root |
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
