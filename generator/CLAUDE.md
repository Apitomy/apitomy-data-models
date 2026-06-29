# Generator (UMG) Style Guide

## Architecture

The Unified Model Generator (UMG) reads YAML spec files and generates Java model classes,
which are then transpiled to TypeScript via JSweet.

### Pipeline structure

- **Stages** (`pipe/java/`) — thin orchestrators that iterate spec versions, entities,
  and properties. Delegate code generation to method classes and code blocks.
- **Method classes** (`pipe/java/method/`) — implement `Method` interface. Each generated
  method (getter, setter, reader, writer, cloner, etc.) is its own class with `getName()`
  and `writeTo(JavaSource<?>)`.
- **Code blocks** (`pipe/java/method/reader/`, `writer/`, `cloner/`) — extend `CodeBlock`.
  Handle property-level code generation for a specific property type (entity, primitive,
  list, map, union, star, regex).
- **`CodeGenContext`** — concrete class owning entity resolution, FQN construction, naming,
  and field logic. Constructed from indexes/config, passed to method classes and code blocks.
- **`PropertyCodeGen`** — wrapper bundling `PropertyModelWithOrigin` + `EntityModel` +
  `CodeGenContext`. Code blocks take `(PropertyCodeGen, JavaClassSource)` instead of
  3-4 separate params.

### Key base classes

- `AbstractStage` — type predicates (`isEntity`, `isPrimitive`, etc.), logging
- `AbstractJavaStage` — FQN construction, entity lookup, naming conventions
- `AbstractIOStage` — shared orchestration for reader/writer/cloner (template method pattern)
- `AbstractCreateMethodsStage` — shared property method dispatch for interface/impl stages

## BodyBuilder conventions

`BodyBuilder` generates method bodies with `${var}` template substitution.

- Use `appendBlock("""...""")` for 4+ line templates. Individual `append()` for short snippets.
- Use `Map.of()` with `addContext(Map)` for 3+ context variables.
  Individual `addContext(name, value)` for 1-2 variables.
- Use `ifElse`/`ifTrue` for conditional code generation.
- Use `forEach` with `LoopContext` + `isFirst` for if/else-if chains.
- Text block content is flush-left (not indented to match surrounding Java source).
- Use `${var}` inline in templates. Don't create intermediate variables like `quotedName`
  just to add quotes — put them directly in the template: `"${name}"`.

## Method classes

- Every generated method gets a class implementing `Method`.
- `getName()` returns the method name, `writeTo(JavaSource<?>)` generates signature + body + imports.
- Use naming-only constructors for name lookups, full constructors for generation.
- No static `methodName()` helpers — use `new MethodClass(args).getName()`.

## Type system

- `resolvedType` is always non-null. No defensive null guards needed.
- Use inherited `AbstractStage` predicates (`isEntity(property)`, `isUnionList(property)`)
  or `Type` interface methods (`type.isEntityType()`, `type.isUnionListType()`) directly.
- Use `PropertyBlockKind.fromResolvedType()` for dispatch in IO stages.

## JSweet / TypeScript compatibility

Generated base classes (`generator/src/main/resources/base/`) are transpiled by JSweet.
These restrictions apply to base classes AND generated code:

- **No `var`** — use explicit types.
- **No `instanceof` pattern matching** — use separate cast after check.
- **No text blocks** — use string concatenation.
- **No `json.remove()`** — use `JsonUtil.removeProperty(json, name)`.
- **No `(ObjectNode) value` casts** — use `JsonUtil.toObject(value)`.
- **Use external iteration** (`for` loops) instead of internal iteration (`.forEach()`,
  `.stream()`) in generated code. JSweet transpilation handles `for` loops reliably.
- JSweet mangles overloaded method names: `toJsonNode(Object)` becomes
  `toJsonNode$java_lang_Object` in TypeScript.

### JsonUtil synchronization

**Java and TypeScript JsonUtil must stay in sync.** When modifying
`generator/src/main/resources/base/.../JsonUtil.java`, always update
`data-models/src/main/ts/src/.../JsonUtil.ts` to match, including
JSweet mangled name aliases.

Patterns:
- Reader: `getProperty` + type check + `removeProperty`
- Writer: `setProperty` + `toJsonNode`/`toArrayNode`/`toObjectNode`
- Defensive validation: `allMatch`/`allValuesMatch`

## Generated code style

- Fields are prefixed with underscore: `_name`, `_items`.
- No unnecessary casts (e.g., don't cast when the return type already matches).
- Use `traverseUnion` (not `traverseNode`) for union properties in traversers.
- Union variant ordering via `UnionVariantComparator` (entities first, then collections,
  then primitives).

## Java source style

- Use imports, not fully-qualified class names in casts.
  `(EntityType) type` not `(io.apitomy.umg.models.concept.type.EntityType) type`.
- Utility classes end in `*Util` (e.g., `PrimitiveTypeUtil`, `TypeNameUtil`),
  unless there's a standard naming expectation (e.g., `Logger` not `LoggerUtil`).
- No empty `@param` tags in javadoc — remove them or add a description.

## Testing

All 1129+ data-models tests must pass after any generator change:
```
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk mvn test -pl data-models -am
```

Synthetic snapshot tests compare generated output byte-for-byte. If generated output
changes intentionally, delete `expected/` and regenerate.
