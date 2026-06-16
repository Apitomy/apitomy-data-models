# Getting Started

This guide walks you through installing the Apitomy Data Models library and using it to parse,
validate, and manipulate an OpenAPI document. Choose the Java or TypeScript quickstart below
depending on your platform.

## Prerequisites

- **Java**: JDK 17 or later
- **Node.js**: 20 or later (for the TypeScript/JavaScript library)

## Installation

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.apitomy</groupId>
        <artifactId>apitomy-data-models</artifactId>
        <version>3.1.1</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    implementation 'io.apitomy:apitomy-data-models:3.1.1'
    ```

=== "npm"

    ```bash
    npm install @apitomy/data-models
    ```

---

## Java Quickstart

This section walks through a complete workflow in Java: reading a document, inspecting it,
validating it, traversing it with a visitor, and writing it back out.

### Reading a Document

Pass a JSON string to `Library.readDocumentFromJSONString()`. The library auto-detects the
specification type and version.

```java
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.v3x.OpenApiDocument;

String json = """
    {
      "openapi": "3.0.3",
      "info": {
        "title": "Pet Store",
        "version": "1.0.0"
      },
      "paths": {
        "/pets": {
          "get": {
            "operationId": "listPets",
            "summary": "List all pets",
            "responses": {
              "200": { "description": "A list of pets" }
            }
          }
        }
      }
    }
    """;

Document doc = Library.readDocumentFromJSONString(json);
OpenApiDocument openApiDoc = (OpenApiDocument) doc;
System.out.println("Title: " + openApiDoc.getInfo().getTitle());
// Output: Title: Pet Store
```

### Validating a Document

Call `Library.validate()` to check for specification compliance issues. Pass `null` for the
severity registry to use the defaults.

```java
import io.apitomy.datamodels.validation.ValidationProblem;
import java.util.List;

List<ValidationProblem> problems = Library.validate(doc, null);
for (ValidationProblem problem : problems) {
    System.out.println("[" + problem.severity + "] " + problem.message);
    System.out.println("  at: " + problem.nodePath);
}
```

### Traversing with a Visitor

Use `CombinedVisitorAdapter` to override only the visit methods you care about. Call
`Library.visitTree()` with `TraverserDirection.down` to walk the tree top-down.

```java
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;

Library.visitTree(doc, new CombinedVisitorAdapter() {
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        System.out.println("Path: " + node.mapPropertyName());
    }

    @Override
    public void visitOperation(OpenApiOperation node) {
        System.out.println("  Operation: " + node.getOperationId());
    }
}, TraverserDirection.down);
// Output:
// Path: /pets
//   Operation: listPets
```

### Writing a Document

Serialize the document back to a JSON string.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

ObjectNode jsonNode = Library.writeDocument(doc);
String output = new ObjectMapper()
    .writerWithDefaultPrettyPrinter()
    .writeValueAsString(jsonNode);
System.out.println(output);
```

---

## TypeScript Quickstart

This section walks through the same workflow in TypeScript: reading a document, inspecting it,
validating it, traversing it with a visitor, and writing it back out.

### Reading a Document

Parse the JSON yourself, then pass the object to `Library.readDocument()`. The library
auto-detects the specification type and version.

```typescript
import {
    Library, Document, OpenApiDocument
} from '@apitomy/data-models';

const json = {
    openapi: '3.0.3',
    info: {
        title: 'Pet Store',
        version: '1.0.0',
    },
    paths: {
        '/pets': {
            get: {
                operationId: 'listPets',
                summary: 'List all pets',
                responses: {
                    '200': { description: 'A list of pets' },
                },
            },
        },
    },
};

const doc: Document = Library.readDocument(json);
const openApiDoc = doc as OpenApiDocument;
console.log('Title:', openApiDoc.getInfo().getTitle());
// Output: Title: Pet Store
```

### Validating a Document

Call `Library.validate()` to check for specification compliance issues. Pass `null` for the
severity registry to use the defaults.

```typescript
import { ValidationProblem } from '@apitomy/data-models';

const problems: ValidationProblem[] = Library.validate(doc, null);
problems.forEach(problem => {
    console.log(`[${problem.severity}] ${problem.message}`);
    console.log(`  at: ${problem.nodePath}`);
});
```

### Traversing with a Visitor

Extend `CombinedVisitorAdapter` to override only the visit methods you care about. Call
`Library.visitTree()` with `TraverserDirection.down` to walk the tree top-down.

```typescript
import {
    TraverserDirection, CombinedVisitorAdapter,
    OpenApiOperation, OpenApiPathItem
} from '@apitomy/data-models';

class EndpointVisitor extends CombinedVisitorAdapter {
    visitPathItem(node: OpenApiPathItem): void {
        console.log('Path:', node.mapPropertyName());
    }
    visitOperation(node: OpenApiOperation): void {
        console.log('  Operation:', node.getOperationId());
    }
}

Library.visitTree(doc, new EndpointVisitor(), TraverserDirection.down);
// Output:
// Path: /pets
//   Operation: listPets
```

### Writing a Document

Serialize the document back to a plain JavaScript object, then stringify it.

```typescript
const output = Library.writeNode(doc);
console.log(JSON.stringify(output, null, 2));
```

---

## Next Steps

- [Reading & Writing](user-guide/reading-and-writing.md) — Creating documents, format detection,
  serialization
- [Visitor Pattern](user-guide/visitor-pattern.md) — Traversal directions, finder and collector
  patterns
- [Validation](user-guide/validation.md) — Severity levels, custom severity registries
- [Commands](user-guide/commands.md) — Mutating documents with undo/redo support
- [Examples](examples/index.md) — End-to-end use cases
