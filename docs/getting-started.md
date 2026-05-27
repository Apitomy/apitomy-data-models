# Getting Started

This guide walks you through installing the Apitomy Data Models library and using it to parse,
validate, and manipulate an OpenAPI document.

## Prerequisites

- **Java**: JDK 17 or later (for the Java library)
- **Node.js**: 18 or later (for the TypeScript library)

## Installation

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.apitomy</groupId>
        <artifactId>apitomy-data-models</artifactId>
        <version>3.1.0</version>
    </dependency>
    ```

=== "npm"

    ```bash
    npm install @apitomy/data-models
    ```

## Reading a Document

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;

    // Parse from a JSON string
    Document doc = Library.readDocumentFromJSONString(jsonString);

    // Or parse with auto-detection (JSON or YAML)
    Document doc = Library.readDocument(content);
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document } from '@apitomy/data-models';

    // Parse from a JSON string
    const doc: Document = Library.readDocumentFromJSONString(jsonString);
    ```

## Writing a Document

=== "Java"

    ```java
    // Serialize to a JSON object
    Object json = Library.writeNode(doc);
    String jsonString = new ObjectMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(json);
    ```

=== "TypeScript"

    ```typescript
    // Serialize to a JSON object
    const json = Library.writeNode(doc);
    const jsonString = JSON.stringify(json, null, 2);
    ```

## Validating a Document

=== "Java"

    ```java
    import io.apitomy.datamodels.validation.ValidationProblem;

    List<ValidationProblem> problems = Library.validate(doc, null);
    for (ValidationProblem problem : problems) {
        System.out.println(problem.severity + ": " + problem.message);
    }
    ```

=== "TypeScript"

    ```typescript
    import { Library, ValidationProblem } from '@apitomy/data-models';

    const problems: ValidationProblem[] = Library.validate(doc, null);
    problems.forEach(p => console.log(`${p.severity}: ${p.message}`));
    ```

## Using the Visitor Pattern

The visitor pattern is the primary way to query and traverse documents. Extend
`CombinedVisitorAdapter` and override only the methods you need.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.TraverserDirection;
    import io.apitomy.datamodels.models.Schema;
    import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;

    Library.visitTree(doc, new CombinedVisitorAdapter() {
        @Override
        public void visitSchema(Schema node) {
            System.out.println("Schema: " + node.getTitle());
        }
    }, TraverserDirection.down);
    ```

=== "TypeScript"

    ```typescript
    import {
        Library, TraverserDirection,
        CombinedVisitorAdapter, Schema
    } from '@apitomy/data-models';

    Library.visitTree(doc, new class extends CombinedVisitorAdapter {
        visitSchema(node: Schema): void {
            console.log('Schema:', node.getTitle());
        }
    }, TraverserDirection.down);
    ```

## Next Steps

- [User Guide](user-guide/index.md) — Learn about visitor patterns, validation rules, and
  commands in depth
