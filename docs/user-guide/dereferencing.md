# Dereferencing

Dereferencing inlines external `$ref` references in a document. After dereferencing, every
`$ref` that pointed to an external resource is replaced with the actual content, resulting
in a self-contained document.

## Basic Usage

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;

    // Read a document that may contain external $refs
    Document doc = Library.readDocumentFromJSONString(json);

    // Dereference it — returns a new document with all external refs inlined
    Document dereferenced = Library.dereferenceDocument(doc);
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document } from '@apitomy/data-models';

    // Read a document that may contain external $refs
    const doc: Document = Library.readDocument(json);

    // Dereference it — returns a new document with all external refs inlined
    const dereferenced: Document = Library.dereferenceDocument(doc);
    ```

!!! note
    `dereferenceDocument()` returns a **new** document. The original document is not modified.

---

## Strict Mode

By default, unresolvable references are silently skipped. Enable strict mode to preserve them
as-is (the `$ref` remains in the output).

=== "Java"

    ```java
    Document dereferenced = Library.dereferenceDocument(doc, true);
    ```

=== "TypeScript"

    ```typescript
    const dereferenced = Library.dereferenceDocument(doc, true);
    ```

---

## Custom Reference Resolvers

By default, the library can only resolve local references (e.g.,
`#/components/schemas/Pet`). To resolve external references — pointing to files, URLs, or
other sources — implement `IReferenceResolver`.

### Implementing IReferenceResolver

A resolver receives a reference string and the node it was found on, and returns a
`ResolvedReference` (or `null` if it can't handle that reference).

=== "Java"

    ```java
    import io.apitomy.datamodels.refs.IReferenceResolver;
    import io.apitomy.datamodels.refs.ResolvedReference;
    import io.apitomy.datamodels.models.Node;

    class FileReferenceResolver implements IReferenceResolver {
        @Override
        public ResolvedReference resolveRef(String reference, Node from) {
            if (!reference.startsWith("./")) {
                return null; // not a relative file reference
            }

            // Read the referenced file
            String content = readFile(reference);
            Object json = new ObjectMapper().readTree(content);

            // Return as a JSON reference
            return ResolvedReference.fromJson(json);
        }
    }
    ```

=== "TypeScript"

    ```typescript
    import { IReferenceResolver, ResolvedReference, Node } from '@apitomy/data-models';

    class FileReferenceResolver implements IReferenceResolver {
        resolveRef(reference: string, from: Node): ResolvedReference | null {
            if (!reference.startsWith('./')) {
                return null; // not a relative file reference
            }

            // Read the referenced file
            const content = readFileSync(reference, 'utf-8');
            const json = JSON.parse(content);

            // Return as a JSON reference
            return ResolvedReference.fromJson(json);
        }
    }
    ```

### ResolvedReference Factory Methods

`ResolvedReference` provides three factory methods depending on the type of content:

| Method | Use When |
|--------|----------|
| `ResolvedReference.fromNode(node)` | The reference resolves to another node already in an Apitomy data model |
| `ResolvedReference.fromJson(json)` | The reference resolves to a JSON/YAML object (parsed as a plain object) |
| `ResolvedReference.fromJson(json, mediaType)` | Same as above, with an explicit media type (e.g., `application/vnd.apache.avro+json`) |
| `ResolvedReference.fromText(text, mediaType)` | The reference resolves to non-JSON text (e.g., Protobuf, GraphQL) |

### Registering and Removing Resolvers

Register a resolver globally before dereferencing or validating.

=== "Java"

    ```java
    // Register
    IReferenceResolver resolver = new FileReferenceResolver();
    Library.addReferenceResolver(resolver);

    // Dereference using the registered resolver
    Document dereferenced = Library.dereferenceDocument(doc);

    // Remove when no longer needed
    Library.removeReferenceResolver(resolver);
    ```

=== "TypeScript"

    ```typescript
    // Register
    const resolver = new FileReferenceResolver();
    Library.addReferenceResolver(resolver);

    // Dereference using the registered resolver
    const dereferenced = Library.dereferenceDocument(doc);

    // Remove when no longer needed
    Library.removeReferenceResolver(resolver);
    ```

### Passing a Resolver Directly

Alternatively, pass a resolver directly to `dereferenceDocument()` without registering it
globally.

=== "Java"

    ```java
    Document dereferenced = Library.dereferenceDocument(doc, new FileReferenceResolver(), true);
    ```

=== "TypeScript"

    ```typescript
    const dereferenced = Library.dereferenceDocument(doc, new FileReferenceResolver(), true);
    ```
