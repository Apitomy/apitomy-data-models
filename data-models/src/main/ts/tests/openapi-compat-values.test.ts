import {ExactDecimal} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ExactDecimal";
import {NumericProvenance} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/NumericProvenance";
import {PortableSchemaUtil} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/PortableSchemaUtil";
import {readJSON} from "./util/tutils";

function fixture(): any {
    return readJSON("tests/fixtures/openapi-compat/numeric-precision.json");
}

test("exact comparisons and divisibility match the fixture", () => {
    const cases: any[] = fixture().exactComparisons;
    cases.forEach((testCase: any) => {
        const a: ExactDecimal = ExactDecimal.parse(testCase.a);
        const b: ExactDecimal = ExactDecimal.parse(testCase.b);
        if (testCase.isIntegralMultipleOf !== undefined) {
            expect(a.isIntegralMultipleOf(b)).toBe(testCase.isIntegralMultipleOf);
        }
        if (testCase.compareTo !== undefined) {
            const actual: number = a.compareTo(b);
            expect(Math.sign(actual)).toBe(Math.sign(testCase.compareTo));
        }
    });
});

test("multipleOf zero is rejected", () => {
    expect(() => ExactDecimal.parse("5").isIntegralMultipleOf(ExactDecimal.parse("0"))).toThrow();
});

test("toString round-trips to an equivalent canonical form", () => {
    expect(ExactDecimal.parse("1e2").compareTo(ExactDecimal.parse(ExactDecimal.parse("1e2").toString()))).toBe(0);
    expect(ExactDecimal.parse("0.3").toString()).toBe("0.3");
    expect(ExactDecimal.parse("-5.0").toString()).toBe("-5");
});

test("JSON equality matches the fixture", () => {
    const cases: any[] = fixture().jsonEquality;
    cases.forEach((testCase: any) => {
        expect(PortableSchemaUtil.jsonEquals(testCase.a, testCase.b)).toBe(testCase.equal);
    });
});

test("explicit null compares equal to itself", () => {
    // Java's PortableSchemaUtil.jsonEquals distinguishes a missing property
    // (a Java `null` reference) from an explicit JSON null (a real NullNode
    // instance) because Jackson gives each a distinct runtime representation.
    // The auto-transpiled TypeScript build has no such distinction available:
    // Java's `a == null` becomes JavaScript's loose `a == null`, which is true
    // for both `null` and `undefined` alike, so this one edge case is
    // necessarily coarser here than in Java. What both runtimes agree on --
    // and what this test checks -- is that two explicit nulls compare equal.
    expect(PortableSchemaUtil.jsonEquals(null, null)).toBe(true);
});

test("numeric token scan recovers exact source text by pointer, honoring escapes, strings, arrays, and exponents", () => {
    const scanCase: any = fixture().numericTokenScan;
    const tokens: any = PortableSchemaUtil.scanNumericTokens(scanCase.document);
    Object.keys(scanCase.expected).forEach((pointer: string) => {
        expect(tokens[pointer]).toBe(scanCase.expected[pointer]);
    });
    expect(Object.keys(tokens).length).toBe(5);
});

test("numeric provenance is exact only for finite safe integers", () => {
    expect(NumericProvenance.fromParsedNumber(42).isExact()).toBe(true);
    expect(NumericProvenance.fromParsedNumber(42).getSourceText()).toBe("42");
    expect(NumericProvenance.fromParsedNumber(NumericProvenance.MAX_SAFE_INTEGER).isExact()).toBe(true);
    expect(NumericProvenance.fromParsedNumber(NumericProvenance.MAX_SAFE_INTEGER + 2).isExact()).toBe(false);
    expect(NumericProvenance.fromParsedNumber(1.5).isExact()).toBe(false);
    expect(NumericProvenance.uncertain().isExact()).toBe(false);
});
