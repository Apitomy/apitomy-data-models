import {readJSON} from "./util/tutils";
import {ContainmentContext} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentContext";
import {ContainmentResult} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentResult";
import {ContainmentVerdict} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentVerdict";
import {SchemaContainment} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaContainment";
import {SchemaDialect} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaDialect";
import {SchemaView} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaView";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {ProviderRole} from "../src/io/apitomy/datamodels/openapi/compat/ProviderRole";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";

function context(): ContainmentContext {
    return ContainmentContext.of(HttpRole.REQUEST, ProviderRole.INPUT, CheckDirection.BACKWARD, CompatibilityPolicy.defaults());
}

function view(node: any): SchemaView {
    return new SchemaView(node, SchemaDialect.DRAFT2020_12, "urn:test:resource", "");
}

test("composition matrix", () => {
    const cases: any[] = readJSON("tests/fixtures/openapi-compat/schema-composition.json").cases;
    cases.forEach((testCase: any) => {
        const source: SchemaView = view(testCase.source);
        const target: SchemaView = view(testCase.target);
        const expected: ContainmentVerdict = (<any>ContainmentVerdict)[testCase.expected];

        const result: ContainmentResult = SchemaContainment.compare(source, target, context());
        expect(result.getVerdict()).toBe(expected);

        if (expected === ContainmentVerdict.NO) {
            expect(result.getWitness()).not.toBeUndefined();
        }
    });
});
