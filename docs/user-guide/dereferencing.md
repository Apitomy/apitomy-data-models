# Dereferencing

Dereferencing inlines external `$ref` references in a document. After dereferencing, every
`$ref` that pointed to an external resource is replaced with the actual content, resulting
in a self-contained document.

!!! info "Two dereferencing APIs"
    This page covers **document** dereferencing through `Library`, which applies to OpenAPI,
    AsyncAPI and OpenRPC. JSON Schema has its own dereferencer with different options — see
    [JSON Schema References](#json-schema-references) below. The two do not share an API.

## Basic Usage

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;

    // Read a document that may contain external $refs
    Document doc = (Document) Library.readRootFromJSONString(json);

    // Dereference it — returns a new document with all external refs inlined
    Document dereferenced = Library.dereferenceDocument(doc);
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document } from '@apitomy/data-models';

    // Read a document that may contain external $refs
    const doc = Library.readRoot(json);

    // Dereference it — returns a new document with all external refs inlined
    const dereferenced: Document = Library.dereferenceDocument(doc);
    ```

!!! note
    `dereferenceDocument()` returns a **new** document. The original document is not modified.

---

## Strict Mode

By default, unresolvable references are silently skipped. Enable strict mode to throw a
`DataModelsException` if any external references could not be resolved.

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

---

## JSON Schema References

JSON Schema has its own dereferencer, `JsonSchemaRefDereferencer`. It understands JSON Schema
reference semantics that the document dereferencer above does not — JSON Pointers, anchors, and
`$ref` cycles.

### Basic Usage

Internal references (`#/definitions/...`, `#/$defs/...`, anchors) resolve with no configuration.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.ref.DereferenceResult;
    import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefDereferencer;

    JsonSchemaRefDereferencer dereferencer = JsonSchemaRefDereferencer.builder().build();

    DereferenceResult result = dereferencer.dereference(schema);
    ```

=== "TypeScript"

    ```typescript
    import { JsonSchemaRefDereferencer } from '@apitomy/data-models';

    const dereferencer = JsonSchemaRefDereferencer.builder().build();

    const result = dereferencer.dereference(schema);
    ```

!!! warning "This mutates the schema in place"
    Unlike `Library.dereferenceDocument()`, which returns a new document, this dereferencer
    modifies the schema you pass in. `result.schema()` is the *same instance*, not a copy.
    Clone beforehand if you need the original intact.

    Resolved targets are shared references rather than clones, so the same sub-schema reached
    through two `$ref`s is one object.

### Unresolvable References

The default strategy collects unresolvable references instead of failing:

=== "Java"

    ```java
    DereferenceResult result = dereferencer.dereference(schema);

    for (String unresolved : result.unresolvedRefs()) {
        System.out.println("Could not resolve: " + unresolved);
    }
    ```

=== "TypeScript"

    ```typescript
    const result = dereferencer.dereference(schema);

    result.unresolvedRefs().forEach(unresolved => {
        console.log('Could not resolve:', unresolved);
    });
    ```

A `$ref` that cannot be resolved is left in place. To fail instead, configure the strategy:

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.ref.UnresolvableRefStrategy;

    JsonSchemaRefDereferencer strict = JsonSchemaRefDereferencer.builder()
            .onUnresolvableRef(UnresolvableRefStrategy.FAIL)
            .build();
    ```

=== "TypeScript"

    ```typescript
    import { UnresolvableRefStrategy } from '@apitomy/data-models';

    const strict = JsonSchemaRefDereferencer.builder()
        .onUnresolvableRef(UnresolvableRefStrategy.FAIL)
        .build();
    ```

`FAIL` throws `ReferenceResolutionException` on the first reference it cannot resolve.

### Cycles

Cycles are detected rather than followed. A cyclic back-edge keeps its `$ref` string and is
reported, so you can walk the cycle yourself without re-resolving it.

=== "Java"

    ```java
    if (result.hasCycles()) {
        result.cyclicRefs().forEach((ref, target) ->
                System.out.println("Cycle at " + ref));
    }
    ```

=== "TypeScript"

    ```typescript
    if (result.hasCycles()) {
        console.log('Cyclic references found');
    }
    ```

Recursion is also bounded — the default maximum depth is 256, and exceeding it throws
`DereferenceException`. Adjust it with `maxDepth(int)`.

### External References

External references need a resolver. Compose one from a fragment resolver and a resource
resolver; `MapResourceResolver` serves schemas held in memory.

=== "Java"

    ```java
    import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolverChain;
    import io.apitomy.datamodels.jsonschema.ref.MapResourceResolver;
    import io.apitomy.datamodels.jsonschema.ref.PointerFragmentResolver;

    JsonSchemaRefResolverChain resolver = JsonSchemaRefResolverChain.builder()
            .addFragmentResolver(new PointerFragmentResolver())
            .addResourceResolver(MapResourceResolver.builder()
                    .addSchema("http://example.com/address.json", addressSchemaJson)
                    .build())
            .build();

    JsonSchemaRefDereferencer dereferencer = JsonSchemaRefDereferencer.builder()
            .refResolver(resolver)
            .build();
    ```

=== "TypeScript"

    ```typescript
    import {
        JsonSchemaRefResolverChain, MapResourceResolver, PointerFragmentResolver,
    } from '@apitomy/data-models';

    const fragments = new PointerFragmentResolver();
    const resources = MapResourceResolver.builder()
        .addSchema('http://example.com/address.json', addressSchemaJson)
        .build();

    const chain = JsonSchemaRefResolverChain.builder()
        .addFragmentResolver((ref, targetDocument, context) =>
            fragments.resolveFragment(ref, targetDocument, context))
        .addResourceResolver((resource, context) =>
            resources.resolveResource(resource, context))
        .build();

    const dereferencer = JsonSchemaRefDereferencer.builder()
        .refResolver((ref, context) => chain.resolve(ref, context))
        .build();
    ```

!!! note "The TypeScript resolver API takes functions, not objects"
    `JsonSchemaRefResolver`, `FragmentResolver` and `ResourceResolver` are functional interfaces.
    In TypeScript they are call signatures, so a resolver *class* does not satisfy them
    structurally — pass a function that delegates to the instance, as above. In Java the classes
    implement the interfaces directly and can be passed as they are.

To fetch schemas from somewhere else — a registry, the filesystem, the network — implement
`JsonSchemaRefResolver` (in Java, a functional interface returning `Optional<JsonSchema>`; in
TypeScript, a plain function) and pass it to `refResolver`. The result may be a boolean schema:
a reference to `true` or `false` is inlined like any other target.

`JsonSchemaRefResolverChain.withDefaults()` builds a chain with the pointer and anchor fragment
resolvers and no resource resolvers, which is what the dereferencer uses when you configure none.

A dereferencer can also be handed to the compatibility checker so that comparison runs against
fully inlined schemas — see [Schema Compatibility](schema-compatibility.md#resolving-references).
