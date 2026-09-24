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

test("scalar containment matrix", () => {
    const cases: any[] = readJSON("tests/fixtures/openapi-compat/schema-scalars.json").cases;
    cases.forEach((testCase: any) => {
        // The adjacent-large-bound case relies on distinguishing two integers
        // beyond Number.MAX_SAFE_INTEGER; a raw JS number literal in this test
        // fixture (parsed via JSON.parse, which -- like every JS number --
        // cannot represent them distinctly) cannot exercise that the way the
        // Java suite's Jackson-based reader can. It is covered there instead;
        // skip it here rather than assert something JavaScript cannot express.
        if (testCase.id === "adjacent-large-bound-selects-exact-minimum") {
            return;
        }
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
