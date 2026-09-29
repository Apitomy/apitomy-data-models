import {readJSON} from "./util/tutils";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {RoutingRules} from "../src/io/apitomy/datamodels/openapi/compat/rules/RoutingRules";
import {RuleContext} from "../src/io/apitomy/datamodels/openapi/compat/rules/RuleContext";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/routing.json");
    for (const testCase of root.cases) {
        if (testCase.id === id) {
            return testCase;
        }
    }
    throw new Error("No fixture case named '" + id + "'");
}

function interpret(documentJson: any): ContractDocument {
    return ContractInterpreter.interpret(documentJson, ORIGINAL_URI);
}

function context(testCase: any, direction: CheckDirection): RuleContext {
    const original: ContractDocument = interpret(testCase.original);
    const updated: ContractDocument = interpret(testCase.updated);
    return new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, direction, CompatibilityPolicy.defaults());
}

function hasFinding(findings: CompatibilityFinding[], code: FindingCode, impact: FindingImpact): boolean {
    return findings.some(f => f.getCode() === code && f.getImpact() === impact);
}

function countOfCode(findings: CompatibilityFinding[], code: FindingCode): number {
    return findings.filter(f => f.getCode() === code).length;
}

test("operation removed is breaking backward and operation added is breaking forward", () => {
    const testCase: any = fixtureCase("operation-removed-and-added");

    const backward: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
    expect(hasFinding(backward, FindingCode.OPERATION_REMOVED, FindingImpact.BREAKING)).toBe(true);
    expect(hasFinding(backward, FindingCode.OPERATION_ADDED, FindingImpact.INFORMATIONAL)).toBe(true);

    const forward: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.FORWARD));
    expect(hasFinding(forward, FindingCode.OPERATION_ADDED, FindingImpact.BREAKING)).toBe(true);
    expect(hasFinding(forward, FindingCode.OPERATION_REMOVED, FindingImpact.INFORMATIONAL)).toBe(true);
});

test("a removed server URL is breaking", () => {
    const testCase: any = fixtureCase("removed-server-url");
    const findings: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
    expect(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.BREAKING)).toBe(true);
});

test("narrowing a finite server variable enum removes an address", () => {
    const testCase: any = fixtureCase("finite-variable-enum-narrowed");
    const findings: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
    expect(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.BREAKING)).toBe(true);
});

test("placeholder renaming is matched, not reported as removed and added", () => {
    const testCase: any = fixtureCase("placeholder-renaming-still-matches");
    const findings: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
    expect(hasFinding(findings, FindingCode.OPERATION_REMOVED, FindingImpact.BREAKING)).toBe(false);
    expect(hasFinding(findings, FindingCode.OPERATION_ADDED, FindingImpact.INFORMATIONAL)).toBe(false);
});

test("an open-ended server variable produces unresolved, not breaking", () => {
    const testCase: any = fixtureCase("open-ended-server-variable-cannot-be-enumerated");
    const findings: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
    expect(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.UNRESOLVED)).toBe(true);
    expect(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.BREAKING)).toBe(false);
});

test("a new literal route shadowing an existing templated route is unresolved", () => {
    const testCase: any = fixtureCase("concrete-route-shadowing");
    const findings: CompatibilityFinding[] = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
    expect(hasFinding(findings, FindingCode.ROUTE_SHADOWING, FindingImpact.UNRESOLVED)).toBe(true);
    expect(countOfCode(findings, FindingCode.ROUTE_SHADOWING)).toBe(1);
});
