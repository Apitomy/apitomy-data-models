import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityResult";
import {FullCompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/FullCompatibilityResult";
import {CompatibilityVerdict} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityVerdict";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";

function finding(code: FindingCode, impact: FindingImpact, interactionId: string = "op-1"): CompatibilityFinding {
    return new CompatibilityFinding(code, impact, interactionId, null, null, null, null, "test finding");
}

test("no findings is compatible", () => {
    const result = new CompatibilityResult([], [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.COMPATIBLE);
});

test("only compatible findings is compatible", () => {
    const result = new CompatibilityResult(
        [finding(FindingCode.SCHEMA_COMPATIBLE, FindingImpact.COMPATIBLE)], [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.COMPATIBLE);
});

test("unresolved finding is indeterminate", () => {
    const result = new CompatibilityResult(
        [finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED)], [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.INDETERMINATE);
});

test("breaking finding is incompatible", () => {
    const result = new CompatibilityResult(
        [finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)], [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.INCOMPATIBLE);
});

test("breaking wins over unresolved but both are retained", () => {
    const result = new CompatibilityResult(
        [
            finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED),
            finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING),
        ], [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.INCOMPATIBLE);
    expect(result.getFindings().length).toEqual(2);
});

test("informational findings do not affect verdict", () => {
    const result = new CompatibilityResult(
        [finding(FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL)], [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.COMPATIBLE);
});

test("mutating caller-supplied array does not affect completed result", () => {
    const mutable: CompatibilityFinding[] = [];
    mutable.push(finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED));
    const result = new CompatibilityResult(mutable, [], []);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.INDETERMINATE);

    mutable.push(finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING));
    mutable.length = 0;

    expect(result.getVerdict()).toEqual(CompatibilityVerdict.INDETERMINATE);
    expect(result.getFindings().length).toEqual(1);
});

test("mutating a returned findings array does not affect later calls", () => {
    const result = new CompatibilityResult(
        [finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED)], [], []);

    const firstCall = result.getFindings();
    firstCall.length = 0;
    firstCall.push(finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING));

    expect(result.getFindings().length).toEqual(1);
    expect(result.getVerdict()).toEqual(CompatibilityVerdict.INDETERMINATE);
});

test("full result is incompatible if either direction is incompatible", () => {
    const compatible = new CompatibilityResult([], [], []);
    const incompatible = new CompatibilityResult(
        [finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)], [], []);
    const full = new FullCompatibilityResult(incompatible, compatible);
    expect(full.getVerdict()).toEqual(CompatibilityVerdict.INCOMPATIBLE);
    expect(full.getForwardResult()).toBe(compatible);
    expect(full.getBackwardResult()).toBe(incompatible);
});

test("full result is indeterminate if either direction is indeterminate and neither is incompatible", () => {
    const compatible = new CompatibilityResult([], [], []);
    const indeterminate = new CompatibilityResult(
        [finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED)], [], []);
    const full = new FullCompatibilityResult(compatible, indeterminate);
    expect(full.getVerdict()).toEqual(CompatibilityVerdict.INDETERMINATE);
});

test("full result is compatible when both directions are compatible", () => {
    const compatibleA = new CompatibilityResult([], [], []);
    const compatibleB = new CompatibilityResult([], [], []);
    const full = new FullCompatibilityResult(compatibleA, compatibleB);
    expect(full.getVerdict()).toEqual(CompatibilityVerdict.COMPATIBLE);
});

test("findings are sorted deterministically by interaction id", () => {
    const findingB = finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, "op-b");
    const findingA = finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, "op-a");
    const result = new CompatibilityResult([findingB, findingA], [], []);
    expect(result.getFindings()[0].getInteractionId()).toEqual("op-a");
    expect(result.getFindings()[1].getInteractionId()).toEqual("op-b");
});

test("coverage and assumptions are defensively copied", () => {
    const coverage: string[] = ["routing"];
    const assumptions: string[] = ["negotiation-stability"];

    const result = new CompatibilityResult([], coverage, assumptions);
    coverage.length = 0;
    assumptions.length = 0;

    expect(result.getCoverage()).toEqual(["routing"]);
    expect(result.getAssumptions()).toEqual(["negotiation-stability"]);

    const returnedCoverage = result.getCoverage();
    returnedCoverage.length = 0;
    expect(result.getCoverage()).toEqual(["routing"]);
});
