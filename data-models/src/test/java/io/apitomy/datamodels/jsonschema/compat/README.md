# Compatibility oracle testing

`CompatibilityOracleTest` checks the JSON Schema compatibility checker's verdicts against an
independent source of truth, a JSON Schema validator. This page explains the idea, walks through
how the test builds its inputs, and describes what to do when it fails.

## Why an oracle

The catalogue test (`JsonSchemaCompatibilityTest`) compares the checker's output with expectations
someone wrote down. That has two weaknesses:

- **It only covers the cases someone thought of.** A defect is usually one kind of edit handled
  wrongly in one kind of place, such as "narrowing a schema inside `dependencies`". A hand-written
  catalogue does not enumerate every edit at every location, so defects like that go unnoticed.
- **An expectation can be wrong.** The catalogue's expectations are regenerated from the checker
  (`ExampleCatalogSeeder`), so a mistake in the checker can become a mistake in the expectation.

An *oracle* is something that can tell you the right answer for a test input without sharing the
code under test. Here, the question the checker answers has a precise meaning:

> The change from `original` to `updated` is **backward compatible** if every document valid
> against `original` is also valid against `updated`.

That definition talks about documents and validity, and a JSON Schema validator decides validity.
So a single document is enough to settle a "compatible" verdict:

- if it is valid against `original` and invalid against `updated`, the change is **not**
  compatible, whatever the checker says. That document is a *counterexample*.

