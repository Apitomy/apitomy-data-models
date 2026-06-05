# Visitor Pattern

The visitor pattern is the primary mechanism for querying, analyzing, and transforming
document models. It replaces manual tree walking and `instanceof` checks with a structured,
type-safe traversal.

## Overview

The library provides two base visitor classes:

- **`CombinedVisitorAdapter`** — provides no-op implementations for all visit methods
  (e.g., `visitInfo()`, `visitOperation()`, `visitSchema()`). Extend it and override only the
  methods you need. This is the most common choice.
- **`AllNodeVisitor`** — funnels every `visitXxx()` call into a single abstract `visitNode()`
  method. Use when you need uniform handling regardless of node type.

## Traversal Directions

The library supports two traversal directions:

- **`TraverserDirection.down`** — depth-first, top-down. Starts at the given node and visits
  all descendants. This is the most common direction.
- **`TraverserDirection.up`** — bottom-up. Starts at the given node and walks up through
  its ancestors to the root.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.TraverserDirection;

    // Top-down: visit the document and all its children
    Library.visitTree(doc, visitor, TraverserDirection.down);

    // Bottom-up: walk from a node up to the root
    Library.visitTree(someNode, visitor, TraverserDirection.up);

    // Visit a single node (no traversal)
    Library.visitNode(someNode, visitor);
    ```

=== "TypeScript"

    ```typescript
    import { Library, TraverserDirection } from '@apitomy/data-models';

    // Top-down: visit the document and all its children
    Library.visitTree(doc, visitor, TraverserDirection.down);

    // Bottom-up: walk from a node up to the root
    Library.visitTree(someNode, visitor, TraverserDirection.up);

    // Visit a single node (no traversal)
    Library.visitNode(someNode, visitor);
    ```

---

## CombinedVisitorAdapter

`CombinedVisitorAdapter` provides typed visit methods for every node type across all
supported specifications. Override only the methods relevant to your task.

=== "Java"

    ```java
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
            System.out.println("  " + node.getOperationId());
        }
    }, TraverserDirection.down);
    ```

=== "TypeScript"

    ```typescript
    import {
        CombinedVisitorAdapter, OpenApiOperation, OpenApiPathItem
    } from '@apitomy/data-models';

    class EndpointPrinter extends CombinedVisitorAdapter {
        visitPathItem(node: OpenApiPathItem): void {
            console.log('Path:', node.mapPropertyName());
        }
        visitOperation(node: OpenApiOperation): void {
            console.log(' ', node.getOperationId());
        }
    }

    Library.visitTree(doc, new EndpointPrinter(), TraverserDirection.down);
    ```

---

## AllNodeVisitor

`AllNodeVisitor` routes every visit call to a single `visitNode()` method. Use this when
you need to handle all node types uniformly.

=== "Java"

    ```java
    import io.apitomy.datamodels.models.Node;
    import io.apitomy.datamodels.models.visitors.AllNodeVisitor;

    class NodeCounter extends AllNodeVisitor {
        int count = 0;

        @Override
        protected void visitNode(Node node) {
            count++;
        }
    }

    NodeCounter counter = new NodeCounter();
    Library.visitTree(doc, counter, TraverserDirection.down);
    System.out.println("Total nodes: " + counter.count);
    ```

=== "TypeScript"

    ```typescript
    import { AllNodeVisitor, Node } from '@apitomy/data-models';

    class NodeCounter extends AllNodeVisitor {
        count = 0;

        visitNode(node: Node): void {
            this.count++;
        }
    }

    const counter = new NodeCounter();
    Library.visitTree(doc, counter, TraverserDirection.down);
    console.log('Total nodes:', counter.count);
    ```

---

## Common Patterns

### Finder

Locate a specific node by criteria.

=== "Java"

    ```java
    class OperationFinder extends CombinedVisitorAdapter {
        OpenApiOperation found;
        private final String operationId;

        OperationFinder(String operationId) {
            this.operationId = operationId;
        }

        @Override
        public void visitOperation(OpenApiOperation node) {
            if (operationId.equals(node.getOperationId())) {
                found = node;
            }
        }
    }

    OperationFinder finder = new OperationFinder("listPets");
    Library.visitTree(doc, finder, TraverserDirection.down);
    if (finder.found != null) {
        System.out.println("Found: " + finder.found.getSummary());
    }
    ```

=== "TypeScript"

    ```typescript
    class OperationFinder extends CombinedVisitorAdapter {
        found: OpenApiOperation | null = null;

        constructor(private operationId: string) { super(); }

        visitOperation(node: OpenApiOperation): void {
            if (node.getOperationId() === this.operationId) {
                this.found = node;
            }
        }
    }

    const finder = new OperationFinder('listPets');
    Library.visitTree(doc, finder, TraverserDirection.down);
    if (finder.found) {
        console.log('Found:', finder.found.getSummary());
    }
    ```

### Collector

Gather data across the entire tree.

=== "Java"

    ```java
    class SchemaCollector extends CombinedVisitorAdapter {
        List<String> schemaNames = new ArrayList<>();

        @Override
        public void visitSchema(Schema node) {
            if (node.mapPropertyName() != null) {
                schemaNames.add(node.mapPropertyName());
            }
        }
    }

    SchemaCollector collector = new SchemaCollector();
    Library.visitTree(doc, collector, TraverserDirection.down);
    System.out.println("Schemas: " + collector.schemaNames);
    ```

=== "TypeScript"

    ```typescript
    class SchemaCollector extends CombinedVisitorAdapter {
        schemaNames: string[] = [];

        visitSchema(node: Schema): void {
            if (node.mapPropertyName()) {
                this.schemaNames.push(node.mapPropertyName());
            }
        }
    }

    const collector = new SchemaCollector();
    Library.visitTree(doc, collector, TraverserDirection.down);
    console.log('Schemas:', collector.schemaNames);
    ```

### Detecting Extra Properties

Find all vendor extensions (`x-` properties) across the document.

=== "Java"

    ```java
    class ExtensionDetector extends AllNodeVisitor {
        List<String> extensions = new ArrayList<>();

        @Override
        protected void visitNode(Node node) {
            for (String name : node.getExtraPropertyNames()) {
                extensions.add(Library.createNodePath(node) + " -> " + name);
            }
        }
    }

    ExtensionDetector detector = new ExtensionDetector();
    Library.visitTree(doc, detector, TraverserDirection.down);
    detector.extensions.forEach(System.out::println);
    ```

=== "TypeScript"

    ```typescript
    class ExtensionDetector extends AllNodeVisitor {
        extensions: string[] = [];

        visitNode(node: Node): void {
            node.getExtraPropertyNames().forEach(name => {
                this.extensions.push(`${Library.createNodePath(node)} -> ${name}`);
            });
        }
    }

    const detector = new ExtensionDetector();
    Library.visitTree(doc, detector, TraverserDirection.down);
    detector.extensions.forEach(ext => console.log(ext));
    ```

### Walking Up the Tree

Use `TraverserDirection.up` to find ancestors of a node. For example, finding which path
an operation belongs to.

=== "Java"

    ```java
    class PathFinder extends CombinedVisitorAdapter {
        String pathName;

        @Override
        public void visitPathItem(OpenApiPathItem node) {
            pathName = node.mapPropertyName();
        }
    }

    // Given an operation node, walk up to find its parent path
    PathFinder pathFinder = new PathFinder();
    Library.visitTree(operationNode, pathFinder, TraverserDirection.up);
    System.out.println("Operation belongs to path: " + pathFinder.pathName);
    ```

=== "TypeScript"

    ```typescript
    class PathFinder extends CombinedVisitorAdapter {
        pathName: string | null = null;

        visitPathItem(node: OpenApiPathItem): void {
            this.pathName = node.mapPropertyName();
        }
    }

    // Given an operation node, walk up to find its parent path
    const pathFinder = new PathFinder();
    Library.visitTree(operationNode, pathFinder, TraverserDirection.up);
    console.log('Operation belongs to path:', pathFinder.pathName);
    ```
