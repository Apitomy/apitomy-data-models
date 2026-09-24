import {readJSON} from "./util/tutils";
import {ContainmentContext} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentContext";
import {ContainmentResult} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentResult";
import {ContainmentVerdict} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentVerdict";
import {CoverageRegistry} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/CoverageRegistry";
import {ExactDecimal} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ExactDecimal";
import {SchemaContainment} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaContainment";
import {SchemaDialect} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaDialect";
import {SchemaDialectResolver} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaDialectResolver";
import {SchemaNormalizer} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaNormalizer";
import {SchemaView} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaView";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {ProviderRole} from "../src/io/apitomy/datamodels/openapi/compat/ProviderRole";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";

function fixture(): any {
    return readJSON("tests/fixtures/openapi-compat/schema-coverage.json");
}

function context(): ContainmentContext {
    return ContainmentContext.of(HttpRole.REQUEST, ProviderRole.INPUT, CheckDirection.BACKWARD, CompatibilityPolicy.defaults());
}

function view(node: any, dialect: SchemaDialect): SchemaView {
    return new SchemaView(node, dialect, "urn:test:resource", "");
}

test("true and false roots follow boolean schema identities", () => {
    const trueSchema: SchemaView = view(true, SchemaDialect.DRAFT2020_12);
    const falseSchema: SchemaView = view(false, SchemaDialect.DRAFT2020_12);

    expect(SchemaContainment.compare(falseSchema, trueSchema, context()).getVerdict()).toBe(ContainmentVerdict.YES);
    expect(SchemaContainment.compare(falseSchema, falseSchema, context()).getVerdict()).toBe(ContainmentVerdict.YES);
    expect(SchemaContainment.compare(trueSchema, trueSchema, context()).getVerdict()).toBe(ContainmentVerdict.YES);

    const trueVsFalse: ContainmentResult = SchemaContainment.compare(trueSchema, falseSchema, context());
    expect(trueVsFalse.getVerdict()).toBe(ContainmentVerdict.NO);
    expect(trueVsFalse.getWitness()).not.toBeUndefined();
});

test("nested boolean child views are reached through schema view navigation", () => {
    const parent: SchemaView = view({additionalProperties: false}, SchemaDialect.DRAFT2020_12);
    const additionalProperties: SchemaView = parent.childView("additionalProperties");
    expect(additionalProperties).not.toBeNull();
    expect(additionalProperties.isFalse()).toBe(true);
});

test("dialect resolves from a $schema or jsonSchemaDialect URI", () => {
    const cases: any[] = fixture().dialectDefaults;
    cases.forEach((testCase: any) => {
        const expected: SchemaDialect = (<any>SchemaDialect)[testCase.expected];
        expect(SchemaDialectResolver.fromSchemaUri(testCase.schemaUri)).toBe(expected);
    });
});

test("a custom required vocabulary keyword produces Unknown", () => {
    const withCustomKeyword: SchemaView = view({type: "string", "x-custom-vocab-keyword": "value"}, SchemaDialect.DRAFT2020_12);
    const result: ContainmentResult = SchemaContainment.compare(withCustomKeyword, withCustomKeyword, context());
    expect(result.getVerdict()).toBe(ContainmentVerdict.UNKNOWN);
    expect(result.getEvidence().length).toBe(1);
    expect(result.getEvidence()[0].getRule()).toBe("unrecognized-keyword");
});

test("OAS 3.0 nullable only widens type when a type sibling is present", () => {
    const withTypeAndNullable: SchemaView = view({type: "string", nullable: true}, SchemaDialect.OAS30);
    const effectiveTypes: string[] = SchemaNormalizer.normalizeEffectiveTypes(withTypeAndNullable);
    expect(effectiveTypes).not.toBeNull();
    expect(effectiveTypes.indexOf("string") >= 0).toBe(true);
    expect(effectiveTypes.indexOf("null") >= 0).toBe(true);

    const withoutTypeButNullable: SchemaView = view({nullable: true}, SchemaDialect.OAS30);
    expect(SchemaNormalizer.normalizeEffectiveTypes(withoutTypeButNullable)).toBeNull();
});

test("legacy exclusive bounds normalize into a value/exclusivity pair", () => {
    const legacyView: SchemaView = view({minimum: 5, exclusiveMinimum: true}, SchemaDialect.OAS30);
    const bound = SchemaNormalizer.normalizeMinimum(legacyView);
    expect(bound).not.toBeNull();
    expect(bound.isExclusive()).toBe(true);
    expect(bound.getValue().compareTo(ExactDecimal.parse("5"))).toBe(0);
});

test("modern independent exclusive bounds collapse to the tighter simultaneous bound", () => {
    // The exact-precision aspect of this rule (an unsafe integer minimum one
    // exact unit tighter than an exclusiveMinimum that rounds to the same
    // double) is covered in openapi-compat-values.test.ts / ExactDecimal
    // directly: a raw JS number literal for those values has already lost the
    // distinguishing digit by the time it reaches this schema view, so this
    // test instead checks the same tighter-bound-selection logic with values
    // JavaScript can represent exactly.
    const schemaView: SchemaView = view({minimum: 5, exclusiveMinimum: 3}, SchemaDialect.DRAFT2020_12);
    const bound = SchemaNormalizer.normalizeMinimum(schemaView);
    expect(bound).not.toBeNull();
    expect(bound.isExclusive()).toBe(false);
    expect(bound.getValue().compareTo(ExactDecimal.parse("5"))).toBe(0);

    const schemaView2: SchemaView = view({minimum: 3, exclusiveMinimum: 5}, SchemaDialect.DRAFT2020_12);
    const bound2 = SchemaNormalizer.normalizeMinimum(schemaView2);
    expect(bound2).not.toBeNull();
    expect(bound2.isExclusive()).toBe(true);
    expect(bound2.getValue().compareTo(ExactDecimal.parse("5"))).toBe(0);
});

test("an unsupported keyword can never produce a vacuous Compatible result", () => {
    const withDynamicRef: SchemaView = view({"$dynamicRef": "#meta"}, SchemaDialect.DRAFT2020_12);
    const trueSchema: SchemaView = view(true, SchemaDialect.DRAFT2020_12);
    const result: ContainmentResult = SchemaContainment.compare(trueSchema, withDynamicRef, context());
    expect(result.getVerdict()).toBe(ContainmentVerdict.UNKNOWN);
});

test("CoverageRegistry has no default no-op for an unknown keyword", () => {
    expect(CoverageRegistry.classify(SchemaDialect.DRAFT2020_12, "x-totally-made-up")).toBeNull();
    expect(CoverageRegistry.classify(SchemaDialect.DRAFT2020_12, "type").getCategory()).toBe(CoverageRegistry.Category.ASSERTION);
    expect(CoverageRegistry.classify(SchemaDialect.DRAFT2020_12, "$dynamicRef").getCategory()).toBe(CoverageRegistry.Category.UNSUPPORTED);
});
