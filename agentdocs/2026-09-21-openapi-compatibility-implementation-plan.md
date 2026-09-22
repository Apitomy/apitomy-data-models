# OpenAPI Compatibility Checking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan
> task-by-task. Use `superpowers:subagent-driven-development` only if the user explicitly authorizes
> delegation. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide Java and TypeScript OpenAPI compatibility checking that preserves existing consumers'
documented contracts and reports Compatible, Incompatible, or Indeterminate with actionable diagnostics.

**Architecture:** Interpret each version-specific document as a read-only effective contract, then compare
interactions with explicit HTTP and provider roles. Use a direction-neutral, evidence-bearing schema
containment engine beneath OpenAPI-specific rules. Share the Java 11-compatible semantic core across Java
and TypeScript, with small native bridges for asynchronous acquisition and platform operations.

**Tech Stack:** Existing Maven reactor, Java, Jackson through portable JSON utilities, UMG model generation,
JSweet, TypeScript, JUnit Jupiter using JUnit 5-compatible APIs, and Jest. Retain the current Java build JDK
baseline and dependency-free TypeScript runtime.

**Spec:** The approved design and decisions are reproduced in [Design contract](#design-contract) below.
This file is self-contained; an executor need not reconstruct the preceding conversation.

## Global constraints

- Implement only after the user authorizes implementation. Writing this plan is not that authorization.
- Initial support is Java **and** TypeScript, including synchronous and asynchronous entry points.
- User requirement: "when writing Java code, make sure it is Typescript transpilation compatible by
  limiting yourself to Java 11 compatible source code."
- Use Java 11-compatible syntax and APIs for added/revised feature code and emitted runtime code.
- Keep the repository's Java 17+ build baseline; Java 11 source compatibility does not promise a Java 11 JAR.
- Follow `generator/CLAUDE.md`: explicit types, no pattern-matching `instanceof`, no text blocks, external
  iteration in shared/emitted code, and portable `JsonUtil` calls rather than direct Jackson operations.
- Follow `.claude/rules/jsweet-transpilation.md` for shared sources, generator runtime templates, and adapter
  changes. Java 11 acceptance alone does not establish JSweet compatibility.
- Avoid streams, `Collectors`, `Collections`, collection `copyOf` methods, `Objects.equals/hash/toString`,
  formatting APIs, `System.identityHashCode`, wildcard static imports, and type-based overloads in shared
  code. Use explicit local constructor type arguments and avoid transpiler-reserved names `in` and `ref`.
- Reuse `CollectionUtil` for defensive collection copies. Copies are mutable and shallow; copy at both
  ownership boundaries and snapshot mutable elements where needed to protect results and sessions.
- Prefer erased JVM-only members or adapter substitutions over file exclusions. An isolated native facade
  with a curated TS replacement requires verified import/export behavior; never re-exclude shared packages.
- Do not use `NumberUtil.compare` or tolerance-based divisibility as definitive containment evidence.
  Preserve exact input/provenance through normalization or report relevant numeric uncertainty.
- Keep Java/TypeScript platform helpers synchronized, including any JSweet overload-name aliases.
- Use four-space indentation and Javadoc/docstrings for new public methods.
- Wrap Markdown at 110 characters, except tables and structured content that cannot be safely wrapped.
- Do not introduce JavaScript runtime dependencies or a second handwritten semantic implementation.
- Never edit `target/generated-sources/umg` or `target/ts` as source; change meta-models, generator templates,
  Java sources, or curated TypeScript sources instead.
- Preserve caller-owned documents, resources, and policy objects. Snapshot model inputs when a check starts.
- No implicit network/filesystem acquisition in the semantic core. Acquisition belongs to caller loaders.
- Do not use the legacy JSON Schema boolean verdict as proof of containment.
- Do not silently drop constraints, interpret missing resources as empty schemas, or equate unknown with safe.
- No commits, pushes, or PRs without an explicit request. Review each completed task before continuing.

## Baseline revision after the main update

This plan was reviewed against `22b1d339`, including the following commits pulled after its initial draft:

| Commit | Change | Planning consequence |
|---|---|---|
| `9e3ecb6d` | Enable JSON Schema and generated diff/conversion transpilation | Reuse and verify the portable baseline; do not schedule its initial port again |
| `bbc9bcbb` | Add the JSweet rule guide | Apply its constraints and validate on JDK 17 and 21 |
| `22b1d339` | Update Jest to 30.5.2 | Use the repository version; no architecture change |

`data-models/pom.xml` now excludes only `**/_*/**` from JSweet. JSON Schema compatibility, references,
conversion, and generated diff/conversion visitors are included. `CollectionUtil`, `NumberUtil`, and
`ResourceUtil` are existing utilities, not new feature deliverables.

The portability update deliberately replaces exact bound comparison with doubles and exact divisibility
with a quotient tolerance. This affects Java as well as TypeScript, including converter selection of tighter
bounds. The new containment engine must not inherit those approximations as proofs.

Bundled help/examples now use `ResourceUtil` and compile-time `JacksonAdapter` substitution. Static resource
paths must be absolute resource paths written literally at each call site. This does not load dynamic
external references, which still belong to the caller-loader design.

The pulled commits do not change OpenAPI meta-models or add dedicated JSON Schema TypeScript compatibility
tests. Model fidelity, reference scope, tri-state semantics, sound containment, async acquisition, and
cross-runtime semantic parity remain planned work. Commit-reported clean builds are useful baseline evidence,
not a replacement for the verification milestones below.

---

## Design contract

### Meaning of compatibility

Backward compatibility means an updated provider can replace the original provider without requiring
changes to consumers conforming to the original documented contract. This includes documented capabilities,
wire representations, security mechanisms, addresses, and data guarantees. It is not a promise of identical
business behavior or generated-SDK source compatibility.

Forward compatibility checks the reverse replacement. Full compatibility contains both directional results.

| Usage | Backward containment obligation | HTTP role | Provider role |
|---|---|---|---|
| Ordinary request parameters/body/part headers | Old values are a subset of new accepted values | Request | Input |
| Ordinary response body/headers | New emitted values are a subset of old permitted values | Response | Output |
| Callback/webhook request | New emitted values are a subset of old permitted values | Request | Output |
| Callback/webhook response | Old values are a subset of new accepted values | Response | Input |

HTTP role controls read/write interpretation; provider role controls replacement variance. Keep them
independent. For nested callbacks, derive roles from the initiating interaction instead of assuming every
nesting depth has the same sender.

Each shared schema is evaluated at each effective usage. A component is not inherently an input or output.
Cache logical subresults by semantic context, then attach findings to every usage; never cache away locations.

### Approved default policies

| Topic | Approved behavior |
|---|---|
| Version scope | Same-family 2.0, 3.0.x, 3.1.x, and 3.2.x; patch differences accepted |
| Cross-family checks | Explicit unsupported-comparison error initially |
| Verdicts | Compatible, Incompatible, Indeterminate |
| Closed objects | Honor `additionalProperties: false`, `unevaluatedProperties: false`, and equivalent constraints |
| Documented query parameter removed | Incompatible, even when optional |
| New response status | Incompatible if no original exact/range/default coverage; otherwise compare effective definitions |
| 3.1+ read/write annotations | Conventional request/response interpretation, explicitly recorded as policy |
| Opposite-direction read/write enforcement | Indeterminate when ignored-versus-rejected behavior affects the result |
| Added response representation | Compatible if retained representations remain compatible, assuming negotiation stability |
| Removed response representation | Incompatible when an old consumer loses its documented representation |
| Component scope | Reachable contract from both documents, including implicit security/discriminator connections |
| Formats | Semantic rules for supported formats; presentation hints separate; unknown changed relationships indeterminate |
| Addresses | Preserve effective old addresses; explicit environment mapping/exclusion allowed |
| Added callbacks/webhooks | Indeterminate unless an explicit opt-in policy establishes compatibility |
| Inputs | Model instances and JSON-string convenience methods |
| Execution | Synchronous supplied-resource checks and asynchronous caller-loader checks |
| Runtime architecture | Shared transpiled Java core and small native platform bridges |

Removal of another documented input or reverse-interaction capability follows the same capability-preserving
principle. Findings must say the declared contract changed, not claim the deployed server was observed
failing.
Adding an optional documented parameter is safe for consumers using only documented inputs, provided it does
not collide with existing serialization. This assumption does not override JSON object-property semantics.

An added optional JSON property can narrow an open request schema. Removing an optional response property's
definition can widen its allowed values through additional properties. Do not import the simplistic rule
"optional property additions are always safe" into the engine.

### Results and failure boundaries

Aggregate per direction: a definite incompatible finding wins; otherwise any relevant unresolved obligation
gives Indeterminate; otherwise Compatible. Retain all unresolved findings even when the overall verdict is
Incompatible. A full result aggregates the two directions and preserves each independently.

Invalid root inputs, known specification violations, unsupported comparison families, and invalid checker
configuration are explicit errors. Unknown dialects, unsupported semantic interactions, and unresolved
external resources give localized Indeterminate findings when the remainder can still be analyzed.
An invalid acquired resource blocks its usages with `RESOURCE_INVALID`; it does not invalidate unrelated
operations. Cancellation and unexpected internal failures terminate the asynchronous operation exceptionally.

Public findings contain stable identifiers, both source locations, interaction identity, HTTP/provider roles,
usage and definition locations, relevant values, explanation, and any policy/coverage dependency. Locations
are document URIs plus escaped JSON Pointers. Never render normalized temporary paths as original locations.

Group repeated causes for presentation only; preserve every affected usage. Order findings deterministically.
Record a policy identifier/version and the assumptions actually used. Reporting filters do not recompute a
safer verdict by hiding findings.

### Specification sources

- [Official released-version index](https://spec.openapis.org/oas/)
- [Swagger/OpenAPI 2.0](https://spec.openapis.org/oas/v2.0.html)
- [OpenAPI 3.0.4](https://spec.openapis.org/oas/v3.0.4.html)
- [OpenAPI 3.1.2](https://spec.openapis.org/oas/v3.1.2.html)
- [OpenAPI 3.2.1](https://spec.openapis.org/oas/v3.2.1.html)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12)

As of this plan, 3.2.1 was released on 2026-09-10. Use clarified family semantics, not a patch-string-based
breaking-change rule. Historical Swagger 1.x formats are outside the project and this feature's scope.

## Initial feature-coverage matrix

**Analyzed** means useful definitive rules are required at the initial release boundary, not that every
combination is decidable. **Bounded** means explicit supported proof rules plus Indeterminate elsewhere.
**Recognized** means preserve and identify the feature, but do not claim general semantic analysis.
This proposed release boundary needs review before execution; it makes limitations visible rather than
silently reducing the approved contract.

| Feature | 2.0 | 3.0 | 3.1 | 3.2 | Initial analysis / task |
|---|---|---|---|---|---|
| Paths, operations, inherited parameters | Yes | Yes | Yes | Yes | Analyzed; T11–T13 |
| TRACE, QUERY, additional methods | N/A | TRACE | TRACE | All | Analyzed where defined; T11–T12 |
| Addresses and server overrides | Legacy fields | Yes | Yes | Yes | Literal/finite-template sets analyzed; open templates bounded; T12 |
| Query/path/header/cookie inputs | No cookie | Yes | Yes | Yes | Analyzed, with location-aware pairing; T13 |
| Whole-querystring parameter | N/A | N/A | N/A | Yes | Recognized; equal encoding analyzed, restructuring bounded; T13–T14 |
| Body/form inputs and body presence | Legacy fields | Yes | Yes | Yes | Analyzed; T13 |
| Exact status and default responses | Yes | Yes | Yes | Yes | Analyzed; T15 |
| Status ranges | N/A | Yes | Yes | Yes | Analyzed with exact-code precedence; T15 |
| Content availability and media ranges | consumes/produces | Yes | Yes | Yes | Common HTTP media ranges analyzed; ambiguous parameters bounded; T14–T15 |
| Parameter/header serialization | collectionFormat | style/explode | style/explode | Extended styles | Equality/defaults and concrete changes analyzed; T14 |
| Multipart, forms, XML, binary | Yes | Yes | Yes | Expanded | Common mappings analyzed; complex mappings bounded; T14 |
| Streaming itemSchema and nested/positional encoding | N/A | N/A | N/A | Yes | Preserved; simple item inclusion analyzed, full-stream interactions bounded; T2/T14 |
| Scalar types, enum, const, ranges, lengths | Subset | Subset | Yes | Yes | Analyzed with applicability and satisfiability checks; T7 |
| Numeric formats and decimals | Yes | Yes | Yes | Yes | Symbolic format ranges and exact supplied decimals; precision boundary explicit; T5/T10 |
| Properties, required, additional properties | Yes | Yes | Yes | Yes | Common object fragments analyzed; T8 |
| Pattern properties, dependencies, propertyNames | Nonstandard if used | Nonstandard if used | Yes | Yes | Bounded; no regex-language inclusion claim; T8 |
| Unevaluated properties/items | N/A | N/A | Yes | Yes | Simple cases analyzed; composition-dependent coverage recognized; T8–T9 |
| Homogeneous/tuple arrays, contains bounds | Version subset | Version subset | Yes | Yes | Bounded, including booleans and tuple/tail interaction; T8 |
| allOf/anyOf/oneOf/not/conditionals | Version subset | Version subset | Yes | Yes | Sound sufficient proofs and witnesses; no size heuristics; T9 |
| Local/external references and anchors | Legacy rules | Reference Objects | Schema + object rules | Also $self | Analyzed for static references; T3–T4 |
| Recursive static schema graphs | Yes | Yes | Yes | Yes | Guarded positive recursion bounded; unsupported recursive negation indeterminate; T6/T9 |
| $dynamicRef and custom dialect/vocabulary | N/A | N/A | Yes | Yes | Recognized; scoped identity proofs only initially; T6/T9 |
| readOnly/writeOnly/nullable | readOnly | All | No nullable semantics | No nullable semantics | Family-aware interpretation; T10 |
| Discriminators and mappings | Legacy string | Mapping | Mapping | defaultMapping | Reachability and common dispatch changes analyzed; complex dispatch bounded; T10/T17 |
| Security alternatives, schemes, scopes | Legacy schemes | Yes | Adds mTLS | URI names/device flow | Logical obligations analyzed; trust equivalence bounded; T16 |
| Callbacks/webhooks | N/A | Callbacks | Both | Both | Directional payloads and capability policies analyzed; T17 |
| Links, operation IDs, defaults, extensions | Subset | Yes | Yes | Yes | Separate contract/tooling findings; behavioral unknowns explicit; T17 |
| Unused components | Yes | Yes | Yes | Yes | Excluded from verdict; T11/T17 |
| SDK source compatibility | No | No | No | No | Informational metadata only; no generator-specific promise |
| Cross-family comparison | No | No | No | No | Explicit error; T18 |

Unchanged opaque features are not automatically safe. A local custom keyword might depend on changed fields
or external resources. An equality proof is allowed only when the entire affected effective obligation,
resource closure, and interpretation are unchanged. Otherwise record the unresolved feature.

## Repository map and planned file ownership

All paths are repository-relative. Prefix aliases below are exact path macros used in task file lists.
An entry such as `OA/CompatibilityPolicy.java` means the prefix followed by that literal file name.

| Prefix | Exact path |
|---|---|
| `OA` | `data-models/src/main/java/io/apitomy/datamodels/openapi/compat` |
| `SC` | `data-models/src/main/java/io/apitomy/datamodels/jsonschema/compat/containment` |
| `JT` | `data-models/src/test/java/io/apitomy/datamodels/openapi/compat` |
| `ST` | `data-models/src/main/ts/tests` |
| `TS` | `data-models/src/main/ts/src/io/apitomy/datamodels/openapi/compat` |
| `FX` | `data-models/src/test/resources/fixtures/openapi-compat` |
| `SPEC` | `data-models/src/main/resources/specs/openapi` |
| `GEN` | `generator/src/main/java/io/apitomy/umg/pipe/java` |
| `BASE` | `generator/src/main/resources/base/io/apitomy/umg/base` |

New code is divided by responsibility, not collected into one large visitor:

| Files / group | Responsibility |
|---|---|
| `OA/OpenApiCompatibilityChecker.java`, `OpenApiCompatibilityCheckerBuilder.java` | Immutable public configuration and synchronous entry points |
| `OA/OpenApiAsyncCompatibilityChecker.java` and native TS counterpart | Async acquisition facade; semantic work delegated to the shared core |
| `OA/CompatibilityPolicy.java`, `CheckOptions.java`, `CheckDirection.java` | Immutable policy and per-check configuration |
| `OA/CompatibilityVerdict.java`, `CompatibilityResult.java`, `FullCompatibilityResult.java` | Verdicts and snapshots of results |
| `OA/CompatibilityFinding.java`, `FindingCode.java`, `FindingImpact.java`, `SourceLocation.java` | Stable diagnostics and provenance |
| `OA/CheckSession.java`, `ResourceSide.java`, `HttpRole.java`, `ProviderRole.java` | Per-invocation state and explicit roles |
| `OA/CheckCancellation.java` | Portable cooperative cancellation signal for async acquisition and analysis |
| `OA/resource/ResourceDocument.java`, `ResourceSet.java`, `ResourceRequest.java` | Source documents and side-specific resource identity |
| `OA/resource/ResourceIndex.java`, `ReferenceGraph.java`, `ReferenceTarget.java` | Base scopes, anchors, and typed reference edges |
| `OA/resource/ResourceDiscovery.java`, `ResourceProblem.java`, `ReferenceKind.java` | Missing-resource work queue and resolution diagnostics |
| `OA/resource/AsyncResourceLoader.java`, `AsyncResourceAcquirer.java` | Java-native async boundary; paired TS implementations |
| `OA/contract/ContractDocument.java`, `EffectiveInteraction.java`, `SchemaUsage.java` | Read-only effective contract and usage provenance |
| `OA/contract/ContractInterpreter.java`, `OpenApi20Interpreter.java`, `OpenApi3Interpreter.java` | Family-aware inheritance/default interpretation |
| `OA/contract/InteractionMatcher.java`, `AddressSet.java`, `ResponseSelector.java` | Correspondence and effective address/status coverage |
| `OA/rules/RoutingRules.java`, `InputRules.java`, `RepresentationRules.java` | Routing, input capabilities, wire representations |
| `OA/rules/ResponseRules.java`, `SecurityRules.java`, `ReverseInteractionRules.java` | Output guarantees, authorization, reverse HTTP roles |
| `OA/rules/RelationshipRules.java`, `SchemaRules.java`, `RuleContext.java` | Implicit edges, schema delegation, shared diagnostic context |
| `SC/SchemaContainment.java`, `ContainmentResult.java`, `ContainmentVerdict.java` | Direction-neutral evidence-bearing comparison |
| `SC/SchemaView.java`, `ContainmentContext.java`, `SchemaNormalizer.java` | Dialect-aware schema views, scopes, policies, normalization |
| `SC/ScalarContainment.java`, `ObjectContainment.java`, `ArrayContainment.java` | Focused sound proof rules |
| `SC/CompositionContainment.java`, `WitnessValidator.java`, `SchemaEvidence.java` | Composition, validated counterexamples, explanation provenance |
| `SC/DirectionalSchemaView.java`, `FormatRegistry.java`, `CoverageRegistry.java` | Read/write semantics, supported formats, explicit unknown features |
| `SC/ExactDecimal.java`, `NumericProvenance.java`, `PortableSchemaUtil.java` | Exact decimal arithmetic, precision provenance, portable JSON operations |

Existing source areas affected:

- `SPEC/openapi-3.1.yaml` and `SPEC/openapi-3.2.yaml`: faithful JSON Schema unions/keywords and 3.2 fields.
- `GEN/CreateTraversersStage.java`, `CreateDiffTraversersStage.java`, `CreateConversionTraversersStage.java`:
  union traversal support where generated output demonstrates a gap.
- `GEN/method/reader/ReadUnionMapPropertyBlock.java` and `ReadUnionListPropertyBlock.java`: schema unions.
- Corresponding `GEN/method/writer` and `GEN/method/cloner` blocks if round-trip/clone tests expose gaps.
- `BASE/util/JsonUtil.java` and curated TypeScript `models/util/JsonUtil.ts`: synchronized JSON primitives.
- `data-models/src/main/java/io/apitomy/datamodels/util/CollectionUtil.java`: reuse defensive-copy helpers;
  modify only for a demonstrated shared need, not to create parallel copy utilities.
- `data-models/src/main/java/io/apitomy/datamodels/util/NumberUtil.java`: existing approximate comparison;
  do not route proof-sensitive normalization through it without established exactness conditions.
- `data-models/src/main/java/io/apitomy/datamodels/util/ResourceUtil.java` and
  `data-models/jsweet_extension/io/apitomy/JacksonAdapter.java`: reuse bundled-resource inlining and evaluate
  member-level substitutions for native bridges. Never commit the compiled adapter `.class`.
- `data-models/pom.xml`: verification configuration and any narrowly justified native facade integration;
  existing shared JSON Schema/diff/conversion packages already transpile.
- `data-models/src/main/java/io/apitomy/datamodels/_build/GenerateCoreTs.java`: exports only if existing
  generation cannot expose the new facade correctly; do not hand-edit generated `core.ts`.
- `data-models/src/main/ts/index.ts`: deliberate public async/type exports if required.
- `docs/user-guide/schema-compatibility.md`, new `docs/user-guide/openapi-compatibility.md`, and `mkdocs.yml`.
- `.github/workflows/verify.yaml`: additional parity/Java-11-source checks within existing JDK coverage.

Inspect surrounding code before changes. Do not broaden generator modifications beyond demonstrated failures.

## Interface contracts between tasks

These are proposed API shapes, not implementation code to paste without Javadoc or accessibility review.
Use distinct JSON method names to avoid JSweet overload ambiguity. All collections returned publicly are
defensive snapshots in both runtimes.

### Public synchronous API

```java
public CompatibilityResult checkBackward(OpenApiDocument original, OpenApiDocument updated,
        CheckOptions options);
public CompatibilityResult checkForward(OpenApiDocument original, OpenApiDocument updated,
        CheckOptions options);
public FullCompatibilityResult checkFull(OpenApiDocument original, OpenApiDocument updated,
        CheckOptions options);
public CompatibilityResult checkBackwardJson(String original, String updated, CheckOptions options);
public CompatibilityResult checkForwardJson(String original, String updated, CheckOptions options);
public FullCompatibilityResult checkFullJson(String original, String updated, CheckOptions options);
```

`CheckOptions.defaults()` supplies empty resource sets, no acquisition, and virtual distinct root URIs.
Relative API addresses with no actual retrieval context are unresolved, not falsely equated through virtual
URIs. `CheckOptions` supplies `originalUri`, `updatedUri`, `originalResources`, `updatedResources`, and an
optional `CheckCancellation` signal. The signal exposes `cancel()`, `isCancelled()`, and
`onCancel(Runnable listener)` returning a `Runnable` that unregisters that listener.
The checker builder accepts `CompatibilityPolicy`; `CompatibilityPolicy.defaults()` selects the agreed
policy, identifier `existing-consumer-v1`. Resource URIs and API deployment URLs remain separate concepts.

`CompatibilityResult` exposes `getVerdict()`, `getFindings()`, `getCoverage()`, and `getAssumptions()`.
`FullCompatibilityResult` exposes `getVerdict()`, `getBackwardResult()`, and `getForwardResult()`.
Do not add a boolean that silently collapses Indeterminate into Compatible.

### Asynchronous boundary

```java
public interface AsyncResourceLoader {
    CompletionStage<ResourceDocument> load(ResourceRequest request);
}
```

`ResourceRequest` contains side and absolute retrieval URI. A successful `ResourceDocument` contains its
retrieval/base URI and JSON text or a snapshotted model. A missing resource is an exceptional completion
classified by the acquirer; null results are loader failures, not empty schemas.
Text resources are JSON. A caller loading YAML converts it before returning a resource; native YAML parsing
is not added to the dependency-free runtime by this feature.

Java `OpenApiAsyncCompatibilityChecker` mirrors the six sync methods, returning `CompletionStage` of the
same result types. Its constructor accepts the synchronous checker and loader. The TypeScript surface uses
`Promise` return types and `AsyncResourceLoader.load(request): Promise<ResourceDocument>`.
T4 first evaluates erased JVM-only members and adapter substitutions, following the JSweet guide. If that
cannot preserve clean public declarations, isolate the native facade files and supply curated TypeScript
replacements, verifying all imports and exports before using precise file exclusions. `CompletionStage`
must not enter the shared semantic core. Only orchestration/platform code is duplicated; acquisition state
transitions and graph discovery remain shared.

If curated replacements are required, the named bridge files are `TS/OpenApiAsyncCompatibilityChecker.ts`,
`TS/resource/AsyncResourceLoader.ts`, and `TS/resource/AsyncResourceAcquirer.ts`.

### Shared internal boundaries

```java
CheckSession CheckSession.snapshot(OpenApiDocument original, OpenApiDocument updated,
        CheckOptions options, CompatibilityPolicy policy);
List<ResourceRequest> ResourceDiscovery.discover(CheckSession session);
void CheckSession.addResource(ResourceSide side, ResourceDocument document);
void CheckSession.addResourceProblem(ResourceRequest request, ResourceProblem problem);
ContractDocument ContractInterpreter.interpret(CheckSession session, ResourceSide side);
List<EffectiveInteraction> ContractDocument.getInteractions();
List<SchemaUsage> EffectiveInteraction.getSchemaUsages();
ContainmentResult SchemaContainment.compare(SchemaView source, SchemaView target,
        ContainmentContext context);
void SchemaRules.compareUsage(SchemaUsage original, SchemaUsage updated, RuleContext context);
CompatibilityResult OpenApiCompatibilityChecker.checkSession(CheckSession session,
        CheckDirection direction);
```

`checkSession` is an internal bridge surface, not a second public end-user API. Keep it available to the
curated TS adapter without exporting every implementation class as a promised stable API.

`SchemaView` carries a JSON value, dialect identifier, resource identity, and original location.
`ContainmentContext` carries HTTP/provider roles, direction, policy, resource graph, proof budget, and numeric
provenance. `ContainmentVerdict` has `YES`, `NO`, and `UNKNOWN`; `ContainmentResult` contains evidence and any
validated witness. `SchemaEvidence` carries source/target pointers and the rule that justified its conclusion.

`RuleContext` records findings, delegates schema checks, and retains original/updated direction labels even
when containment operands are reversed. `CoverageRegistry` supplies explicit unsupported reasons rather than
default no-op visitor methods.

`CompatibilityFinding` uses `FindingImpact` values `BREAKING`, `UNRESOLVED`, `COMPATIBLE`, and
`INFORMATIONAL`.
`CompatibilityVerdict` has `COMPATIBLE`, `INCOMPATIBLE`, and `INDETERMINATE`. These are separate enums because
informational diagnostics do not create an extra top-level verdict.

### Stable initial finding identifiers

Define `FindingCode` entries for the following groups; emit only entries supported by implemented rules:

- Analysis: `RESOURCE_UNRESOLVED`, `RESOURCE_INVALID`, `DIALECT_UNSUPPORTED`, `FEATURE_UNANALYZED`,
  `NUMERIC_PRECISION_UNCERTAIN`, `ANALYSIS_LIMIT_REACHED`, `MATCH_AMBIGUOUS`.
- Routing/input: `OPERATION_REMOVED`, `OPERATION_ADDED`, `ADDRESS_REMOVED`, `ROUTE_SHADOWING`,
  `PARAMETER_REMOVED`, `PARAMETER_REQUIRED_ADDED`, `REQUEST_BODY_REQUIRED_ADDED`, `REQUEST_BODY_REMOVED`.
- Representation/output: `SERIALIZATION_CHANGED`, `REQUEST_MEDIA_TYPE_REMOVED`, `RESPONSE_MEDIA_TYPE_REMOVED`,
  `RESPONSE_MEDIA_TYPE_ADDED`, `RESPONSE_STATUS_ADDED`, `RESPONSE_CAPABILITY_REMOVED`,
  `RESPONSE_HEADER_GUARANTEE_REMOVED`.
- Schemas: `SCHEMA_INPUT_NARROWED`, `SCHEMA_OUTPUT_WIDENED`, `SCHEMA_COMPATIBLE`, `SCHEMA_UNRESOLVED`,
  `READ_WRITE_ENFORCEMENT_UNCERTAIN`, `FORMAT_RELATION_UNKNOWN`.
- Security/reverse/relationships: `SECURITY_REQUIREMENT_STRENGTHENED`, `SECURITY_MECHANISM_CHANGED`,
  `CREDENTIAL_FLOW_REMOVED`, `SECURITY_EQUIVALENCE_UNCERTAIN`, `REVERSE_INTERACTION_ADDED`,
  `REVERSE_INTERACTION_REMOVED`, `CALLBACK_DESTINATION_CHANGED`, `DISCRIMINATOR_MAPPING_CHANGED`,
  `LINK_TARGET_UNRESOLVED`, `BEHAVIORAL_DEFAULT_CHANGED`, `EXTENSION_SEMANTICS_UNKNOWN`, `METADATA_CHANGED`.

Code identifies a change/reason; impact and evidence determine the verdict. A name alone must not hard-code
compatibility independent of context as in the legacy `DiffType` boolean design.

## Verification commands and lifecycle conventions

Run from the repository root unless a working directory is specified. Do not run these merely to write this
plan. During implementation, first reproduce a meaningful new test failure, then implement and rerun it.

**V1 — targeted Java compatibility tests:**

```bash
mvn -pl data-models -am '-Dtest=OpenApi*Test,SchemaContainment*Test' -Dsurefire.failIfNoSpecifiedTests=false test
```

**V2 — generator regression and required downstream tests after generator changes:**

```bash
mvn -pl generator -am -Dtest=SyntheticSnapshotTest -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk mvn test -pl data-models -am
```

Preserve the generator guide's command above. If that JDK path is unavailable, explicitly report the
environment issue and use an installed supported JDK only after recording the equivalent invocation.

**V3 — complete Java/TypeScript clean build:**

Explicitly select an installed JDK 17 or 21, set `JAVA_HOME` and `PATH`, and record `mvn -version` before
running the build. The following uses the JDK 21 path documented in the new JSweet guide; substitute an
actual installed path if necessary and record it. Repeat with the installed JDK 17 at release gates.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
sh build.sh
```

This invokes `mvn clean install -Ptranspilation` with the project's JSweet module-opening options.
The existing CI runs the equivalent build on JDK 17 and 21. Native Node/npm versions come from Maven.
A newer JDK alone is insufficient: the guide records that `var` can pass on JDK 25 but crash JSweet on
JDK 17/21. Check both transpiler diagnostics and TypeScript declaration generation, not just bundle output.

If the specific `ClassNotFoundException: io.apitomy.JacksonAdapter` occurs after the adapter-class cleanup,
inspect `data-models/.jsweet/extension.json` and remove only the stale generated `.jsweet` cache before
rebuilding. Do not restore a tracked `.class` or treat arbitrary build failures as cache problems.

**V4 — focused TypeScript tests after generated sources and curated files are refreshed:**

Working directory: `data-models/target/ts`.

```bash
npm test -- --runInBand openapi-compat
```

Run V3 to refresh generated/copied inputs before relying on a packaged-runtime milestone. Do not edit copied
files under `target/ts`. Tests import generated classes directly because `core.ts` is currently generated
at Maven `package`, after the `test` phase. Public-package smoke tests run after packaging.

**V5 — cross-runtime corpus comparison, introduced by T19:**

```bash
mvn -pl data-models -Dtest=OpenApiParityReportTest test
```

Then, working directory `data-models/target/ts`:

```bash
npm test -- --runInBand openapi-compat-parity
```

The Java report is written to `data-models/target/openapi-compat-java-results.json`. The TS parity test reads
it as `../openapi-compat-java-results.json`. T19 configures ordering so the report exists before the Maven
frontend test execution. A missing report fails explicitly; it never skips parity.

**V6 — final source/diff checks:**

```bash
git diff --check
git status --short
```

Use a clean build at milestone boundaries: a prior review found an obsolete catalog in `target/test-classes`
shadowing current resources and causing vacuous catalog passes. T1 adds strict catalog shape validation.

## Task dependency graph and milestones

```text
T1 (contracts/catalog) ── T2 (model fidelity) ── T3 (reference graph) ── T4 (async acquisition)
          │                       │                    │
          └── T5 (portable values) ┴── T6 (schema views/coverage)
                                          │
                                T7 (scalars) ── T8 (objects/arrays)
                                          └──── T9 (composition/recursion)
                                                    │
                                              T10 (roles/formats)
T2/T3/T6 ── T11 (effective contracts) ── T12 (matching/routing)
                                           ├── T13 (inputs)
                                           ├── T14 (representations)
                                           └── T16 (security)
T10/T13/T14 ── T15 (responses)
T12–T16 ── T17 (reverse interactions/relationships)
T1–T17 ── T18 (public API) ── T19 (parity/release verification) ── T20 (documentation)
```

Dependencies express implementation order, not authorization to spawn agents. Prefer inline execution with
review checkpoints. Each task ends with focused green tests and a reviewed diff; commits require permission.

| Milestone | Required evidence |
|---|---|
| M1: faithful portable foundations, after T5 | Round-trip/clone/traversal tests, loader parity, numeric-boundary tests, V2/V3 |
| M2: trustworthy schema boundary, after T10 | Sound yes/no/unknown corpus, counterexamples, recursive-scope tests, V1/V3 |
| M3: effective ordinary contracts, after T16 | Routing/input/output/representation/security cases in both runtimes |
| M4: complete declared surface, after T18 | Reverse interactions, six sync/six async entry points, snapshot/nonmutation tests |
| M5: release readiness, after T20 | Full clean builds, parity, coverage manifest, documentation examples and package smoke tests |

## Implementation tasks

Each numbered step is a small work unit. Where a case table has several rows, execute the test/implementation
cycle one row at a time rather than writing a large unverified subsystem. Code blocks specify concrete
interfaces, fixtures, or algorithms; they are design artifacts and are not production changes in this plan.

### T1 — Result contracts and a non-vacuous shared test catalog

**Dependencies:** None.

**Files:** Create `OA/CompatibilityVerdict.java`, `FindingImpact.java`, `FindingCode.java`,
`CompatibilityFinding.java`, `CompatibilityResult.java`, `FullCompatibilityResult.java`,
`SourceLocation.java`,
`CompatibilityPolicy.java`, `CheckDirection.java`, `ResourceSide.java`, `HttpRole.java`, `ProviderRole.java`;
`JT/OpenApiResultTest.java`, `OpenApiCatalogShapeTest.java`, `OpenApiCaseSupport.java`;
`ST/openapi-compat-results.test.ts`, `openapi-compat-catalog.test.ts`; `FX/cases.json`.
Create `ST/util/openapiCompatCases.ts` for the equivalent TypeScript catalog harness.

**Interfaces:** Produces the result/policy/location types defined above. `OpenApiCaseSupport` reads cases and
requires an explicit expected verdict per tested direction; until T18, catalog entries can target named rule
units instead of a nonexistent public checker. Add shared case execution to that same helper as units land.

- [ ] Write aggregation tests for all verdict combinations, especially breaking plus unresolved findings.
- [ ] Reject cases with missing expectations, unknown finding codes, duplicate IDs, or an empty case list.
  An intentionally unsupported construct is an expected Indeterminate case, not a skipped test.
- [ ] Run V1 and the focused result test; confirm failure is the absent behavior, not malformed test setup.
- [ ] Implement immutable result snapshots and deterministic finding order using the following rule:

```text
aggregate(findings):
    if any finding.impact == BREAKING: return INCOMPATIBLE
    if any finding.impact == UNRESOLVED: return INDETERMINATE
    return COMPATIBLE
sort by interaction ID, usage URI/pointer, code, original URI/pointer, updated URI/pointer
```

- [ ] Seed the catalog with this complete data contract; add expectations independently of checker output:

```json
{
  "cases": [{
    "id": "o30-request-enum-widened",
    "family": "3.0",
    "original": {
      "openapi": "3.0.4", "info": {"title": "Test", "version": "1"},
      "paths": {"/pets": {"get": {
        "parameters": [{"name": "state", "in": "query", "schema": {"enum": ["a"]}}],
        "responses": {"200": {"description": "ok"}}
      }}}
    },
    "updated": {
      "openapi": "3.0.4", "info": {"title": "Test", "version": "2"},
      "paths": {"/pets": {"get": {
        "parameters": [{"name": "state", "in": "query", "schema": {"enum": ["a", "b"]}}],
        "responses": {"200": {"description": "ok"}}
      }}}
    },
    "options": {"originalUri": "https://api.example/openapi", "updatedUri": "https://api.example/openapi"},
    "expected": {
      "backward": {"verdict": "COMPATIBLE", "breakingCodes": [], "unresolvedCodes": []},
      "forward": {"verdict": "INCOMPATIBLE", "breakingCodes": ["SCHEMA_INPUT_NARROWED"],
        "unresolvedCodes": []}
    },
    "reason": "Every old query enum value is still accepted; the reverse direction rejects b."
  }]
}
```

- [ ] Add a Java assertion and equivalent Jest assertion that mutating caller-provided finding lists does
  not mutate a completed result. TypeScript readonly declarations alone are not runtime immutability.
- [ ] Reuse `CollectionUtil.copyOfList/copyOfSet/copyOfMap` at constructors and getters. Test mutation of a
  returned collection and its mutable elements cannot change later getter results. Do not require mutation
  to throw: defensive mutable copies satisfy isolation if internal state remains protected.
  Existing Javadoc saying "unmodifiable" is not evidence of that guarantee. In particular,
  `CompatibilityExample.getDiffTypes()` and `DereferenceResult` collection accessors expose stored mutable
  collections; snapshot them rather than exposing legacy instances through new immutable results.
- [ ] Define catalog loading and expectation assertions now. Add
  `OpenApiCaseSupport.assertCase(String id)` and exported TS `assertCase(id: string)` when the first rule
  runner is connected, then connect all cases end-to-end in T18. Use this regression-test shape once the
  corresponding rule exists:

```java
@Test
public void requestEnumWideningPreservesOldConsumers() {
    OpenApiCaseSupport.assertCase("o30-request-enum-widened");
}
```

```typescript
import {assertCase} from "./util/openapiCompatCases";

test("request enum widening preserves old consumers", () => {
    assertCase("o30-request-enum-widened");
});
```

  Before T18, the helper dispatches to the case's tested rule unit. An unavailable rule is a failing test,
  not a skipped or passing expectation. Use focused unit tests until the relevant rule is available.
- [ ] Keep the now-enabled JSON Schema and generated diff/conversion packages included. Verify the new
  result classes follow the JSweet guide, run V3, and review the public TypeScript declarations. No initial
  legacy-package transpilation port is needed.

### T2 — Preserve all relevant model content before semantic analysis

**Dependencies:** T1.

**Files:** Modify `SPEC/openapi-3.1.yaml`, `SPEC/openapi-3.2.yaml`; generator union reader/writer/cloner and
traverser sources listed in the repository map only when necessary. Create
`JT/OpenApiModelFidelityTest.java`, `ST/openapi-compat-model-fidelity.test.ts`, and `FX/model-fidelity.json`.
Update affected generator synthetic fixture/expected files under
`generator/src/test/resources/io/apitomy/umg/synthetic/` with reviewed generated changes.

**Interfaces:** Produces lossless model read/write/clone behavior and traversable schema unions. Prefer
`typeAliases` with `boolean|Schema` for 3.1/3.2 schema positions, following the standalone JSON Schema
pattern.
Keep the object Schema entity; do not pretend the existing object-only getter represents boolean schemas.
Document generated getter signature changes and review source compatibility before proceeding.

- [ ] Add round-trip, clone, and visitor-path cases for this legal 3.1 schema at every schema-bearing
  position:

```json
{
  "$id": "https://schemas.example/item", "$defs": {"closed": false},
  "type": "object", "properties": {"blocked": false, "free": true},
  "dependentSchemas": {"free": {"required": ["other"]}},
  "if": {"required": ["free"]}, "then": {"properties": {"other": {"type": "string"}}},
  "unevaluatedProperties": false
}
```

- [ ] Assert that serializing a parsed boolean property does not erase it, and that `false` and missing are
  distinct. Include `enum: [null]`, `const: null`, mixed boolean/object composition, boolean array items,
  schema-level `$schema`, anchors, dynamic references, and custom keywords.
- [ ] Run the new Java test against existing readers to reproduce model loss.
- [ ] Add the 2020-12 schema keyword/union surface to both meta-models. Include `prefixItems`, `contains`,
  count bounds, `dependentRequired`, `propertyNames`, content annotations, and unevaluated keywords.
- [ ] Add 3.2 `MediaType` Reference Object support, `prefixEncoding`, `itemEncoding`, and nested Encoding
  fields. Verify response/header schema positions and reusable media types, not just request bodies.
- [ ] Regenerate through Maven. Fix generator union code at its source if tests show traversal, cloning, or
  import problems. Keep stage classes thin and property behavior in code blocks.
- [ ] Run V2 and V3. Verify old 2.0/3.0 model behavior and existing command/transformation tests still pass.
  Review every intentional generated public API change; stop for approval if preserving fidelity requires
  a broader model API break than the new union signatures described here.

### T3 — Side-specific resource indexing and static reference graphs

**Dependencies:** T2.

**Files:** Create `OA/CheckOptions.java`, `CheckSession.java`, `CheckCancellation.java`;
resource types listed in the repository map
except async loader/acquirer; `JT/OpenApiReferenceGraphTest.java`,
`ST/openapi-compat-references.test.ts`, `FX/references.json`.

**Interfaces:** Produces `CheckSession.snapshot`, `ResourceDiscovery.discover`, `ReferenceGraph`, and
`ReferenceTarget`. `ReferenceKind` distinguishes OAS Reference Object, Path Item reference, Schema reference,
security requirement URI, and discriminator mapping. Use existing URI/pointer helpers only after testing
their base-scope behavior. `ResourceIndex.resolve` returns target plus provenance or a localized problem.

- [ ] Test the same absolute resource URI mapping to different old/new documents; internal and external
  pointers containing `~0`/`~1`; relative nested `$id`; anchors; 3.2 `$self`; sibling schema constraints;
  an unresolved resource affecting only one of two operations; and cyclic static references.
- [ ] Test that `$self` affects description reference identity but does not replace retrieval context for
  relative API server URLs. Unknown retrieval location must not be invented as a deployed address.
- [ ] Run V1; implement complete-document indexing before fragment resolution:

```text
index(resource, side):
    preserve retrieval URI and source pointers
    determine OAS family and schema-resource dialect boundaries
    register document identity, schema IDs, static anchors, and dynamic-anchor declarations
    create typed edges without overwriting nodes or inlining cycles
resolve(edge):
    derive its base from side + owning resource + schema scope + reference kind
    apply family-specific sibling rules
    return target/provenance or a ResourceProblem
```

- [ ] Ignore illegal Reference Object siblings according to family rules, retain applicable 3.1+ schema
  siblings conjunctively, and flag conflicting Path Item reference fields as unresolved semantics.
- [ ] Implement cycle-safe discovery using side/resource identity and a visited expansion state. Do not
  conclude two unresolved references are compatible merely because their strings match.
- [ ] Reuse the new portable identity-ID pattern only where keys have identity equality. Test structurally
  equal but distinct nodes, old/new instances at the same URI, and wrappers with value equality. New semantic
  classes may override equals/hashCode, so an ordinary map is not universally an identity map.
- [ ] Scope node-ID maps, resource caches, and visited state to the check session and release them with it.
  `JsonSchemaRefTraversal` now stores document IDs and cached nodes on the traversal instance; do not place
  a reused instance on an immutable checker and assume its state is per invocation. Nested `$id` base-URI
  resolution remains unfinished in that class and must be tested before any reuse.
- [ ] Deep snapshot supplied models and resources without modifying parent links or attributes on originals.
- [ ] Run V1/V4 and verify resource-location diagnostics and snapshot isolation in both runtimes.

### T4 — Native async bridges over shared resource discovery

**Dependencies:** T3.

**Files:** Create `OA/resource/AsyncResourceLoader.java`, `AsyncResourceAcquirer.java`;
create `TS/resource/AsyncResourceLoader.ts` and `AsyncResourceAcquirer.ts` if the native-facade approach wins
the feasibility gate;
`JT/OpenApiAsyncAcquisitionTest.java`, `ST/openapi-compat-async-acquisition.test.ts`.
Modify `OA/CheckCancellation.java` as acquisition integration requires. Evaluate
`data-models/jsweet_extension/io/apitomy/JacksonAdapter.java` for member substitutions and use
`data-models/pom.xml` exclusions only for verified isolated native files if necessary.

**Interfaces:** `AsyncResourceAcquirer.acquire(CheckSession, AsyncResourceLoader)` returns
`CompletionStage<CheckSession>` in Java and `Promise<CheckSession>` in TypeScript. Loader request/result
contracts are defined above. The acquirer does not run compatibility rules.

- [ ] Write fake-loader tests for nested relative resources, one load per side/URI, failures, cycles,
  opposite-side isolation, and completion after all reachable resources are resolved or recorded missing.
- [ ] Test immediate and delayed loader completions; a 1,000-resource chain must not overflow the stack.
  Preserve failures in the session so discovery does not retry a permanently failed URI indefinitely.
- [ ] Run the tests; implement this iterative state machine with thin native completion handling:

```text
while discovery yields unattempted resource requests:
    mark side/URI as attempted before invoking the loader
    acquire the batch with bounded in-flight requests
    snapshot successful results and record acquisition problems
    index newly available documents
return the completed session
```

  Put attempted/in-flight/completed/failed request state in shared `CheckSession`/`ResourceDiscovery`.
  Native acquirers only schedule requests and translate completion/cancellation; neither adapter independently
  decides which references are reachable or how failures affect analysis coverage.

- [ ] Use Java 11 `CompletionStage`/`CompletableFuture` only in native adapter files. Use native TS promises
  only in platform bridge code. Never block a browser thread or implement semantic rules in the adapters.
- [ ] First test erasure of JVM-only members with adapter substitution while preserving transpiled import
  targets. Inspect emitted declarations for Java-native type leakage. If this fails, test isolated native
  facade files with curated TS replacements and exact exclusions. Record the selected approach and update
  the bridge file map before downstream work; never solve this by excluding shared callers or packages.
- [ ] Configure resource-count and expansion-depth budgets as check options. Exhaustion becomes localized
  `ANALYSIS_LIMIT_REACHED`, not an arbitrary no-change result. Loader implementations own transport timeouts;
  document that a loader that never completes prevents completion unless the caller cancels its work.
- [ ] Preserve cancellation as exceptional completion rather than an Indeterminate compatibility result.
  Race pending acquisition against the shared cancellation signal so cancellation does not wait for a
  non-completing loader. Unregister listeners on completion and ignore late results after cancellation.
  This cancels the check; aborting transport is the caller loader's responsibility. Test this in Java and TS.
  Record unexpected programming exceptions separately from ordinary retrieval failures.
- [ ] Run V3 and inspect generated/public TS types. This is the async feasibility gate: if the thin bridge
  cannot be exported cleanly, revise the adapter boundary before building downstream semantics.

### T5 — Portable JSON semantics, numeric provenance, and Java 11 checks

**Dependencies:** T1; integrate with T2/T3 snapshots.

**Files:** Create `SC/ExactDecimal.java`, `NumericProvenance.java`, `PortableSchemaUtil.java`;
`JT/OpenApiPortableValuesTest.java`, `OpenApiJava11SourceTest.java`;
`ST/openapi-compat-values.test.ts`; `FX/numeric-precision.json`.
Modify `BASE/util/JsonUtil.java` and the curated TypeScript `models/util/JsonUtil.ts` together if primitives
for distinguishing missing/null or observing numeric tokens are required.

**Interfaces:** `ExactDecimal.parse(String)`, `compareTo(ExactDecimal)`, and
`isIntegralMultipleOf(ExactDecimal)` use decimal strings/integer digit operations, not JS floating remainder.
`NumericProvenance` maps resource pointer to an exact token or uncertainty; `PortableSchemaUtil` performs
structural JSON equality with unordered object keys and ordered arrays.
The existing `NumberUtil.compare` and private `DiffUtil.isMultipleOf` are approximate baseline behavior,
not substitutes for these proof-bearing operations. This task adds exact reasoning for the new engine;
changing the legacy checker's published behavior is a separately reviewed decision.

- [ ] Test exact comparisons and divisibility with the following independent expectations:

```text
parse("0.3").isIntegralMultipleOf(parse("0.1")) == true
parse("9007199254740993").compareTo(parse("9007199254740992")) > 0
parse("1.0000000001").isIntegralMultipleOf(parse("1")) == false
parse("1e2").compareTo(parse("100.0")) == 0
JSON equality({"a": 1, "b": null}, {"b": null, "a": 1.0}) == true
JSON equality(missing, null) == false
```

- [ ] Reproduce the native JS precision-loss case before choosing a bridge implementation. Use a lexical
  numeric-token scanner for checker JSON-string/resource-text inputs, associated with JSON Pointers; it must
  honor escaped keys, strings containing numbers, arrays, and exponents. The ordinary parser still validates
  JSON. Do not change numbers into strings in public schema data.
- [ ] For already-built model inputs, treat finite safe integers as exact. Mark fractional or unsafe numeric
  values without exact provenance as uncertain wherever original precision matters. Apply this conservative
  provenance boundary consistently in Java and TS, even if Java happens to retain a wider `Number` subtype.
  Allow supplied exact provenance only when it is validated against the model and invalidated after edits.
- [ ] Use exact tokens for numeric enum/const values and constraints, not just minimum/maximum. Report
  `NUMERIC_PRECISION_UNCERTAIN` for affected obligations instead of comparing rounded values as equal.
- [ ] Add characterization tests showing the baseline double comparator collapses adjacent integers above
  the safe range and the `1e-9` quotient tolerance accepts a nonintegral near-integer quotient. Pair these
  with new-engine tests requiring the exact verdict with text provenance or Indeterminate without it.
- [ ] Capture tokens and original numeric constraints before invoking any existing compound converter.
  Those converters now use `NumberUtil.compare` to choose tighter bounds; approximate normalization can
  discard a necessary constraint. Use exact bound selection or preserve both constraints plus uncertainty.
  Test `minimum: 9007199254740993` with `exclusiveMinimum: 9007199254740992`: selecting the exclusive
  bound because the doubles compare equal must not lose the stronger exact minimum.
- [ ] Avoid calling `new BigDecimal(jsNumber.toString())` as a claimed recovery of the original token.
  Keep symbolic int64 limits as exact decimal strings.
- [ ] Add a JavaCompiler-based source test using `--release 11` for the explicit new/shared source manifest
  against the built dependency classpath. Include emitted helpers used by the feature; do not globally
  downgrade the project's compiler release. Run this with JDK 17 and 21.
- [ ] Test the existing RegexUtil Java/TS matching semantics before reusing it. Schema patterns use search
  semantics; whole-string Java `Pattern.matches` is not an equivalent default. Unknown dialect features or
  unproved regex inclusion yield Unknown, not Java-specific conclusions.
- [ ] Run V1/V3. This closes M1 only after model fidelity, async bridges, and precision limitations are
  tested.

### T6 — Dialect-aware schema views and explicit coverage

**Dependencies:** T2, T3, T5.

**Files:** Create `SC/SchemaView.java`, `ContainmentContext.java`, `SchemaNormalizer.java`,
`ContainmentVerdict.java`, `ContainmentResult.java`, `SchemaEvidence.java`, `SchemaContainment.java`,
`CoverageRegistry.java`; `JT/SchemaContainmentCoverageTest.java`,
`ST/openapi-compat-schema-coverage.test.ts`; `FX/schema-coverage.json`.
The containment package and its existing JSON Schema/generated dependencies are already included by the
current POM. Reuse portable helpers after semantic verification; do not reintroduce legacy-package
exclusions or repeat the completed source-syntax port.

**Interfaces:** Produces the `SchemaContainment.compare` boundary. `CoverageRegistry` returns the known
assertions, annotations, schema children, and unsupported semantic reasons for a dialect. No default no-op
means supported. Views accept object and boolean schemas without requiring a `JFullSchema` root cast.

- [ ] Test true/false roots and nested booleans; supported family dialect defaults; `$schema` resource-root
  overrides; document `jsonSchemaDialect`; and a custom required vocabulary producing Unknown.
- [ ] Test OAS 3.0 `nullable` only where a sibling `type` exists. Preserve enum and composition constraints
  that can still exclude null. Do not make a Reference Object's ignored nullable sibling effective.
- [ ] Normalize legacy exclusive bounds into value/exclusivity pairs, retaining the tighter simultaneous
  bound in modern dialects. Preserve original provenance for every normalized value.
  Apply T5's exact selection rules before reusing converter output. A rounded comparator result cannot
  establish bound equality, redundancy, or schema containment.
- [ ] Implement conservative dispatch:

```text
compare(source, target, context):
    establish applicable dialects and reference scopes
    use a closed-obligation identity proof only when semantics/resources are unchanged
    attempt supported containment proofs or validated counterexamples
    return UNKNOWN for relevant unsupported interactions and exhausted proof budgets
```

- [ ] Recognize `$dynamicRef`/custom vocabulary scope without attempting static string substitution.
  An unsupported keyword is ignored only if its irrelevance to the obligation is established.
- [ ] Keep assertion applicability by instance type; a string pattern does not require string instances.
  Preserve type-independent enum/const/composition restrictions.
- [ ] Run V1/V4. Verify known unsupported inputs cannot produce a vacuous Compatible result.

### T7 — Scalar containment with sound proof and counterexample evidence

**Dependencies:** T6.

**Files:** Create `SC/ScalarContainment.java`, `WitnessValidator.java`;
`JT/SchemaContainmentScalarTest.java`, `ST/openapi-compat-schema-scalars.test.ts`;
`FX/schema-scalars.json`. Extend `SchemaContainment` and `SchemaEvidence`.

**Interfaces:** `ScalarContainment.compare(SchemaView, SchemaView, ContainmentContext)` returns a
`ContainmentResult`. `WitnessValidator.validate(JsonNode, SchemaView, ContainmentContext)` returns YES, NO,
or UNKNOWN for supported schema validation; a NO containment witness requires source YES and target NO.

- [ ] Add each row as a separate test cycle:

| Source | Target | Expected | Reason |
|---|---|---|---|
| integer | number | YES | Integer values are numbers |
| number | integer | NO | Witness 0.5 |
| enum [a] | enum [a,b] | YES | Finite inclusion |
| enum [a,b] | enum [a] | NO | Witness b |
| string minLength 5 | string minLength 10 | NO | Witness abcde |
| number minimum 10 | number minimum 5 | YES | Interval containment |
| integer minimum 10 maximum 5 | false | YES | Empty source |
| number multipleOf 0.3 | number multipleOf 0.1 | YES | Exact decimal divisibility |
| string pattern ^a | string pattern a | UNKNOWN or YES with explicit supported proof | No general regex inclusion shortcut |

- [ ] Run the new failing tests. Implement type-set inclusion, integer subset rules, structural enum/const
  equality, exact intervals, length ranges, and exact multipleOf proofs under supported interactions.
- [ ] Never stop after a type widening if another target constraint can reject source values. Check
  satisfiability before classifying a local tightening as a definite break.
- [ ] For finite enum sources, validate each value against the whole target and the source; irrelevant enum
  members excluded by other source constraints are not counterexamples.
- [ ] Use the following evidence rule for NO:

```text
if supported mathematical proof establishes source is not a subset:
    return NO with the proof's applicability conditions
if validate(candidate, source) == YES and validate(candidate, target) == NO:
    return NO with candidate
otherwise a failed sufficient proof returns UNKNOWN, never NO by default
```

- [ ] Run V1/V4. Add numeric equality cases with reordered object enum values and null versus missing.
- [ ] Include near-integer multipleOf and adjacent-large-bound cases from T5 in full schema comparisons,
  not only arithmetic-helper tests. YES/NO evidence must survive normalization and all source constraints.

### T8 — Objects, arrays, and evaluated-location constraints

**Dependencies:** T7.

**Files:** Create `SC/ObjectContainment.java`, `ArrayContainment.java`;
`JT/SchemaContainmentObjectArrayTest.java`, `ST/openapi-compat-schema-objects-arrays.test.ts`;
`FX/schema-objects-arrays.json`. Extend witness validation for the supported fragments.

**Interfaces:** Each containment unit implements the same compare signature as `ScalarContainment`.
Track effective allowed properties/items and evaluated locations; do not conflate named definitions with
actual instance presence or with properties evaluated by composition.

- [ ] Test an open object gaining optional string property `email`: NO from old to new using `{"email":123}`.
- [ ] Test an old closed response gaining optional `email`: NO from new to old using a string-valued email.
- [ ] Test requiredness in both directions, a removed optional definition with open extras, and an
  additional-properties schema that still accepts a newly named property's old values.
- [ ] Test homogeneous boolean items, tuple positions, tail schema transitions, min/max length, uniqueness,
  contains count bounds, and the interaction between 2020-12 prefixItems and items.
- [ ] Implement object proofs against the conjunction of matching named/pattern/additional constraints:

```text
for each possible source property class relevant to the target:
    determine source allowed values and target obligations
    account for source/target requiredness and object-size feasibility
    compare effective value schemas recursively
    preserve any UNKNOWN pattern coverage or dependency interaction
```

- [ ] Implement array proofs over feasible lengths and positions; a tuple position beyond the maximum
  possible length does not create a real counterexample. Account for item-level false schemas.
- [ ] Support simple `unevaluatedProperties: false`/`unevaluatedItems: false` where evaluated locations are
  established. Complex composition-dependent annotation coverage gives UNKNOWN. Do not rewrite every
  unevaluated keyword into additionalProperties/additionalItems.
- [ ] Check dependentRequired and simple dependentSchemas implications; validate object witnesses against
  every source constraint before using them as NO evidence.
- [ ] Run V1/V4, including boolean-schema regressions from T2.

### T9 — Composition and recursive proof discipline

**Dependencies:** T7, T8.

**Files:** Create `SC/CompositionContainment.java`;
`JT/SchemaContainmentCompositionTest.java`, `ST/openapi-compat-schema-composition.test.ts`;
`FX/schema-composition.json`. Extend `ContainmentContext`, `WitnessValidator`, and `SchemaContainment`.

**Interfaces:** Context tracks ordered source/target resource identities, dialect, roles, policy, dynamic
scope, and proof state. Distinguish active recursion assumptions from completed cached conclusions.

- [ ] Test `oneOf` addition with overlapping branches: integer matches both integer and number, so adding a
  number branch can invalidate an existing integer. Branch-count widening must never prove YES.
- [ ] Test permutations, duplicates, constraint interactions across allOf, not reversal, conditionals, and
  recursive shapes with a changed leaf. A cycle guard must not hide that changed leaf.
- [ ] Implement these sound sufficient rules, falling back to UNKNOWN when their premises cannot be proved:

```text
S subset (T1 AND T2) if S subset T1 and S subset T2
(S1 OR S2) subset T if S1 subset T and S2 subset T
S subset (T1 OR T2) if S subset either branch
(S1 AND S2) subset T if either source conjunct is a subset of T
NOT S subset NOT T if T subset S
```

- [ ] Do not infer non-containment merely because sufficient branch matching fails. For oneOf, require
  proved exclusivity or a validated witness; additions/removals alone are insufficient evidence.
- [ ] Implement guarded positive-recursion comparison using a worklist/fixed-point proof obligation graph.
  Reject a provisional YES if any dependent obligation is NO or UNKNOWN. Recursive negation, dynamic-scope
  changes, and unsupported conditional reasoning return UNKNOWN within the configured budget.
- [ ] Preserve identical complete recursive-contract proofs where justified without flattening references.
- [ ] Test repeated comparisons of equal-but-distinct graphs under different roles/policies. Portable
  node-ID assignment is only graph identity infrastructure, not a completed proof cache. Keep active
  obligations distinct from finalized outcomes and discard session identity maps after each check.
- [ ] Run V1/V3 and retain independently reasoned expectations rather than regenerating verdicts from output.

### T10 — Directional schema interpretation, formats, and discriminators

**Dependencies:** T9.

**Files:** Create `SC/DirectionalSchemaView.java`, `FormatRegistry.java`;
`JT/SchemaContainmentDirectionTest.java`, `ST/openapi-compat-schema-direction.test.ts`;
`FX/schema-direction.json`. Extend schema normalization and policy types.

**Interfaces:** `DirectionalSchemaView.of(SchemaView, HttpRole, CompatibilityPolicy)` returns a view with
directional obligations without destructive rewriting. `FormatRegistry` returns known value/representation
relations or UNKNOWN; symbolic ranges use `ExactDecimal`.

- [ ] Test the same component as ordinary request/response and webhook request/response. Add request enum
  members and verify the four direction outcomes independently.
- [ ] Test OAS 2 readOnly prohibition; OAS 3.0 directional requiredness; 3.1/3.2 conventional interpretation;
  readOnly required through allOf; and writeOnly response enforcement uncertainty.
- [ ] Implement projection of obligations, not property deletion. Preserve additionalProperties and
  dependency interactions; branch-dependent annotations that cannot be resolved remain UNKNOWN.
- [ ] Test supported formats with explicit bounds:

```text
int32 input -> int64 input: compatible
int32 output -> int64 output: incompatible if wider outputs remain feasible
int32 output -> int64 output + explicit int32 bounds: compatible
password hint added: informational
unknown format A -> unknown format B: unresolved
```

- [ ] Implement int32/int64 ranges, defined date/date-time string handling, UUID where a precise supported
  definition is selected, and legacy binary/byte interpretation in representation context. Do not equate
  all registered formats with validation assertions or assume float/double representation equivalence.
- [ ] Preserve discriminator mappings as semantic edges and interpretation metadata. Modern discriminators
  cannot change JSON Schema validation outcomes. Legacy 2.0 dispatch semantics are interpreted separately.
- [ ] Run V1/V3. Close M2 only when unsupported annotations, formats, and composition cannot silently pass.

### T11 — Family-aware effective contract interpretation

**Dependencies:** T3, T6; T10 before schema-based rule integration.

**Files:** Create the `OA/contract` interpretation/view files listed above except matcher/address/selector;
`JT/OpenApiEffectiveContractTest.java`, `ST/openapi-compat-contract.test.ts`;
`FX/effective-contract.json`.

**Interfaces:** Produces `ContractInterpreter.interpret`, `ContractDocument.getInteractions`, and
`EffectiveInteraction.getSchemaUsages`. Views retain source pointers and the effective setting's declaration
location. `SchemaUsage` contains schema view, interaction ID, HTTP role, provider role, media type, and usage
location. Raw typed getters and preserved extras are interpreted through a single family adapter.

- [ ] Test moving a parameter from an operation to its Path Item and back without changing effective
  behavior; list reordering; component extraction; and operation-level overrides.
- [ ] Test absent versus empty security, root/path/operation server precedence, and Swagger operation
  consumes/produces/schemes overrides. Do not treat root parameter/response definitions as globally applied.
- [ ] Implement these effective rules:

```text
parameters = path parameters keyed by semantic identity, replaced by operation entries of that identity
security = operation declaration if present, otherwise root declaration
servers = operation declaration if present, else path declaration if present, else root/default
legacy media/schemes = operation declaration if present, otherwise root declaration
```

- [ ] Materialize default style/explode/required/allowReserved values using family and location. Normalize
  header case for identity without lowercasing query names or case-sensitive custom HTTP methods.
- [ ] Discover reachable usages from both sides. Keep definition-only changes out of verdicts unless
  implicit connections make them relevant. Include component-only documents as empty API surfaces, with
  an explicit coverage note rather than pretending to check their published component-library contracts.
- [ ] Recognize invalid conflicting or duplicate declarations. Invalid declared fields are input errors;
  specification-defined ambiguous behavior gives localized uncertainty.
- [ ] Run V1/V4, checking effective views and provenance rather than snapshotting incidental object IDs.

### T12 — Operation correspondence, deployment addresses, and route overlap

**Dependencies:** T11.

**Files:** Create `OA/contract/InteractionMatcher.java`, `AddressSet.java`, `OA/rules/RuleContext.java`,
`RoutingRules.java`; `JT/OpenApiRoutingTest.java`, `ST/openapi-compat-routing.test.ts`;
`FX/routing.json`.

**Interfaces:** `InteractionMatcher.match(ContractDocument, ContractDocument)` produces matched/unmatched
interactions with correspondence evidence. Match the relative route/method first to explain moved addresses,
then compare address availability; do not create an unrelated cascade of removed-operation findings solely
because an address changed.

- [ ] Test operation removal/addition, removed server URLs, finite variable-enum expansion, environment
  mapping, placeholder renaming, unresolved relative servers, and concrete-route shadowing.
- [ ] Implement literal and finite-template address-set coverage. Open-ended template equivalence uses
  structural proof when available; otherwise Indeterminate. Never enumerate an unbounded domain.
- [ ] Use exact path matching before structural placeholder correspondence. Reject ambiguous pairings and
  keep `operationId` as explanatory metadata, not routing authority.
- [ ] Test `/pets/{id}` plus a new `/pets/search`: check the old feasible `id=search` request against the new
  selected operation. If feasibility or equivalent behavior is unproved, emit unresolved `ROUTE_SHADOWING`.
- [ ] Preserve name-sensitive matrix/label/query serialization checks for paired renamed placeholders.
- [ ] Run V1/V4. Validate findings refer to old/new source paths even for paired renamed map keys.

### T13 — Input capabilities, parameter values, and body presence

**Dependencies:** T10, T12.

**Files:** Create `OA/rules/InputRules.java`, `SchemaRules.java`;
`JT/OpenApiInputTest.java`, `ST/openapi-compat-inputs.test.ts`; `FX/inputs.json`.

**Interfaces:** `InputRules.compare(EffectiveInteraction, EffectiveInteraction, RuleContext)` checks input
capabilities and invokes `SchemaRules.compareUsage`. SchemaRules translates containment evidence into
usage-specific findings without reversing original/updated labels in diagnostics.

- [ ] Add tests for removed optional query input, newly required parameter, optional parameter addition,
  same-name different-location parameters, case-only header rename, and narrowing enum/length constraints.
- [ ] Add body tests distinguishing missing body, empty bytes, JSON null, and `{}`. Required body is a
  presence obligation independent of whether its schema accepts null or an empty object.
- [ ] Implement input containment and capability rules:

```text
removed documented input -> BREAKING capability finding
new required input not already guaranteed by old requests -> BREAKING
retained input -> prove old permitted values subset new permitted values
new optional input -> safe unless existing wire-field interaction is affected
```

- [ ] Normalize 2.0 body/formData inputs into views without changing source models. Keep body-parameter
  names documentation-only; form field names are wire names. Respect file/media constraints.
- [ ] Ignore Accept/Content-Type/Authorization header Parameter Objects where the family specification says
  to ignore them; compare their actual content/security contracts in the proper rules.
- [ ] Recognize 3.2 querystring parameters as whole-query representations; changes between individual query
  parameters and whole-query schemas need a proven serialization mapping or Indeterminate.
- [ ] Run V1/V4. Assert shared input/output components are not cached under a single direction.

### T14 — Representation and serialization compatibility

**Dependencies:** T10, T12, T13.

**Files:** Create `OA/rules/RepresentationRules.java`;
`JT/OpenApiRepresentationTest.java`, `ST/openapi-compat-representations.test.ts`;
`FX/representations.json`.

**Interfaces:** `RepresentationRules.compare` works on the media/encoding context of schema usages and
returns findings through RuleContext. Content-type matching normalizes only HTTP-defined case-insensitive
parts; do not lowercase parameter values that may be case-sensitive.

- [ ] Test omitted versus explicit default style/explode, CSV versus repeated query fields, content/schema
  serialization switches, binary/base64 distinction, and multipart part/header changes.
- [ ] Use a concrete serialization witness where available:

```text
value: [1, 2]
old query form/explode=false: ids=1,2
new query form/explode=true: ids=1&ids=2
result: SERIALIZATION_CHANGED with BREAKING impact if old encoding is no longer supported
```

- [ ] Do not classify an irrelevant explode change for a scalar as breaking. Unknown serializer behavior
  is Indeterminate, not an inferred equivalence.
- [ ] Compare request media coverage and response representation availability separately from schemas.
  Added response media records the negotiation-stability assumption. A new more-specific entry overriding
  an old wildcard must still satisfy the old effective contract for that representation.
- [ ] Analyze XML name/namespace/attribute/wrapper changes and simple multipart encoding. Changes to opaque
  serialization formats or ambiguous media parameters produce explicit unresolved findings.
- [ ] Preserve 3.2 itemSchema, prefixEncoding/itemEncoding, and nested encoding. Compare simple independent
  stream items directionally; if whole-stream schema constraints interact with itemSchema beyond supported
  proofs, report Indeterminate rather than treating a stream as an ordinary JSON array on the wire.
- [ ] Run V1/V4 with escaped values, repeated fields, and explicit media selection examples.

### T15 — Response selection, output guarantees, and retained representations

**Dependencies:** T10, T13, T14.

**Files:** Create `OA/contract/ResponseSelector.java`, `OA/rules/ResponseRules.java`;
`JT/OpenApiResponseTest.java`, `ST/openapi-compat-responses.test.ts`; `FX/responses.json`.

**Interfaces:** `ResponseSelector.select(EffectiveInteraction, int status)` resolves exact, family-supported
range, then default. `ResponseRules.compare` evaluates effective coverage and provider-output obligations.

- [ ] Test a new 202 with only old 200 (breaking), new 404 covered by old default, explicit overrides of
  4XX, incompatible refinement under default, and redundant explicit-entry removal.
- [ ] Compute effective partitions rather than map-key deltas:

```text
for each relevant HTTP status partition induced by both documents:
    oldDefinition = select(old, partition)
    newDefinition = select(new, partition)
    if new outcome has no old coverage: report RESPONSE_STATUS_ADDED
    otherwise compare new output constraints against old guarantees
```

- [ ] Preserve the approved documented-outcome policy even though OAS allows incomplete response lists.
  Retain an assumption identifying this policy instead of claiming observed runtime behavior.
- [ ] Check retained response media for clients requesting each old representation. Removing application/json
  is not safe merely because fewer output formats are possible. Retain negotiation stability for additions.
- [ ] Compare response-header presence and values; HTTP header maps are not closed JSON objects by default.
  Ignore a declared Content-Type header where the specification delegates it to content mappings.
- [ ] Flag withdrawal of a documented successful capability even if output-set inclusion alone would accept
  fewer outcomes. Treat removal of a redundant error branch through equivalent default coverage as safe;
  do not claim business equivalence from status-set narrowing.
- [ ] Check no-body responses under family rules and HTTP method/status restrictions. Model missing content
  separately from an unconstrained schema for a present representation.
- [ ] Run V1/V4. Include response optional-property removal with open additional properties to catch widened
  allowed values that a field-by-field removal heuristic misses.

### T16 — Security expression implication and credential mechanisms

**Dependencies:** T11, T12.

**Files:** Create `OA/rules/SecurityRules.java`;
`JT/OpenApiSecurityTest.java`, `ST/openapi-compat-security.test.ts`; `FX/security.json`.

**Interfaces:** SecurityRules consumes effective security expressions and resolved scheme definitions from
the contract graph. Scheme identity is semantic/reference-scoped, not merely a local component name.

- [ ] Test OR alternatives, AND schemes, required scopes, redundant alternative removal, anonymous access,
  empty override versus absence, and scheme renaming with updated references.
- [ ] Implement implication for known mechanisms using this sufficient positive-expression rule:

```text
for every feasible old alternative:
    find a new alternative whose obligations are implied by that old alternative
if all found with established mechanism equivalence: compatible
if a concrete old credential obligation set is rejected: incompatible
otherwise: indeterminate
```

- [ ] Compare API-key location/name, HTTP authentication scheme case-insensitively, known scope obligations,
  and credential-flow availability. Preserve ordinary versus reverse-interaction credential roles.
- [ ] Emit `CREDENTIAL_FLOW_REMOVED` for withdrawn documented acquisition/refresh capability. Endpoint
  relocation withdraws an old documented location unless explicitly mapped; do not assume redirects.
- [ ] For unknown trust equivalence, issuer changes, opaque roles, and mTLS conditions in prose, emit
  `SECURITY_EQUIVALENCE_UNCERTAIN`. Do not assume two bearer definitions accept the same tokens.
- [ ] Resolve 3.2 URI-valued security requirement names with the specified component-name precedence.
  Handle device-authorization flow fields and metadata URL context.
- [ ] Run V1/V3. Close M3 with ordinary-operation corpus parity, not just unit coverage of individual rules.

### T17 — Reverse interactions, implicit relationships, and behavioral metadata

**Dependencies:** T12–T16.

**Files:** Create `OA/rules/ReverseInteractionRules.java`, `RelationshipRules.java`;
`JT/OpenApiReverseInteractionTest.java`, `OpenApiRelationshipTest.java`;
`ST/openapi-compat-reverse.test.ts`, `openapi-compat-relationships.test.ts`;
`FX/reverse-interactions.json`, `FX/relationships.json`.

**Interfaces:** Reuses input/output/security/representation units with explicit sender/receiver roles.
RelationshipRules follows discriminator, security, Link, and expression edges without making unrelated
components public roots. Add opt-in interaction IDs and address mapping to immutable policy configuration.

- [ ] Test the full four-row direction table using the same schema pair. Test nested callback role changes,
  new webhook with default Indeterminate, explicitly opt-in webhook with compatible addition, and removed
  documented reverse capability with an Incompatible finding.
- [ ] Compare callback runtime destination expressions. A changed expression is not a harmless map rename;
  proven changed delivery gives a break, uncertain expression equivalence gives Indeterminate.
- [ ] Test explicit and implicit discriminator mappings, component renames that preserve explicit mapping,
  and implicit-name changes that alter dispatch. Do not let discriminator hints change schema validation.
- [ ] Check 3.2 defaultMapping and optional discriminating properties. Complex dispatch equivalence beyond
  known cases is unresolved, not accepted because payload schemas match.
- [ ] Resolve Links and runtime expressions; broken Link targets are relationship findings. An operationId
  rename alone is informational if all effective relationships remain valid. Links do not guarantee that a
  target operation will succeed, so do not invent that guarantee.
- [ ] Treat changed behavioral defaults as Indeterminate unless a supported rule establishes the effect.
  Documentation, tags, examples, and deprecation are informational. Known documentation-only extensions
  can be informational; changed unknown reachable extensions receive `EXTENSION_SEMANTICS_UNKNOWN`.
- [ ] Run V1/V4. Assert every shared-definition finding lists all affected usages with their own roles.

### T18 — Public checker assembly, validation boundaries, and async snapshots

**Dependencies:** T1–T17.

**Files:** Create `OA/OpenApiCompatibilityChecker.java`, `OpenApiCompatibilityCheckerBuilder.java`,
`OpenApiAsyncCompatibilityChecker.java`; create `TS/OpenApiAsyncCompatibilityChecker.ts` if T4 selects
curated replacements;
`JT/OpenApiCheckerTest.java`, `OpenApiAsyncCheckerTest.java`;
`ST/openapi-compat-checker.test.ts`, `openapi-compat-async-checker.test.ts`.
Modify export configuration and `data-models/pom.xml` as required by the tested bridge boundary.

**Interfaces:** Implements all six synchronous and six asynchronous entry points described above.
`OpenApiCompatibilityCheckerBuilder.policy(CompatibilityPolicy)` returns the builder; `build()` returns the
immutable checker. Options are passed explicitly, with `CheckOptions.defaults()` available for convenience.

- [ ] Add end-to-end tests for each entry point, all four families, patch-version differences, cross-family
  rejection, invalid JSON/root documents, and an independently unresolved operation alongside a known break.
- [ ] Implement snapshot -> validation -> index/acquire -> interpret -> match -> rules -> aggregate.
  Validate against known family requirements; incomplete legacy validators must not classify valid modern
  schema keywords as invalid. Unsupported semantics use the coverage mechanism instead.
- [ ] Full checking snapshots/acquires once and evaluates both directions over the same resources. Keep
  caches context-sensitive and per check. Preserve side identity when reversing comparison direction.
- [ ] Write the following public usage test without newer Java syntax:

```java
OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder()
        .policy(CompatibilityPolicy.defaults())
        .build();
CompatibilityResult result = checker.checkBackwardJson(originalJson, updatedJson, options);
assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
```

- [ ] Test caller mutation while an async loader is pending: the check uses the start-of-check snapshot.
  Test completed results remain unchanged after caller model/resource edits. Do not expose mutable session
  references through public results.
- [ ] Verify nested collection isolation using `CollectionUtil` and element snapshots as required. Test
  sequential and concurrent checks through one checker do not share mutable traversal caches or identity
  maps. A legacy resolver instance with persistent document caches is not a per-check session substitute.
- [ ] Test sync/async equivalence for identical supplied resource snapshots and arbitrary loader completion
  order. Ensure no loader is invoked by the synchronous API.
- [ ] Run V1/V3 and inspect packaged TypeScript declarations/imports. Close M4 only after all entry points
  run through the same rule engine with matching diagnostics.

### T19 — Cross-runtime parity, source restrictions, and package verification

**Dependencies:** T18.

**Files:** Create `JT/OpenApiParityReportTest.java`, `OpenApiMetamorphicTest.java`;
`ST/openapi-compat-parity.test.ts`, `openapi-compat-properties.test.ts`,
`openapi-compat-package-smoke.test.ts`; `FX/coverage.json`.
Modify `data-models/pom.xml`, `data-models/src/main/ts/package.json`, and `.github/workflows/verify.yaml`
for ordering, source checks, and post-package smoke tests without a new runtime dependency.

**Interfaces:** Canonical result serialization includes verdict, sorted findings with impact/locations/roles,
coverage, policy ID, and assumptions. It excludes platform stack traces and internal identity hashes.
`coverage.json` maps every matrix row and family to required case IDs and declared analysis level.
Split fixtures under `FX` are registered by a manifest in `FX/cases.json`; both catalog harnesses load that
same manifest. Keep the initial inline `cases` entry and add a `files` array containing the exact split-file
names as each task creates them. Reject duplicate IDs across files and stale unregistered coverage cases.

- [ ] Run the same corpus through Java and TypeScript, including numeric provenance and resource failures.
  Produce the Java canonical report before frontend tests; fail on missing or stale report fingerprints.
- [ ] Add baseline smoke/parity cases for the now-transpiled JSON Schema checker, reference helpers,
  `DiffType.getHelp()`, and `getExamples()`. The enabling commits added no dedicated TypeScript compatibility
  tests; successful transpilation or unrelated suites do not establish these runtime semantics. Keep legacy
  behavior characterization separate from the new containment engine's correctness expectations.
- [ ] Require every enabled case to assert a verdict and expected breaking/unresolved codes. Assert exact
  paths for representative cases, not only sets of enum names. Prohibit automatic verdict reseeding.
- [ ] Add these metamorphic assertions with generated permutations of fixed fixture data:

```text
forward(A, B) matches backward(B, A) after directional-label normalization
full(A, B) preserves the two individual directional verdicts
reordering parameters/security alternatives/object keys preserves effective verdicts
extracting an inline schema into an equivalent reference preserves verdicts
adding an unused component preserves verdicts
sync(snapshot) equals async(the same snapshot)
```

- [ ] Validate supported NO witnesses with independent JSON Schema validators in test tooling where their
  dialect/format configuration matches the tested semantics. Such validators are optional development
  dependencies only and cannot turn sampling into a YES proof. Keep deterministic corpus expectations
  independent of both engines; a validator disagreement requires investigation, not automatic reseeding.
- [ ] Run Java 11 source checks from T5 and full CI-equivalent builds on JDK 17 and 21. Check generated code,
  not only handwritten Java. Avoid stream/collection APIs unsupported by JSweet even if Java 11 allows them.
- [ ] Test ESM and CommonJS package exports and a browser-like environment without Node fs/http globals.
  Native networking remains the caller loader's responsibility. Verify documentation/result helpers do not
  depend on Java classpath resources in the browser bundle.
- [ ] Reuse `ResourceUtil.readResourceAsString` for bundled runtime help/examples with literal absolute
  resource paths at call sites. Verify `JacksonAdapter` inlines their contents during transpilation; no
  handwritten filesystem bridge is needed. Test representative escaping and missing-resource build failure.
  Keep dynamic loader URIs out of this compile-time-only API, and keep adapter `.class` output untracked.
- [ ] Add stress tests for many shared usages, recursive graphs, and bounded resource acquisition. Budget
  exhaustion must yield stable Indeterminate findings rather than stack overflow or nondeterministic output.
- [ ] Run V3, V5, and V6. Review the published coverage manifest against every approved policy.

### T20 — Public documentation and release-readiness review

**Dependencies:** T19.

**Files:** Create `docs/user-guide/openapi-compatibility.md`;
modify `docs/user-guide/schema-compatibility.md`, `docs/user-guide/index.md`, `mkdocs.yml`,
`data-models/src/main/ts/README.md`, and `README.md` where feature availability is described.

**Interfaces:** Documentation examples use the exact public signatures from T18, native TS promises, and
the same policy/result vocabulary in both languages. Document that the existing JSON Schema packages now
participate in TypeScript builds, verifying actual package exports with T19 smoke tests. Distinguish their
legacy boolean/approximate behavior from the new tri-state containment guarantees; portability does not
establish semantic equivalence between the two APIs.

- [ ] Document ordinary versus reverse direction with the four-row table and a shared-schema example.
- [ ] Include model and JSON inputs, sync supplied resources, async loader acquisition, base URIs,
  separate old/new resource snapshots, and callback/webhook opt-in policy examples.
- [ ] Explain tri-state aggregation, validation errors, precision provenance limitations, read/write and
  negotiation assumptions, formats, server mapping, and scoped coverage limits.
- [ ] Publish the initial feature matrix from the machine-checked coverage manifest. Clearly distinguish
  family parsing support from complete semantic analysis of every construct.
- [ ] Correct stale JSON Schema documentation only where verified by actual API/tests. Do not imply the
  legacy boolean checker now provides the same proof guarantees unless separately changed and verified.
- [ ] Compile/run documentation examples through existing Java/Jest test harnesses, including one loader
  failure producing Indeterminate and one definite break with unresolved findings still retained.
- [ ] Run V3/V5/V6 after any executable-example changes. Review user-visible generated-model API changes,
  package exports, coverage, and remaining explicit limitations. Close M5 only with recorded evidence.

## Execution checkpoints and review questions

These are bounded engineering decisions at named gates, not unspecified functionality:

1. **After T2:** Review generated public schema-union signatures and migration impact. Model fidelity is
   mandatory; silent loss is not an acceptable compatibility-preserving alternative.
2. **After T4:** Confirm the native CompletionStage/Promise bridges can share the session/discovery surface
   without leaking Java-native types into transpiled classes. Record whether member erasure/substitution or
   isolated native facade replacement was selected and why. If needed, adjust only the orchestration boundary.
3. **After T5:** Confirm the documented numeric-provenance policy is acceptable. Exact text inputs and
   precision-uncertain model inputs can legitimately differ in coverage; they must agree when supplied
   equivalent exact provenance. Do not promise parity over information that one input path already lost.
   Ensure current approximate `NumberUtil` comparisons and `DiffUtil` tolerance cannot silently discard
   constraints during normalization or create definitive new-engine proofs.
4. **After T10:** Audit YES/NO evidence against whole-schema interactions, not individual keyword deltas.
   Existing catalog verdicts are evidence of historical behavior, not the semantic authority.
5. **Before M5:** Review the initial bounded/recognized feature rows. Adding a family label cannot substitute
   for coverage of its actual constructs, and unsupported relevant behavior must remain visible.

## Self-review and acceptance checklist

- [ ] All approved policies in the design table have implementation tasks and independent cases.
- [ ] All four same-family entry paths exist in both runtimes; cross-family requests fail explicitly.
- [ ] Schema comparisons preserve both HTTP role and provider role, including nested reverse interactions.
- [ ] Shared schemas are compared per usage; unused components do not affect the default verdict.
- [ ] Serialization, addresses, security, and documented capabilities are not reduced to schema inclusion.
- [ ] Missing/invalid external resources cannot become unconstrained schemas or disappear from results.
- [ ] Read/write projection does not erase constraints or silently reinterpret modern dialect annotations.
- [ ] No regex/oneOf/recursive heuristic is promoted to a definitive proof without established premises.
- [ ] The matrix's unsupported combinations produce explicit Indeterminate coverage.
- [ ] JSON booleans, null, exact numeric tokens, and unknown schema keywords survive the supported input
  paths.
- [ ] Sync/async model/string APIs preserve inputs and use a shared semantic engine.
- [ ] Java 11-compatible feature sources and generated helpers pass source checks and actual transpilation.
- [ ] Actual builds and declaration generation pass on explicitly selected JDK 17 and 21; newer-JDK-only
  success is not substituted for this requirement.
- [ ] Existing JSON Schema/diff/conversion transpilation remains enabled; shared package exclusions are not
  reintroduced to accommodate a native async bridge.
- [ ] Defensive mutable copies cannot expose internal result state, and identity/cache state is per check.
- [ ] Approximate legacy numeric normalization is not used as exact containment evidence.
- [ ] Java/TS corpus verdicts, findings, assumptions, and coverage match after canonicalization.
- [ ] Public documentation reflects actual package exports and tested limitations.
- [ ] No implementation, commit, or delegation begins merely because this plan exists.

## Handoff

This is a documentation-only planning deliverable. The next step is user review of the proposed feature
boundary and task order. After implementation is authorized, execute inline with `executing-plans` and stop
at the named review gates. Delegated execution is an alternative only with explicit user authorization.
