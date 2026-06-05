# Node Paths

Node paths provide an XPath-like syntax for identifying and navigating to specific nodes
within a document tree. They are used extensively by the command system to reference nodes
and are useful for diagnostics, logging, and programmatic navigation.

## Path Syntax

A node path is a `/`-separated sequence of segments, where each segment is either a property
name, a map key in brackets, or an array index in brackets.

| Path | Meaning |
|------|---------|
| `/` | The root document |
| `/info` | The `info` property of the root |
| `/info/title` | The `title` property of the info object |
| `/paths[/pets]` | The `/pets` entry in the `paths` map |
| `/paths[/pets]/get` | The GET operation on the `/pets` path |
| `/paths[/pets]/get/responses[200]` | The `200` response of that operation |
| `/components/schemas[Pet]` | The `Pet` schema definition |
| `/tags[0]` | The first element in the `tags` array |

---

## Creating a Path from a Node

Given any node in the document tree, create a path that identifies it.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.paths.NodePath;

    NodePath path = Library.createNodePath(doc.getInfo());
    System.out.println(path.toString()); // "/info"

    NodePath schemaPath = Library.createNodePath(
        doc.getComponents().getSchemas().get("Pet")
    );
    System.out.println(schemaPath.toString()); // "/components/schemas[Pet]"
    ```

=== "TypeScript"

    ```typescript
    import { Library, NodePath } from '@apitomy/data-models';

    const path: NodePath = Library.createNodePath(doc.getInfo());
    console.log(path.toString()); // "/info"

    const schemaPath = Library.createNodePath(
        doc.getComponents().getSchemas().get('Pet')
    );
    console.log(schemaPath.toString()); // "/components/schemas[Pet]"
    ```

---

## Parsing a Path from a String

Parse a path string into a `NodePath` object.

=== "Java"

    ```java
    NodePath path = NodePath.parse("/paths[/pets]/get/responses[200]");
    ```

=== "TypeScript"

    ```typescript
    const path: NodePath = NodePath.parse('/paths[/pets]/get/responses[200]');
    ```

---

## Resolving a Path to a Node

Given a `NodePath` and a document, resolve the path to the actual node it points to.

=== "Java"

    ```java
    import io.apitomy.datamodels.models.Node;

    NodePath path = NodePath.parse("/info");
    Node infoNode = Library.resolveNodePath(path, doc);
    System.out.println(infoNode); // the Info node
    ```

=== "TypeScript"

    ```typescript
    import { Node } from '@apitomy/data-models';

    const path = NodePath.parse('/info');
    const infoNode: Node = Library.resolveNodePath(path, doc);
    console.log(infoNode); // the Info node
    ```

---

## Round-Trip

Paths can be round-tripped: create a path from a node, convert it to a string, parse the
string, and resolve it back to the same node.

=== "Java"

    ```java
    // Start with a node
    Node originalNode = doc.getInfo();

    // Create a path
    NodePath path = Library.createNodePath(originalNode);

    // Convert to string and back
    String pathString = path.toString();
    NodePath parsedPath = NodePath.parse(pathString);

    // Resolve back to the same node
    Node resolvedNode = Library.resolveNodePath(parsedPath, doc);
    assert originalNode == resolvedNode; // same object reference
    ```

=== "TypeScript"

    ```typescript
    // Start with a node
    const originalNode = doc.getInfo();

    // Create a path
    const path = Library.createNodePath(originalNode);

    // Convert to string and back
    const pathString: string = path.toString();
    const parsedPath = NodePath.parse(pathString);

    // Resolve back to the same node
    const resolvedNode = Library.resolveNodePath(parsedPath, doc);
    // resolvedNode === originalNode
    ```

---

## Working with Path Segments

A `NodePath` is composed of `NodePathSegment` objects. You can inspect individual segments
to understand the structure of a path.

=== "Java"

    ```java
    NodePath path = NodePath.parse("/paths[/pets]/get/responses[200]");

    for (var segment : path.toSegments()) {
        System.out.println(segment);
    }
    // Output:
    // paths
    // [/pets]
    // get
    // responses
    // [200]
    ```

=== "TypeScript"

    ```typescript
    const path = NodePath.parse('/paths[/pets]/get/responses[200]');

    path.toSegments().forEach(segment => {
        console.log(segment);
    });
    // Output:
    // paths
    // [/pets]
    // get
    // responses
    // [200]
    ```