The validator used is [networknt json-schema-validator](https://github.com/networknt/json-schema-validator),
a test-scope dependency. It shares no code with the checker, and it supports all five drafts the
checker does.

### What the oracle can and cannot prove

The argument only works one way:

| checker says | counterexample found | meaning |
|---|---|---|
| compatible | yes | **proven wrong**: the test fails |
| compatible | no | consistent (not proven right: a counterexample may exist that was not generated) |
| incompatible | yes | **proven right** |
| incompatible | no | possibly over-strict, or the generator missed the counterexample: reported, not failed |

A false "compatible" is the dangerous mistake for a schema registry: it lets through a change that
breaks consumers. It is also the one the oracle can prove, which is why it is what the test fails
on. Over-strict verdicts go to a report instead. See [the report](#the-report).

## The pipeline

```
catalogue cases ──────────────┐
                              ├──► schema pairs ──► documents ──► validator ──┐
base schemas ──► mutations ───┘         │                                     ├──► finding?
                                        └──────────► checker ─────────────────┘
```

Each step has its own class:

| step | class |
|---|---|
| derive schema pairs from base schemas | `OracleSchemaMutator` |
| generate documents for a pair | `OracleInstanceGenerator` |
| validate, run the checker, classify, report | `CompatibilityOracleTest` |

### 1. Schema pairs

Pairs come from two places.

**The catalogue.** Every enabled case in `compatibility-test-data.json`, except cases with
external references or an expected error. Both directions are checked: `backward` is
`(original, updated)` and `forward` is `(updated, original)`.

**Mutations of base schemas.** `oracle-bases.json` holds a set of small schemas, each listed with
the drafts to render it in. For each one, `OracleSchemaMutator`:

1. **Walks every subschema location**: the root, then everything under `properties`,
   `patternProperties`, `dependencies`, `dependentSchemas`, `definitions`, `$defs`, `items`
   (single or tuple), `prefixItems`, `additionalProperties`, `additionalItems`, `unevaluated*`,
   `contains`, `propertyNames`, `allOf`/`anyOf`/`oneOf`, `not`, and `if`/`then`/`else`.
2. **Applies every edit that fits that location**, one at a time. The edits are:
   - replace the subschema with `true`, `false` or `{}`
   - wrap it in `anyOf: [<it>, {"type": "null"}]`
   - move each bound (`maxLength`, `minimum`, `maxItems`, …) up and down by one, or remove it
   - add a bound that is not there
   - change `type`, add `null` to it, or remove it
   - add or remove a property, `required` entry or dependency member
   - close an object (`additionalProperties: false`) or a tuple
   - grow or shrink a tuple
   - add or remove an `enum` value, or change a `const`
3. **Checks both directions.** `apply` checks `(base, mutated)` and `revert` checks
   `(mutated, base)`. Almost every edit narrows in one direction and widens in the other, so each
   mutation tests both.

Each pair gets an id that says exactly what it is, for example:

```
schema dependency @draft-07 /dependencies/a/properties/b maxLength - 1 apply
└─ base ────────┘ └draft──┘ └─ location (JSON Pointer) ───┘ └─ edit ──┘ └direction
```

A single edit keeps a failure readable: the difference between the two schemas is one keyword.

### 2. Documents

Random JSON would almost never find a counterexample. For a `maxLength` going from 10 to 9, it
takes a string of exactly 10 characters, in the right property, next to whatever other properties
make the rest of the document valid. `OracleInstanceGenerator` builds documents from the schemas
themselves instead:

1. **Collect the schemas that apply at one location.** That means the schema itself plus what it
   pulls in: local `$ref` targets, `allOf`/`anyOf`/`oneOf` branches, `not`, `if`/`then`/`else`, and
   dependency schemas. This is done for both sides of the pair together.
2. **Collect the constants in them:** length and number bounds, `enum` and `const` values,
   property names (from `properties`, `required` and dependencies), and tuple lengths.
   A constant that appears on **only one side** is the likeliest to tell the schemas apart, so
   candidates built from it come first.
3. **Build candidates around each constant**, per type:
   - *strings*: lengths 0 and 1, and each length bound −1, exactly, and +1
   - *numbers*: each bound, and the bound ±1 (and ±0.5 unless only integers are allowed)
   - *arrays*: lengths around `minItems`/`maxItems` and the tuple length, with element candidates
     generated recursively for each position
   - *objects*: the empty object; one with only the required properties; one with every known
     property; then variants that replace one property's value with each of its candidates, or
     remove one property. Two names no schema mentions (`u`, `unknown`) are included to exercise
     `additionalProperties`.
   - `enum`/`const` values as they are, and one value of every JSON type, so a `type` change is
     always exercised.
4. **Recurse with limits:** at most 4 levels deep, 10 candidates per nested location (plus one of
   each type), and 400 documents per pair.

The generator is deterministic: the same pair always produces the same documents in the same
order, so every failure reproduces exactly.

It never decides validity itself. Most generated documents are invalid against both schemas, and
that is fine: they are discarded in the next step.

### 3. Verdicts

For each direction of each pair, the test:

1. validates every document against both schemas;
2. looks for the first document valid against `original` and invalid against `updated`;
3. runs the checker with `checkBackward(original, updated)`, configured as the catalogue test does
   it, with a dereferencer;
4. classifies the result as in [the table above](#what-the-oracle-can-and-cannot-prove).

A result that reports unsupported features counts as **no verdict**, because callers are told to
gate on `isCompatible() && !hasUnsupportedFeatures()`. So does an exception from the checker.

### 4. Neutral edits

Some edits must not change a verdict at all, and checking that needs no validator. The test
repeats every check with an unused definition added to both schemas (`definitions` or `$defs`,
depending on the draft). If the verdict changes, that is a finding.

### Finding categories

| category | meaning |
|---|---|
| `FALSE_COMPATIBLE` | the checker said compatible, and a document proves otherwise |
| `NEUTRAL_EDIT_CHANGED_VERDICT` | adding an unused definition changed the verdict |
| `NO_VERDICT` | the checker threw, or reported unsupported features |

## Worked example

Base schema `schema dependency`, rendered as draft 7:

```json
{
  "type": "object",
  "properties": {"a": {"type": "integer"}},
  "dependencies": {"a": {"properties": {"b": {"type": "string", "maxLength": 10}}}}
}
```

When `a` is present, `b` must be a string of at most 10 characters.

**Mutation.** At location `/dependencies/a/properties/b`, the edit `maxLength - 1` gives an updated
schema with `"maxLength": 9`. The `apply` direction checks whether that change is backward
compatible. It is not: a 10-character `b` next to an `a` used to be valid.

**Documents.** The generator produces 78 documents for this pair. The first few:

```
{}
{"a":0,"b":"s","u":"s","unknown":"s"}
{"a":0}
{"a":-1}
{"a":1}
{"a":"s"}
...
```

`b`'s bounds 10 and 9 differ between the sides, so lengths 8 to 11 are generated for it. One of
the documents is:

```json
{"a": 0, "b": "ssssssssss", "u": "s", "unknown": "s"}
```

**Validation.** Against the original it is valid: `b` has 10 characters, the limit is 10. Against
the updated schema it is invalid: the limit is now 9. So it is a counterexample.

**Checker.** The checker reports one difference, `OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED`, which is
flagged backward-compatible, so the verdict is **compatible**. That is a `FALSE_COMPATIBLE`
finding, and the test prints:

```
FALSE_COMPATIBLE  schema dependency @draft-07 /dependencies/a/properties/b maxLength - 1 apply
  original: {"$schema":"http://json-schema.org/draft-07/schema#","type":"object",...,"maxLength":10}}}}}
  updated:  {"$schema":"http://json-schema.org/draft-07/schema#","type":"object",...,"maxLength":9}}}}}
  document: {"a":0,"b":"ssssssssss","u":"s","unknown":"s"}
  differences: [OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED]
```

This is issue [#1227](https://github.com/Apitomy/apitomy-data-models/issues/1227). Every edit inside a dependency schema hits the same cause, so one entry in
the known-failures file covers all of them:

```json
{"issue": "#1227", "category": "FALSE_COMPATIBLE", "pattern": "schema dependency @* /dependencies/*"}
```

## Known failures

`oracle-known-failures.json` lists findings that are tracked by an open issue. Each entry has a
category and a pattern over finding ids, in which `*` matches anything. The first matching entry
wins, so more specific entries come first.

The test fails when:

- a finding matches no entry, which means a **new problem**; or
- an entry matches no finding, which means it is **stale**: the issue was fixed, or the pattern
  no longer fits.

The second rule keeps the file honest. It cannot silently accumulate entries that hide nothing,
and a fix is not done until its entries are deleted.

### When the test fails on a new finding

1. **Reproduce by hand.** The failure message carries both schemas and the document. Paste them
   into any validator and confirm that the document is valid against the original and invalid
   against the updated schema.
2. **If that does not hold, the harness is wrong.** Typical causes are a mutation producing a
   schema that means something different in that draft, or a validator quirk (see
   [Limits](#limits)). Fix the harness.
3. **If it holds, the checker is wrong.** Fix it, or open an issue and add an entry for it. Keep the
   pattern as narrow as the cause: one pattern per cause, not per failing id.

### When you fix an issue

Delete its entries and run the test. Any finding that is still there either needs more of the
fix, or has a different cause and needs its own issue.

## The report

Every run writes `data-models/target/compatibility-oracle-report.md`. It contains:

- counts: pairs, documents, confirmed verdicts, and so on;
- every finding, unexpected or known, with its schemas and document;
- **possibly over-strict** verdicts: "incompatible" with no counterexample found, grouped by the
  difference types the checker reported.

The over-strict list is input for review, not a list of bugs. Some of its entries are
deliberately conservative (see [#1042](https://github.com/Apitomy/apitomy-data-models/issues/1042) CC10), and some are gaps in the generator.

## Running it

```shell
mvn -pl data-models test -Dtest=CompatibilityOracleTest
```

It takes about a second, and it also runs as part of the normal build.

## Extending it

- **More shapes:** add a base schema to `oracle-bases.json`. Keep it small, because coverage comes
  from the edits and a small base keeps failures readable.
- **More edits:** add them in `OracleSchemaMutator.edits`.
- **More keywords in documents:** add them in `OracleInstanceGenerator`, usually by collecting a new
  kind of constant.

A new base or edit will often find something. That is the point, so expect to add issues and entries
along with it.

## Limits

- **Some keywords get weak document coverage:** `pattern` (only the literal prefix of a simple
  regex is used), `format`, non-integer `multipleOf`, `uniqueItems`, `contains` counts, and
  `$dynamicRef`/`$recursiveRef`.
- **`format` depends on the draft in the validator.** With default settings networknt asserts
  `format` for draft 7 and treats it as an annotation for 2020-12, as those drafts specify.
- **Only self-contained schemas.** Catalogue cases with external references are skipped.
- **Verdicts only.** Whether the reported *differences* are the right ones, and whether they are
  at the right path, is not checked. That is [#1224](https://github.com/Apitomy/apitomy-data-models/issues/1224)'s territory.
- **It can find bugs; it cannot prove there are none.** A clean run means no counterexample was
  generated, not that none exists.
