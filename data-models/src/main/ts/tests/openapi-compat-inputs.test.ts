import {readJSON} from "./util/tutils";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {InputRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/InputRules";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/inputs.json");
    for (const testCase of root.cases) {
        if (testCase.id === id) {
            return testCase;
        }
    }
    throw new Error("No fixture case named '" + id + "'");
}

function evaluate(id: string): CompatibilityFinding[] {
    const testCase: any = fixtureCase(id);
    const original: ContractDocument = ContractInterpreter.interpret(testCase.original, ORIGINAL_URI);
    const updated: ContractDocument = ContractInterpreter.interpret(testCase.updated, UPDATED_URI);
    const context: RuleContext = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD,
        CompatibilityPolicy.defaults());
    const originalInteraction: EffectiveInteraction = original.getInteractions()[0];
    const updatedInteraction: EffectiveInteraction = updated.getInteractions()[0];
    return InputRules.compare(originalInteraction, updatedInteraction, context);
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

test("removed optional query input is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("removed-optional-query-input");
    expect(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING)).toBe(true);
});

test("newly required parameter is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("newly-required-parameter");
    expect(hasFinding(findings, FindingCode.PARAMETER_REQUIRED_ADDED, FindingImpact.BREAKING)).toBe(true);
});

test("optional parameter addition is safe", () => {
    const findings: CompatibilityFinding[] = evaluate("optional-parameter-addition-is-safe");
    expect(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING)).toBe(false);
    expect(hasFinding(findings, FindingCode.PARAMETER_REQUIRED_ADDED, FindingImpact.BREAKING)).toBe(false);
});

test("same-name different-location parameters are distinct identities", () => {
    const findings: CompatibilityFinding[] = evaluate("same-name-different-location-are-distinct");
    expect(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING)).toBe(false);
});

test("case-only header rename is not a removal", () => {
    const findings: CompatibilityFinding[] = evaluate("case-only-header-rename-is-not-a-removal");
    expect(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING)).toBe(false);
});

test("narrowing enum on a retained parameter is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("narrowing-enum-on-retained-parameter-is-breaking");
    expect(hasFinding(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)).toBe(true);
});

test("missing body to required body is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("missing-body-vs-required-body-added");
    expect(hasFinding(findings, FindingCode.REQUEST_BODY_REQUIRED_ADDED, FindingImpact.BREAKING)).toBe(true);
});

test("required body stays required regardless of null or empty object acceptance", () => {
    const findings: CompatibilityFinding[] = evaluate("required-body-stays-required-even-if-schema-accepts-null-or-empty-object");
    expect(hasFinding(findings, FindingCode.REQUEST_BODY_REQUIRED_ADDED, FindingImpact.BREAKING)).toBe(true);
});

test("required body removed is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("required-body-removed");
    expect(hasFinding(findings, FindingCode.REQUEST_BODY_REMOVED, FindingImpact.BREAKING)).toBe(true);
});
