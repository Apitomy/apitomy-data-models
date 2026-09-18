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

Two of these are worth extra care because they change behaviour rather than just failing:

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

> **The compiled `JacksonAdapter.class` next to the source is load-bearing, and nothing in the
> Maven build produces it.** JSweet loads that prebuilt class; delete it and the build fails with
> `ClassNotFoundException: io.apitomy.JacksonAdapter`. So after editing the `.java` you **must**
> recompile and commit the `.class`, or CI transpiles with the old adapter — a green build that
> silently produces the wrong output. An IDE compiling on save hides this locally, which is how
> the `.class` has previously been swept into unrelated commits.
>
> ```
> CP=$(find ~/.m2/repository/io/apitomy/jsweet-transpiler -name '*.jar' | grep -v sources | head -1)
> cd data-models/jsweet_extension && javac -cp "$CP" -d . io/apitomy/JacksonAdapter.java
> ```
>
> Having the build compile this instead is tracked in #1210.

## Changing a generator template

Templates in `generator/src/main/resources/base/` are copied into generated output, so they are
subject to every rule above. After changing one, the committed snapshot must be regenerated:

```
rm -rf generator/src/test/resources/io/apitomy/umg/synthetic/expected/
mvn -pl generator test -Dtest=SyntheticSnapshotTest
```

Then review the resulting diff — it should contain only your intended change.
