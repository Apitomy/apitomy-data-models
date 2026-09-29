import {readJSON} from "./util/tutils";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {RelationshipRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/RelationshipRules";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/relationships.json");
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
    return RelationshipRules.compare(originalInteraction, updatedInteraction, context);
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

test("operationId rename is informational", () => {
    const findings: CompatibilityFinding[] = evaluate("operation-id-rename-is-informational");
    expect(hasFinding(findings, FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL)).toBe(true);
});

test("deprecation flag change is informational", () => {
    const findings: CompatibilityFinding[] = evaluate("deprecation-flag-change-is-informational");
    expect(hasFinding(findings, FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL)).toBe(true);
});

test("tags change is informational", () => {
    const findings: CompatibilityFinding[] = evaluate("tags-change-is-informational");
    expect(hasFinding(findings, FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL)).toBe(true);
});

test("no metadata change produces no findings", () => {
    expect(evaluate("no-metadata-change-produces-no-findings").length).toBe(0);
});
