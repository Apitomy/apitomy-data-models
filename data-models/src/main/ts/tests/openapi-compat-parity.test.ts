import * as fs from "fs";
import * as path from "path";
import {readJSON} from "./util/tutils";
import {CheckOptions} from "../src/io/apitomy/datamodels/openapi/compat/CheckOptions";
import {CompatibilityVerdict} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityVerdict";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {ProviderRole} from "../src/io/apitomy/datamodels/openapi/compat/ProviderRole";
import {OpenApiCompatibilityChecker} from "../src/io/apitomy/datamodels/openapi/compat/OpenApiCompatibilityChecker";

/**
 * V5 cross-runtime parity: re-runs the same corpus
 * ({@code fixtures/openapi-compat/parity.json}) the Java
 * {@code OpenApiParityReportTest} already ran, through the TypeScript
 * checker, and asserts both runtimes agree on verdict and finding
 * code/impact/interactionId/role for every case in both directions.
 * <p>
 * The Java report is a required, non-optional input: a missing report
 * fails this test explicitly rather than silently skipping parity.
 */

interface FindingSnapshot {
    code: string;
    impact: string;
    interactionId: string;
    httpRole: string;
    providerRole: string;
}

interface DirectionSnapshot {
    verdict: string;
    findings: FindingSnapshot[];
}

interface CaseSnapshot {
    id: string;
    backward: DirectionSnapshot;
    forward: DirectionSnapshot;
}

function loadJavaReport(): {cases: CaseSnapshot[]} {
    const reportPath: string = path.resolve(__dirname, "../../openapi-compat-java-results.json");
    if (!fs.existsSync(reportPath)) {
        throw new Error("Java parity report not found at " + reportPath
            + " -- run `mvn -pl data-models -Dtest=OpenApiParityReportTest test` first (V5).");
    }
    return JSON.parse(fs.readFileSync(reportPath, "utf8"));
}

function toSnapshot(result: any): DirectionSnapshot {
    const findings: FindingSnapshot[] = result.getFindings().map((f: any) => {
        return {
            code: FindingCode[f.getCode()],
            impact: FindingImpact[f.getImpact()],
            interactionId: f.getInteractionId(),
            httpRole: f.getHttpRole() == null ? null : HttpRole[f.getHttpRole()],
            providerRole: f.getProviderRole() == null ? null : ProviderRole[f.getProviderRole()],
        };
    });
    return {verdict: CompatibilityVerdict[result.getVerdict()], findings: findings};
}

function assertSameDirection(caseId: string, direction: string, javaSnapshot: DirectionSnapshot, tsSnapshot: DirectionSnapshot): void {
    expect(tsSnapshot.verdict).toBe(javaSnapshot.verdict);
    expect(tsSnapshot.findings.length).toBe(javaSnapshot.findings.length);
    for (let i = 0; i < javaSnapshot.findings.length; i++) {
        const javaFinding: FindingSnapshot = javaSnapshot.findings[i];
        const tsFinding: FindingSnapshot = tsSnapshot.findings[i];
        expect(tsFinding.code).toBe(javaFinding.code);
        expect(tsFinding.impact).toBe(javaFinding.impact);
        expect(tsFinding.interactionId).toBe(javaFinding.interactionId);
        expect(tsFinding.httpRole).toBe(javaFinding.httpRole);
        expect(tsFinding.providerRole).toBe(javaFinding.providerRole);
    }
}

test("Java and TypeScript agree on the shared parity corpus", () => {
    const javaReport: {cases: CaseSnapshot[]} = loadJavaReport();
    const fixture: any = readJSON("tests/fixtures/openapi-compat/parity.json");

    const javaById: any = {};
    javaReport.cases.forEach((c) => { javaById[c.id] = c; });

    fixture.cases.forEach((testCase: any) => {
        const javaCase: CaseSnapshot = javaById[testCase.id];
        expect(javaCase).not.toBeUndefined();

        const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
        const backward: any = checker.checkBackwardJson(testCase.original, testCase.updated, CheckOptions.defaults());
        const forward: any = checker.checkForwardJson(testCase.original, testCase.updated, CheckOptions.defaults());

        assertSameDirection(testCase.id, "backward", javaCase.backward, toSnapshot(backward));
        assertSameDirection(testCase.id, "forward", javaCase.forward, toSnapshot(forward));
    });
});
