import {readJSON} from "./util/tutils";
import {Library} from "../src/io/apitomy/datamodels/Library";
import {JsonUtil} from "../src/io/apitomy/datamodels/models/util/JsonUtil";
import {RootCapable} from "../src/io/apitomy/datamodels/models/RootCapable";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {SecuritySchemeCatalog} from "../src/io/apitomy/datamodels/openapi/compat/contract/SecuritySchemeCatalog";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";
import {SecurityRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/SecurityRules";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/security.json");
    for (const testCase of root.cases) {
        if (testCase.id === id) {
            return testCase;
        }
    }
    throw new Error("No fixture case named '" + id + "'");
}

function evaluate(id: string): CompatibilityFinding[] {
    const testCase: any = fixtureCase(id);
    const originalJson: any = testCase.original;
    const updatedJson: any = testCase.updated;

    const original: ContractDocument = ContractInterpreter.interpret(originalJson, ORIGINAL_URI);
    const updated: ContractDocument = ContractInterpreter.interpret(updatedJson, UPDATED_URI);
    const originalRoot: RootCapable = <RootCapable><any>Library.readRoot(JsonUtil.clone(originalJson));
    const updatedRoot: RootCapable = <RootCapable><any>Library.readRoot(JsonUtil.clone(updatedJson));
    const originalSchemes: SecuritySchemeCatalog = SecuritySchemeCatalog.from(originalRoot);
    const updatedSchemes: SecuritySchemeCatalog = SecuritySchemeCatalog.from(updatedRoot);

    const context: RuleContext = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD,
        CompatibilityPolicy.defaults());
    const originalInteraction: EffectiveInteraction = original.getInteractions()[0];
    const updatedInteraction: EffectiveInteraction = updated.getInteractions()[0];
    return SecurityRules.compare(originalInteraction, updatedInteraction, context, originalSchemes, updatedSchemes);
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

test("OR alternatives all retained is compatible", () => {
    expect(evaluate("or-alternatives-retained").length).toBe(0);
});

test("redundant alternative removal is safe", () => {
    const findings: CompatibilityFinding[] = evaluate("redundant-alternative-removed-is-safe");
    expect(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING)).toBe(false);
});

test("required scopes widened is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("required-scopes-widened-is-breaking");
    expect(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING)).toBe(true);
});

test("anonymous access removed is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("anonymous-access-removed-is-breaking");
    expect(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING)).toBe(true);
});

test("scheme renamed with updated reference is compatible", () => {
    expect(evaluate("scheme-renamed-with-updated-reference-is-compatible").length).toBe(0);
});

test("HTTP scheme name is compared case-insensitively", () => {
    expect(evaluate("http-scheme-name-compared-case-insensitively").length).toBe(0);
});

test("an AND requirement demanding an extra scheme is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("and-schemes-requiring-an-extra-scheme-is-breaking");
    expect(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING)).toBe(true);
});

test("a withdrawn credential flow is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("credential-flow-removed-is-breaking");
    expect(hasFinding(findings, FindingCode.CREDENTIAL_FLOW_REMOVED, FindingImpact.BREAKING)).toBe(true);
});
