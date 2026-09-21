---
paths:
  - "data-models/src/main/java/**/*.java"
  - "generator/src/main/resources/base/**/*.java"
  - "data-models/jsweet_extension/**/*.java"
  - "data-models/pom.xml"
---
# Writing Java that transpiles to TypeScript (JSweet)

Everything under `data-models/src/main/java` — and every template under
`generator/src/main/resources/base` — is compiled **twice**: by `javac` for the JAR, and by
JSweet into TypeScript for the npm package. The Java build passing tells you nothing about
whether the TypeScript build will.

The transpilation runs in the `transpilation` profile, so plain `mvn test` will **not** catch
these. Verify with:

```
mvn clean install -Ptranspilation
```

Failures surface in two distinct phases, and they mean different things:

- **"transpilation failed with N error(s)"** — JSweet itself rejected the Java. Look for
  `[ERROR] * <file>(line,col)` lines.
- **`error TS####` during `npm run package`** — JSweet emitted TypeScript, but `tsc` rejected
  it while building the `.d.ts`. The JS bundle may build fine while this fails.

## Constructs that do not transpile

Each of these compiles under `javac` and then fails for TS. The right-hand column is what to
write instead.

| Do not use | Use instead |
|---|---|
| `instanceof X x` (pattern variable) | `instanceof X` + an explicit cast on the next line |
| `record` | a plain class (keep record-style accessor names for source compatibility) |
| `.stream()`, `Collectors`, `.toList()` | a `for` loop |
| `List.copyOf` / `Set.copyOf` / `Map.copyOf` | `CollectionUtil.copyOfList/copyOfSet/copyOfMap` |
| `java.util.Collections` (any member) | build the collection directly, or `CollectionUtil` |
| `java.util.Objects.equals/hash/toString` | write the null check or hash out by hand |
| `String.formatted(...)` / `String.format` | string concatenation |
| `System.identityHashCode` | a `Map<Object, Integer>` of lazily assigned ids |
| `java.math.BigDecimal` | `NumberUtil.compare`, or `double` arithmetic |
| `java.util.Arrays.*` | a `for` loop |
| `Map.Entry` / `jsonNode.fields()` | `jsonNode.fieldNames()`, then `get(name)` |
| wildcard static import (`import static X.*`) | explicit imports, one per member |
| `new Foo<>()` (diamond) in a **local variable** | explicit type arguments: `new Foo<Bar>()` |
| `new LinkedHashSet<T>(source)` (copy constructor) | construct empty, then add in a loop |
| overloading by parameter type | distinct method names (`copyOfList` / `copyOfSet`) |
| `this` passed to a parameter of the enum's own type | pass `name()` and look up by name |
| the identifiers `in`, `ref` | any other name — these are reserved by the transpiler |
| `var` (local type inference) | the explicit type — see below, this one is a trap |

### `var` fails only on the JDKs CI uses

`var` crashes the transpiler: JSweet asks javac for the source position of the inferred type
node, which does not have one, and dies with
`ArrayIndexOutOfBoundsException: Index -1` inside `Position.getColumnNumber`. The report
surfaces as `internal transpiler error` against the enclosing method, block and `if` as well, so
a handful of `var`s produces a hundred errors pointing at code that is fine.

**It reproduces only on JDK 17 and 21 — the versions CI builds with. On JDK 25 it transpiles
cleanly.** A local build on a newer JDK therefore proves nothing:

```
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk mvn clean install -Ptranspilation
```

Run that before pushing anything that touches transpiled sources. There is no `var` anywhere
else under `data-models/src/main/java` — that is this constraint, not a style preference.

Two constructs are worth extra care because they change behaviour rather than just failing:

- **`instanceof` with a pattern variable inside a compound condition.** Rewriting
  `if (x instanceof T t && t.foo())` as an early-return guard is *not* equivalent when code
  follows the block — an `x` that is a `T` but fails `foo()` must still fall through. Compute
  the condition into a local `boolean` first, or unroll a `while` condition into the body.
- **Numeric precision.** `BigDecimal` is exact; `double` is not. `NumberUtil.compare` and the
  `multipleOf` check in `DiffUtil` are deliberate approximations — the TypeScript target cannot
  do better, since JSON numbers are doubles there.

## Bundled resources

There is no classpath in the TypeScript target, so `getResourceAsStream` cannot work. Read
bundled resources through `ResourceUtil.readResourceAsString`, whose calls `JacksonAdapter`
replaces at transpile time with the file's contents inlined as a string literal.

Two constraints follow:

- **The argument must be a string literal at the call site.** A constant reference cannot be
  resolved at transpile time — the build fails with "internal transpiler error". Repeat the
  path literally even where a `RESOURCE` constant already holds it.
- **The path is absolute**, rooted at `src/main/resources`.

`ResourceUtil.readResourceAsString` is annotated `@jsweet.lang.Erased` so its JVM-only body is
not emitted. Note that erasing the *member* is the tool for this; excluding the *file* is not,
because anything importing it then fails to resolve.

## Excluding a file is almost never the answer

`data-models/pom.xml` has a JSweet `<excludes>` list. It is tempting but usually wrong: an
excluded class cannot be imported by any transpiled class, so the exclusion spreads outward to
every caller. The JSON Schema packages were excluded for exactly this reason and that exclusion
then blocked putting JSON Schema capabilities on `Library`. Prefer `@jsweet.lang.Erased` on the
specific member, or teach `JacksonAdapter` a substitution.

## Extending the adapter

`data-models/jsweet_extension/io/apitomy/JacksonAdapter.java`, wired via
`data-models/jsweetconfig.json`, maps Java APIs onto TypeScript by printing raw TS at the call
site. Add a `case` to `substituteMethodInvocation` for the declaring class. Note its working
directory is the **reactor root**, not the module, so resolve module paths accordingly.

> **Do not commit the compiled `JacksonAdapter.class`.** JSweet compiles the extension source
> itself on each build (`ExtensionManager: compiling 1 extension file(s)`) and writes the class
> next to it, so the `.class` is build output. Committing one is worse than redundant: it is
> easy to build it against the wrong `jsweet-transpiler` version (the one that happens to be in
> `.m2` is not necessarily the one the build uses), and a version-mismatched adapter is loaded
> in CI while your own machine quietly recompiles it from source.
>
> The ignore rule for it was anchored at the repo root and never matched
> `data-models/jsweet_extension/`, which is how the class came to be tracked and swept into
> unrelated commits.
>
> If a build ever fails with `ClassNotFoundException: io.apitomy.JacksonAdapter`, the cause is a
> stale `data-models/.jsweet/extension.json` — it records that the extension is already compiled,
> so JSweet skips rebuilding a class that is no longer there. Delete `data-models/.jsweet/` and
> rebuild; do not reintroduce a committed `.class` to work around it.

## Changing a generator template

Templates in `generator/src/main/resources/base/` are copied into generated output, so they are
subject to every rule above. After changing one, the committed snapshot must be regenerated:

```
rm -rf generator/src/test/resources/io/apitomy/umg/synthetic/expected/
mvn -pl generator test -Dtest=SyntheticSnapshotTest
```

Then review the resulting diff — it should contain only your intended change.
