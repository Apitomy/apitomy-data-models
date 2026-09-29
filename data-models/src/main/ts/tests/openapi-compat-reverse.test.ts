import {readJSON} from "./util/tutils";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {ProviderRole} from "../src/io/apitomy/datamodels/openapi/compat/ProviderRole";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {InputRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/InputRules";
import {ResponseRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/ResponseRules";
import {ReverseInteractionRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/ReverseInteractionRules";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/reverse-interactions.json");
    for (const testCase of root.cases) {
        if (testCase.id === id) {
            return testCase;
        }
    }
    throw new Error("No fixture case named '" + id + "'");
}

function findByWebhook(document: ContractDocument, webhook: boolean): EffectiveInteraction {
    const interactions: EffectiveInteraction[] = document.getInteractions();
    for (const interaction of interactions) {
        if (interaction.isWebhook() === webhook) {
            return interaction;
        }
    }
    throw new Error("No " + (webhook ? "webhook" : "ordinary") + " interaction found");
}

function hasFindingWithRoles(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact,
        httpRole: HttpRole, providerRole: ProviderRole): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact && f.getHttpRole() === httpRole
        && f.getProviderRole() === providerRole);
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

function evaluate(id: string, policy: CompatibilityPolicy): CompatibilityFinding[] {
    const testCase: any = fixtureCase(id);
    const original: ContractDocument = ContractInterpreter.interpret(testCase.original, ORIGINAL_URI);
    const updated: ContractDocument = ContractInterpreter.interpret(testCase.updated, UPDATED_URI);
    const context: RuleContext = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD, policy);
    return ReverseInteractionRules.evaluate(context);
}

test("full four-row direction table on a shared narrowed schema", () => {
    const testCase: any = fixtureCase("four-row-direction-table-narrowed-enum");
    const original: ContractDocument = ContractInterpreter.interpret(testCase.original, ORIGINAL_URI);
    const updated: ContractDocument = ContractInterpreter.interpret(testCase.updated, UPDATED_URI);
    const context: RuleContext = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD,
        CompatibilityPolicy.defaults());

    const originalOrdinary: EffectiveInteraction = findByWebhook(original, false);
    const updatedOrdinary: EffectiveInteraction = findByWebhook(updated, false);
    let findings: CompatibilityFinding[] = [];
    findings = findings.concat(InputRules.compare(originalOrdinary, updatedOrdinary, context));
    findings = findings.concat(ResponseRules.compare(originalOrdinary, updatedOrdinary, context));
    findings = findings.concat(ReverseInteractionRules.evaluate(context));

    expect(hasFindingWithRoles(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, HttpRole.REQUEST,
        ProviderRole.INPUT)).toBe(true);
    expect(hasFindingWithRoles(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING, HttpRole.RESPONSE,
        ProviderRole.OUTPUT)).toBe(false);
    expect(hasFindingWithRoles(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING, HttpRole.REQUEST,
        ProviderRole.OUTPUT)).toBe(false);
    expect(hasFindingWithRoles(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, HttpRole.RESPONSE,
        ProviderRole.INPUT)).toBe(true);
});

test("new webhook defaults to Indeterminate unless opted in", () => {
    const defaultFindings: CompatibilityFinding[] = evaluate("new-webhook-default-indeterminate", CompatibilityPolicy.defaults());
    expect(hasFinding(defaultFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.UNRESOLVED)).toBe(true);
    expect(hasFinding(defaultFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.INFORMATIONAL)).toBe(false);

    const optedIn: CompatibilityPolicy = CompatibilityPolicy.defaults().withOptedInReverseInteractionAddition("webhook POST widgetCreated");
    const optedInFindings: CompatibilityFinding[] = evaluate("new-webhook-default-indeterminate", optedIn);
    expect(hasFinding(optedInFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.INFORMATIONAL)).toBe(true);
    expect(hasFinding(optedInFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.UNRESOLVED)).toBe(false);
});

test("removed webhook is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("removed-webhook-is-breaking", CompatibilityPolicy.defaults());
    expect(hasFinding(findings, FindingCode.REVERSE_INTERACTION_REMOVED, FindingImpact.BREAKING)).toBe(true);
});
