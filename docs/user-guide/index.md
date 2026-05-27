# User Guide

This guide covers the core concepts and patterns for working with the Apitomy Data Models
library.

!!! note "Under Development"
    This user guide is under active development. New sections will be added over time.

## Table of Contents

- [Visitor Pattern](#visitor-pattern)
- [Validation](#validation)
- [Commands](#commands)
- [Supported Specifications](#supported-specifications)

---

## Visitor Pattern

The visitor pattern is the primary mechanism for querying, analyzing, and transforming document
models. It replaces direct `instanceof` checks and manual tree walking.

### Base Classes

- **`CombinedVisitorAdapter`** — no-op implementations for all 67+ visit methods. Extend and
  override only the methods you need.
- **`AllNodeVisitor`** — funnels every `visitXxx()` call into a single abstract `visitNode()`.
  Use when you need uniform handling of all node types.

### Traversal

```java
// Traverse a subtree depth-first (top-down)
Library.visitTree(node, visitor, TraverserDirection.down);

// Dispatch to a single node (no traversal)
node.accept(visitor);

// Resolve a NodePath string to a Node
Node node = Library.resolveNodePath(document, nodePath);
```

### Common Patterns

**Finder** — query by criteria:

```java
Library.visitTree(doc, new CombinedVisitorAdapter() {
    @Override
    public void visitOperation(Operation node) {
        if ("getUserById".equals(node.getOperationId())) {
            // Found it
        }
    }
}, TraverserDirection.down);
```

**Collector** — aggregate data across the tree:

```java
List<String> paths = new ArrayList<>();
Library.visitTree(doc, new CombinedVisitorAdapter() {
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        paths.add(node.mapPropertyName());
    }
}, TraverserDirection.down);
```

---

## Validation

The library includes a built-in validation engine with hundreds of rules for detecting problems
in API specifications.

### Basic Usage

```java
List<ValidationProblem> problems = Library.validate(doc, null);
```

Each `ValidationProblem` includes:

- **`severity`** — Error, Warning, Information, or Hint
- **`message`** — Human-readable description of the problem
- **`nodePath`** — Path to the node where the problem was found
- **`errorCode`** — Machine-readable error code

### Severity Levels

| Severity | Description |
|----------|-------------|
| Error | Violations of the specification that must be fixed |
| Warning | Potential issues that should be reviewed |
| Information | Suggestions for improvement |
| Hint | Minor style or convention notes |

---

## Commands

The library provides a command pattern for document mutations that supports undo/redo
operations.

### Using Commands

```java
import io.apitomy.datamodels.cmd.commands.CommandFactory;

// Create a command
ICommand command = CommandFactory.createChangePropertyCommand(
    node, "description", "New description"
);

// Execute it
command.execute(document);

// Undo it
command.undo(document);
```

### Available Command Categories

- **Add** — add paths, schemas, operations, parameters, responses, etc.
- **Delete** — remove any document element
- **Change** — modify properties, rename elements
- **Aggregate** — combine multiple commands into a single undoable operation

---

## Supported Specifications

| Specification | Versions |
|--------------|----------|
| OpenAPI | 2.0, 3.0.x, 3.1.x, 3.2.x |
| AsyncAPI | 2.0–2.6, 3.0, 3.1 |

The library auto-detects the specification type and version when parsing a document.
