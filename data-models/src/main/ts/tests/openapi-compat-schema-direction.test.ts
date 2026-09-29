import {readJSON} from "./util/tutils";
import {DirectionalSchemaView} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/DirectionalSchemaView";
import {ExactDecimal} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ExactDecimal";
import {FormatRegistry} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/FormatRegistry";
import {ObjectContainment} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ObjectContainment";
import {SchemaDialect} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaDialect";
import {SchemaNormalizer} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaNormalizer";
import {SchemaView} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaView";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";

function fixture(): any {
    return readJSON("tests/fixtures/openapi-compat/schema-direction.json");
}

test("directional requiredness projection", () => {
    const cases: any[] = fixture().directionalCases;
    cases.forEach((testCase: any) => {
        const dialect: SchemaDialect = (<any>SchemaDialect)[testCase.dialect];
        const httpRole: HttpRole = (<any>HttpRole)[testCase.httpRole];
        const schema: SchemaView = new SchemaView(testCase.schema, dialect, "urn:test:resource", "");

        const directional: DirectionalSchemaView = DirectionalSchemaView.of(schema, httpRole, CompatibilityPolicy.defaults());

        if (testCase.expectedEnforcementCertain !== undefined) {
            expect(directional.isEnforcementCertain()).toBe(testCase.expectedEnforcementCertain);
        }

        const effective: SchemaView = directional.effectiveView();
        const effectiveRequired: string[] = ObjectContainment.requiredNames(effective);
        const expectedRequired: string[] = testCase.expectedEffectiveRequired;

        expect(effectiveRequired.length).toBe(expectedRequired.length);
        expectedRequired.forEach((name: string) => {
            expect(effectiveRequired.indexOf(name) >= 0).toBe(true);
        });
    });
});

test("format compatibility table", () => {
    const cases: any[] = fixture().formatCases;
    cases.forEach((testCase: any) => {
        const expected: FormatRegistry.FormatRelation = (<any>FormatRegistry.FormatRelation)[testCase.expected];

        if (testCase.generalRelate) {
            expect(FormatRegistry.relate(testCase.sourceFormat, testCase.targetFormat)).toBe(expected);
            return;
        }

        const sourceMinimum = testCase.sourceMinimum !== undefined
            ? SchemaNormalizer.Bound.of(ExactDecimal.parse(String(testCase.sourceMinimum)), false) : null;
        const sourceMaximum = testCase.sourceMaximum !== undefined
            ? SchemaNormalizer.Bound.of(ExactDecimal.parse(String(testCase.sourceMaximum)), false) : null;
        const targetMinimum = testCase.targetMinimum !== undefined
            ? SchemaNormalizer.Bound.of(ExactDecimal.parse(String(testCase.targetMinimum)), false) : null;
        const targetMaximum = testCase.targetMaximum !== undefined
            ? SchemaNormalizer.Bound.of(ExactDecimal.parse(String(testCase.targetMaximum)), false) : null;

        const actual: FormatRegistry.FormatRelation = FormatRegistry.relateNumericRange(
            testCase.sourceFormat, sourceMinimum, sourceMaximum, testCase.targetFormat, targetMinimum, targetMaximum);
        expect(actual).toBe(expected);
    });
});
