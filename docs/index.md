# Apitomy Data Models

A Java and TypeScript library for reading, writing, and manipulating OpenAPI and AsyncAPI
documents using a rich, typed object model.

## Key Features

- **Multi-Spec Support** — OpenAPI 2.0, 3.0, 3.1, 3.2 and AsyncAPI 2.x, 3.x
- **Read & Write** — Parse from JSON or YAML, serialize back to any format
- **Validation** — Built-in validation engine with hundreds of spec-compliance rules
- **Visitor Pattern** — Powerful visitor and traverser patterns for querying and transforming
  document trees
- **Command Pattern** — Undo/redo-capable commands for all document mutations
- **Java & TypeScript** — Available on both Maven Central and npm with the same API surface

## Quick Links

- [Getting Started](getting-started.md) — Installation and basic usage
- [User Guide](user-guide/index.md) — Visitor patterns, validation, commands
- [GitHub Repository](https://github.com/Apitomy/apitomy-data-models) — Source code and issues
- [Maven Central](https://central.sonatype.com/artifact/io.apitomy/apitomy-data-models) — Java
  releases
- [npm](https://www.npmjs.com/package/@apitomy/data-models) — TypeScript releases

## Installation

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.apitomy</groupId>
        <artifactId>apitomy-data-models</artifactId>
        <version>3.1.0</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    implementation 'io.apitomy:apitomy-data-models:3.1.0'
    ```

=== "npm"

    ```bash
    npm install @apitomy/data-models
    ```

## Community

All Apitomy projects are open source under the Apache License 2.0. We welcome contributions,
feedback, and ideas.

- **Issues**: Report bugs and request features on
  [GitHub Issues](https://github.com/Apitomy/apitomy-data-models/issues)
- **Contributing**: See the
  [Contributing Guide](https://github.com/Apitomy/apitomy-data-models/blob/main/CONTRIBUTING.md)
