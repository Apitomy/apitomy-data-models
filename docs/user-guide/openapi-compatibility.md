# OpenAPI Compatibility

The library includes an OpenAPI compatibility checker for detecting breaking changes between two
versions of an OpenAPI document (2.0, 3.0, 3.1, or 3.2). Unlike the [JSON Schema compatibility
checker](schema-compatibility.md), this checker understands the whole documented contract: paths,
operations, parameters, request/response bodies, security, servers, and (for 3.1/3.2) webhooks --
not just a single schema.

!!! warning "Experimental"
    This API is experimental and subject to change in future versions.

## Verdicts

Every check produces one of three verdicts. There is no boolean shorthand: callers must handle
`INDETERMINATE` explicitly rather than have it silently collapse into a pass or a fail.

| Verdict | Meaning |
|---|---|
| `COMPATIBLE` | Every relevant obligation was established as preserved. |
| `INCOMPATIBLE` | At least one relevant obligation was established as broken. |
| `INDETERMINATE` | No obligation was established as broken, but at least one could not be resolved either way. |

A result also carries every individual `CompatibilityFinding` that contributed to the verdict --
including `COMPATIBLE`-impact findings, so a passing check still shows what was actually checked --
plus `getCoverage()` (which interactions were analyzed) and `getAssumptions()` (which policy defaults
were relied on, such as content-negotiation stability for an added response representation).

## Backward vs. forward, and reverse interactions

**Backward compatible** means the *updated* document can replace the *original* for consumers who
only rely on the original's documented contract. **Forward compatible** is the reverse: can the
*original* replace the *updated* document for the updated document's consumers?

For an ordinary request/response, the "old accepted values" and "who sends first" line up the way
you would expect. A webhook (or, in a future release, a callback) reverses who sends first: the
*provider* sends the webhook request, and the original *consumer* sends the webhook response back.
The checker tracks this automatically -- the same shared schema used in both an ordinary operation
and a webhook is compared with the correct, independently-derived obligation in all four cases:

| Usage | Backward obligation |
|---|---|
| Ordinary request parameters/body | Old values must remain accepted |
| Ordinary response body/headers | New emitted values must remain acceptable to old consumers |
| Webhook request | New emitted values must remain acceptable to old consumers |
| Webhook response | Old values must remain accepted |

