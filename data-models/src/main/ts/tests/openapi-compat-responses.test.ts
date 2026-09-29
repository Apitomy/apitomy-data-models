import {readJSON} from "./util/tutils";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {ResponseRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/ResponseRules";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/responses.json");
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
    return ResponseRules.compare(originalInteraction, updatedInteraction, context);
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

test("a new status with no old coverage is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("new-202-with-only-old-200-is-breaking");
    expect(hasFinding(findings, FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING)).toBe(true);
});

test("a new status covered by old default is not reported as added", () => {
    const findings: CompatibilityFinding[] = evaluate("new-404-covered-by-old-default-is-not-added");
    expect(hasFinding(findings, FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING)).toBe(false);
});

test("an incompatible refinement discovered under the default is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("incompatible-refinement-under-default-is-breaking");
    expect(hasFinding(findings, FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING)).toBe(false);
    expect(hasFinding(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING)).toBe(true);
});

test("redundant explicit entry removal through equivalent default is safe", () => {
    const findings: CompatibilityFinding[] = evaluate("redundant-explicit-entry-removal-through-equivalent-default-is-safe");
    expect(hasFinding(findings, FindingCode.RESPONSE_CAPABILITY_REMOVED, FindingImpact.BREAKING)).toBe(false);
    expect(hasFinding(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING)).toBe(false);
});

test("removed response media type is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("removed-response-media-type-is-breaking");
    expect(hasFinding(findings, FindingCode.RESPONSE_MEDIA_TYPE_REMOVED, FindingImpact.BREAKING)).toBe(true);
});

test("added response media type is informational", () => {
    const findings: CompatibilityFinding[] = evaluate("added-response-media-type-is-informational");
    expect(hasFinding(findings, FindingCode.RESPONSE_MEDIA_TYPE_ADDED, FindingImpact.INFORMATIONAL)).toBe(true);
});

test("removed response header guarantee is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("removed-response-header-guarantee-is-breaking");
    expect(hasFinding(findings, FindingCode.RESPONSE_HEADER_GUARANTEE_REMOVED, FindingImpact.BREAKING)).toBe(true);
});
