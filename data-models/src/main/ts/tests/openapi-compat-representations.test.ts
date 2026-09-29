import {readJSON} from "./util/tutils";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {RepresentationRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/RepresentationRules";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/representations.json");
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
    return RepresentationRules.compare(originalInteraction, updatedInteraction, context);
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

test("omitted versus explicit default style/explode is equivalent", () => {
    const findings: CompatibilityFinding[] = evaluate("omitted-vs-explicit-default-style-explode-is-equivalent");
    expect(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.BREAKING)).toBe(false);
    expect(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED)).toBe(false);
});

test("CSV to repeated query encoding is breaking for an array value", () => {
    const findings: CompatibilityFinding[] = evaluate("csv-to-repeated-query-encoding-is-breaking");
    expect(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.BREAKING)).toBe(true);
});

test("an explode change on a scalar is irrelevant", () => {
    const findings: CompatibilityFinding[] = evaluate("explode-change-on-scalar-is-irrelevant");
    expect(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.BREAKING)).toBe(false);
    expect(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED)).toBe(false);
});

test("a content-to-schema switch is unresolved", () => {
    const findings: CompatibilityFinding[] = evaluate("content-to-schema-switch-is-unresolved");
    expect(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED)).toBe(true);
});

test("byte to binary format change is breaking", () => {
    const findings: CompatibilityFinding[] = evaluate("byte-to-binary-format-change-is-breaking");
    expect(hasFinding(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)).toBe(true);
});

test("unknown format pair change is unresolved", () => {
    const findings: CompatibilityFinding[] = evaluate("unknown-format-pair-change-is-unresolved");
    expect(hasFinding(findings, FindingCode.FORMAT_RELATION_UNKNOWN, FindingImpact.UNRESOLVED)).toBe(true);
});
