# User Guide

This guide covers the core concepts and patterns for working with the Apitomy Data Models
library. Each section focuses on a single feature area with practical examples in both Java
and TypeScript.

## Topics

### [Reading & Writing](reading-and-writing.md)

Parse documents from JSON strings, create new documents programmatically using factory methods,
serialize documents back to JSON, and clone documents.

### [Visitor Pattern](visitor-pattern.md)

Traverse and query document trees using the visitor pattern. Covers `CombinedVisitorAdapter`
for typed visits, `AllNodeVisitor` for uniform handling, traversal directions (top-down and
bottom-up), and common patterns like finders and collectors.

### [Validation](validation.md)

Validate documents against their specification rules. Covers the validation engine, problem
severity levels, error codes, and custom severity registries.

### [Commands](commands.md)

Mutate documents using the command pattern with built-in undo/redo support. Covers creating
commands, executing and undoing them, marshalling commands to/from JSON, and combining
multiple commands into a single undoable operation.

### [Node Paths](node-paths.md)

Navigate documents using XPath-like node paths. Covers path syntax, creating paths from nodes,
parsing paths from strings, and resolving paths back to nodes.

### [Dereferencing](dereferencing.md)

Inline external `$ref` references by resolving them. Covers the built-in dereferencer,
strict mode, and implementing custom reference resolvers for external content.

### [Document Transformation](document-transformation.md)

Transform documents between specification versions, such as upgrading from OpenAPI 2.0 to 3.0
or from OpenAPI 3.0 to 3.1.

### [Schema Compatibility](schema-compatibility.md)

Check backward, forward, and full compatibility between JSON Schema versions. Useful for
detecting breaking changes when evolving API schemas.

---

## Supported Specifications

The library auto-detects the specification type and version when parsing a document.

| Specification | Versions |
|--------------|----------|
| OpenAPI | 2.0, 3.0.x, 3.1.x, 3.2.x |
| AsyncAPI | 2.0, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 3.0, 3.1 |
| OpenRPC | 1.3, 1.4 |
| JSON Schema | Draft 4, Draft 6, Draft 7, 2019-09, 2020-12 |