A newly added webhook or callback is `INDETERMINATE` by default -- the checker cannot know whether a
brand-new provider-initiated capability is something the caller wants counted as compatible. Opt a
specific interaction in explicitly if you want its addition treated as informational instead (see
[Policy and webhook opt-in](#policy-and-webhook-opt-in) below).

## Quick checks

=== "Java"

    ```java
    import io.apitomy.datamodels.openapi.compat.OpenApiCompatibilityChecker;
    import io.apitomy.datamodels.openapi.compat.CheckOptions;
    import io.apitomy.datamodels.openapi.compat.CompatibilityPolicy;
    import io.apitomy.datamodels.openapi.compat.CompatibilityResult;
    import io.apitomy.datamodels.openapi.compat.CompatibilityVerdict;

    OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder()
            .policy(CompatibilityPolicy.defaults())
            .build();

    CompatibilityResult result = checker.checkBackwardJson(originalJson, updatedJson, CheckOptions.defaults());

    if (result.getVerdict() == CompatibilityVerdict.INCOMPATIBLE) {
        result.getFindings().forEach(finding -> System.out.println(finding));
    }
    ```

=== "TypeScript"

    ```typescript
    import {OpenApiCompatibilityChecker} from "@apitomy/data-models/openapi/compat/OpenApiCompatibilityChecker";
    import {CheckOptions} from "@apitomy/data-models/openapi/compat/CheckOptions";
    import {CompatibilityPolicy} from "@apitomy/data-models/openapi/compat/CompatibilityPolicy";
    import {CompatibilityVerdict} from "@apitomy/data-models/openapi/compat/CompatibilityVerdict";

    const checker = OpenApiCompatibilityChecker.builder()
        .policy(CompatibilityPolicy.defaults())
        .build();

    const result = checker.checkBackwardJson(originalJson, updatedJson, CheckOptions.defaults());

    if (result.getVerdict() === CompatibilityVerdict.INCOMPATIBLE) {
        result.getFindings().forEach((finding) => console.log(finding));
    }
    ```

`checkBackwardJson`/`checkForwardJson` take raw JSON text for both documents. `checkFullJson` runs both
directions over the very same captured snapshot pair and returns a `FullCompatibilityResult` that
preserves each direction's result independently:

```java
FullCompatibilityResult full = checker.checkFullJson(originalJson, updatedJson, CheckOptions.defaults());
full.getBackwardResult();  // CompatibilityResult
full.getForwardResult();   // CompatibilityResult
full.getVerdict();         // INCOMPATIBLE if either direction is; else INDETERMINATE if either is; else COMPATIBLE
```

## Model instance inputs

Every JSON-text entry point has a model-instance counterpart, for callers who already have a parsed
`RootCapable` document (from `Library.readRoot`/`Library.readDocument`, or a document you built or
mutated in memory):

```java
CompatibilityResult result = checker.checkBackward(originalDocument, updatedDocument, CheckOptions.defaults());
```

Every check snapshots its input immediately: the document is serialized to JSON once at the start of
the call, and every later step reads only that captured snapshot. Mutating the model object you
passed in after the call starts (or after it completes) cannot affect that check's result.

## Retrieval URIs and separate old/new resource snapshots

`CheckOptions` carries a distinct retrieval URI for each side (`withOriginalUri`/`withUpdatedUri`),
used both to key each side's resources and to resolve any relative external reference in that
document against its own real base location:

```java
CheckOptions options = CheckOptions.defaults()
        .withOriginalUri("https://api.example.com/v1/openapi.json")
        .withUpdatedUri("https://api.example.com/v2/openapi.json");
```

The two sides have fully independent resource sets: the same URI can resolve to different content on
each side, and a resource acquired for one side is never substituted for the other -- this matters
when comparing two versions of a document that both reference a shared, but independently versioned,
external schema file.

## Asynchronous checking with a resource loader

If your document references external resources (a separate schema file, a shared parameter
definition), use `OpenApiAsyncCompatibilityChecker` with a caller-supplied `AsyncResourceLoader`. The
loader is invoked only for resources actually discovered as reachable -- a document with no external
references never triggers it at all:

=== "Java"

    ```java
    import io.apitomy.datamodels.openapi.compat.OpenApiAsyncCompatibilityChecker;
    import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceLoader;
    import io.apitomy.datamodels.openapi.compat.resource.ResourceDocument;
    import io.apitomy.datamodels.openapi.compat.resource.ResourceRequest;
    import java.util.concurrent.CompletionStage;

    AsyncResourceLoader loader = (ResourceRequest request) -> {
        // Fetch request.getUri() for request.getSide(), returning a
        // CompletionStage<ResourceDocument>. A failed or missing resource
        // becomes a localized RESOURCE_UNRESOLVED finding, not a thrown
        // exception -- one bad external reference never blocks the rest of
        // the check.
        return myHttpClient.fetchAsResourceDocument(request);
    };

    OpenApiAsyncCompatibilityChecker asyncChecker = OpenApiAsyncCompatibilityChecker.builder().build();
    CompletionStage<CompatibilityResult> future =
            asyncChecker.checkBackwardJson(originalJson, updatedJson, CheckOptions.defaults(), loader);
    ```

=== "TypeScript"

    ```typescript
    import {OpenApiAsyncCompatibilityChecker} from "@apitomy/data-models/openapi/compat/OpenApiAsyncCompatibilityChecker";

    const loader = {
        load: (request) => myHttpClient.fetchAsResourceDocument(request), // returns a Promise<ResourceDocument>
    };

    const asyncChecker = OpenApiAsyncCompatibilityChecker.builder().build();
    const result = await asyncChecker.checkBackwardJson(originalJson, updatedJson, CheckOptions.defaults(), loader);
    ```

    The TypeScript checker uses native `Promise`s throughout; there is no `CompletionStage`-equivalent
    wrapper to unwrap.

!!! note "Known limitation: external schema content is not yet consulted"
    Resources the async checker acquires are recorded and available for `RESOURCE_UNRESOLVED`/
    `RESOURCE_INVALID` reporting, but are not yet consulted by schema containment for an external
    `$ref` target's actual content. This is the same underlying gap noted below: schema `$ref` (both
    internal and external) is not dereferenced by the containment engine yet. Acquiring a resource
    therefore does not currently change a schema comparison's outcome.

## Policy and webhook opt-in

`CompatibilityPolicy.defaults()` is the "preserve existing consumers' documented contract" policy. To
treat a specific newly-added webhook or callback as an approved, compatible addition rather than the
default `INDETERMINATE`, opt it in explicitly by its interaction id (never inferred from its schema):

```java
CompatibilityPolicy policy = CompatibilityPolicy.defaults()
        .withOptedInReverseInteractionAddition("webhook POST orderShipped");
```

The policy's identifier is recorded on every result it produces, so a stored result can always be
traced back to the assumptions that were in effect.

## Cross-family and invalid input handling

Comparing an OpenAPI 2.0 document against a 3.x document (or vice versa) is an explicit
`IllegalArgumentException`/thrown error, not a best-effort comparison -- per the approved policy,
cross-family comparison is unsupported, not silently degraded. Patch/minor version differences within
the same family (3.0.0 vs. 3.0.4, for example) are accepted normally. Malformed JSON input is also an
explicit error.

## Numeric precision and format limitations

Exact numeric literals from the source document (large integers, high-precision decimals) are
preserved and compared exactly wherever both sides supplied exact text; where a value's precision
could not be established (for example, a numeric literal that already passed through a lossy
intermediate representation before reaching the checker), the comparison reports
`NUMERIC_PRECISION_UNCERTAIN` rather than silently rounding. `format` (e.g. `int32`/`int64`,
`byte`/`binary`, `date`/`date-time`) is compared separately from JSON Schema validation keywords,
since `format` is a representation annotation, not an assertion: a known-incompatible format change is
reported breaking; an unrecognized or unresolved format pair is reported `FORMAT_RELATION_UNKNOWN`
rather than assumed either way.

## Coverage and known limitations

This is an evolving checker, not a complete implementation of every construct across all four OpenAPI
versions. Constructs the checker does not yet analyze in depth are either recognized (preserved and
identified, but not semantically compared) or bounded (a sound proof rule for the common cases, with an
explicit `INDETERMINATE` finding for anything beyond that) rather than silently treated as compatible.
Notable current limitations:

- **Schema `$ref` is not dereferenced.** A schema usage whose top-level or nested schema is a `$ref`
  (internal or external) is reported as an unresolved schema comparison, not compared against its
  target's actual content. This affects any document that reuses schemas via `components.schemas`
  (a very common pattern) until a future release closes this gap.
- **Discriminator mapping dispatch, `Link` target resolution, and callback runtime-destination
  expressions** are not yet compared for equivalence.
- **XML serialization, multipart part/header encoding, and 3.2 streaming `itemSchema`** are recognized
  but not deeply analyzed for wire-format equivalence.
- **Security trust equivalence** (whether two differently-named `oauth2`/`openIdConnect` schemes, or
  an mTLS condition described only in prose, actually accept the same credentials) is never assumed --
  these compare as `SECURITY_EQUIVALENCE_UNCERTAIN` rather than either compatible or incompatible.

Family parsing support (the document reads and interprets successfully) is not the same claim as
complete semantic analysis of every construct in that family; consult the finding codes in a given
result (particularly any `FEATURE_UNANALYZED`, `*_UNRESOLVED`, or `*_UNCERTAIN` code) to see what a
specific check actually established versus left open.
