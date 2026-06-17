# Document Transformation

The library can transform documents between specification versions. This is useful for
upgrading older API definitions to newer versions of the specification.

## Basic Usage

Call `Library.transformDocument()` with the source document and the target `ModelType`.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;
    import io.apitomy.datamodels.models.ModelType;

    // Read an OpenAPI 2.0 (Swagger) document
    Document swagger = Library.readDocumentFromJSONString(swaggerJson);

    // Transform to OpenAPI 3.0
    Document openApi30 = Library.transformDocument(swagger, ModelType.OPENAPI30);

    // Write out the transformed document
    String output = Library.writeDocumentToJSONString(openApi30);
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document, ModelType } from '@apitomy/data-models';

    // Read an OpenAPI 2.0 (Swagger) document
    const swagger: Document = Library.readDocument(swaggerJson);

    // Transform to OpenAPI 3.0
    const openApi30: Document = Library.transformDocument(swagger, ModelType.OPENAPI30);

    // Write out the transformed document
    const output = JSON.stringify(Library.writeNode(openApi30), null, 2);
    ```

!!! note
    `transformDocument()` returns a **new** document. The original document is not modified.

---

## Supported Transformations

### OpenAPI Upgrades

| From | To | Description |
|------|----|-------------|
| OpenAPI 2.0 | OpenAPI 3.0 | Converts `definitions` to `components/schemas`, `basePath` + `host` to `servers`, path-level `parameters` to operation-level, etc. |
| OpenAPI 2.0 | OpenAPI 3.1 | Same as above, plus OpenAPI 3.1-specific changes |
| OpenAPI 2.0 | OpenAPI 3.2 | Same as above, plus OpenAPI 3.2-specific changes |
| OpenAPI 3.0 | OpenAPI 3.1 | Adjusts schema handling for JSON Schema 2020-12 alignment |
| OpenAPI 3.0 | OpenAPI 3.2 | Same as above, plus OpenAPI 3.2-specific changes |
| OpenAPI 3.1 | OpenAPI 3.2 | Minimal changes for 3.2 compatibility |

### AsyncAPI Upgrades

| From | To | Description |
|------|----|-------------|
| AsyncAPI 2.x | AsyncAPI 2.y (y > x) | Incremental upgrades within the 2.x series |
| AsyncAPI 2.x | AsyncAPI 3.x | Cross-major upgrade from any 2.x version to 3.0 or 3.1 |
| AsyncAPI 3.0 | AsyncAPI 3.1 | Upgrade within the 3.x series |

!!! note
    AsyncAPI transformations update the version identifier and re-parse the document using
    the target version's data model. Structural changes between major versions (e.g., the
    channel/operation model differences between 2.x and 3.x) are handled by the library's
    reader, which maps the existing JSON structure into the target model.

---

## Multi-Step Upgrades

You can chain transformations to upgrade through multiple versions. However, a single call
to `transformDocument()` with the final target version is equivalent — the library handles
intermediate conversions internally.

=== "Java"

    ```java
    // Direct upgrade from 2.0 to 3.2 in one step
    Document openApi32 = Library.transformDocument(swagger, ModelType.OPENAPI32);
    ```

=== "TypeScript"

    ```typescript
    // Direct upgrade from 2.0 to 3.2 in one step
    const openApi32 = Library.transformDocument(swagger, ModelType.OPENAPI32);
    ```

---

## Error Handling

Unsupported transformations throw `TransformationException` (a subclass of
`DataModelsException`). For example, transforming an OpenRPC document or downgrading
from a newer version to an older one will throw this exception.

=== "Java"

    ```java
    import io.apitomy.datamodels.TransformationException;

    try {
        Document result = Library.transformDocument(source, ModelType.OPENAPI30);
    } catch (TransformationException e) {
        System.err.println("Transformation not supported: " + e.getMessage());
    }
    ```

=== "TypeScript"

    ```typescript
    try {
        const result = Library.transformDocument(source, ModelType.OPENAPI30);
    } catch (e) {
        console.error('Transformation not supported:', e.message);
    }
    ```

---

## Example: Upgrading a Swagger 2.0 Document

=== "Java"

    ```java
    String swaggerJson = """
        {
          "swagger": "2.0",
          "info": { "title": "Pet Store", "version": "1.0" },
          "host": "api.example.com",
          "basePath": "/v1",
          "schemes": ["https"],
          "paths": {
            "/pets": {
              "get": {
                "operationId": "listPets",
                "produces": ["application/json"],
                "responses": {
                  "200": { "description": "A list of pets" }
                }
              }
            }
          }
        }
        """;

    Document swagger = Library.readDocumentFromJSONString(swaggerJson);
    Document openApi30 = Library.transformDocument(swagger, ModelType.OPENAPI30);

    // The result now has:
    // - "openapi": "3.0.3" instead of "swagger": "2.0"
    // - "servers" array instead of "host" + "basePath" + "schemes"
    // - Response content uses "content" with media types
    String result = Library.writeDocumentToJSONString(openApi30);
    ```

=== "TypeScript"

    ```typescript
    const swaggerJson = {
        swagger: '2.0',
        info: { title: 'Pet Store', version: '1.0' },
        host: 'api.example.com',
        basePath: '/v1',
        schemes: ['https'],
        paths: {
            '/pets': {
                get: {
                    operationId: 'listPets',
                    produces: ['application/json'],
                    responses: {
                        '200': { description: 'A list of pets' }
                    }
                }
            }
        }
    };

    const swagger = Library.readDocument(swaggerJson);
    const openApi30 = Library.transformDocument(swagger, ModelType.OPENAPI30);

    // The result now has:
    // - "openapi": "3.0.3" instead of "swagger": "2.0"
    // - "servers" array instead of "host" + "basePath" + "schemes"
    // - Response content uses "content" with media types
    const result = JSON.stringify(Library.writeNode(openApi30), null, 2);
    ```
