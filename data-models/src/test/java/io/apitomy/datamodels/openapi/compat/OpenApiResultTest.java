package io.apitomy.datamodels.openapi.compat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiResultTest {

    private static CompatibilityFinding finding(FindingCode code, FindingImpact impact) {
        return new CompatibilityFinding(code, impact, "op-1", null, null, null, null, "test finding");
    }

    private static CompatibilityFinding finding(FindingCode code, FindingImpact impact, String interactionId) {
        return new CompatibilityFinding(code, impact, interactionId, null, null, null, null, "test finding");
    }

    @Test
    void noFindingsIsCompatible() {
        var result = new CompatibilityResult(List.of(), List.of(), List.of());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
    }

    @Test
    void onlyCompatibleFindingsIsCompatible() {
        var result = new CompatibilityResult(
                List.of(finding(FindingCode.SCHEMA_COMPATIBLE, FindingImpact.COMPATIBLE)),
                List.of(), List.of());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
    }

    @Test
    void unresolvedFindingIsIndeterminate() {
        var result = new CompatibilityResult(
                List.of(finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED)),
                List.of(), List.of());
        assertEquals(CompatibilityVerdict.INDETERMINATE, result.getVerdict());
    }

    @Test
    void breakingFindingIsIncompatible() {
        var result = new CompatibilityResult(
                List.of(finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)),
                List.of(), List.of());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
    }

    @Test
    void breakingWinsOverUnresolvedButBothAreRetained() {
        var result = new CompatibilityResult(
                List.of(
                        finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED),
                        finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)),
                List.of(), List.of());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
        assertEquals(2, result.getFindings().size());
    }

    @Test
    void informationalFindingsDoNotAffectVerdict() {
        var result = new CompatibilityResult(
                List.of(finding(FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL)),
                List.of(), List.of());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
    }

    @Test
    void mutatingCallerSuppliedListDoesNotAffectCompletedResult() {
        List<CompatibilityFinding> mutable = new ArrayList<>();
        mutable.add(finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED));
        var result = new CompatibilityResult(mutable, List.of(), List.of());
        assertEquals(CompatibilityVerdict.INDETERMINATE, result.getVerdict());

        mutable.add(finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING));
        mutable.clear();

        assertEquals(CompatibilityVerdict.INDETERMINATE, result.getVerdict(),
                "constructor argument mutation after the fact must not change the completed result");
        assertEquals(1, result.getFindings().size());
    }

    @Test
    void mutatingReturnedFindingsListDoesNotAffectLaterCalls() {
        var result = new CompatibilityResult(
                List.of(finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED)),
                List.of(), List.of());

        List<CompatibilityFinding> firstCall = result.getFindings();
        firstCall.clear();
        firstCall.add(finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING));

        assertEquals(1, result.getFindings().size(),
                "mutating a previously returned collection must not affect the result's internal state");
        assertEquals(CompatibilityVerdict.INDETERMINATE, result.getVerdict());
    }

    @Test
    void fullResultIncompatibleIfEitherDirectionIncompatible() {
        var compatible = new CompatibilityResult(List.of(), List.of(), List.of());
        var incompatible = new CompatibilityResult(
                List.of(finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING)),
                List.of(), List.of());
        var full = new FullCompatibilityResult(incompatible, compatible);
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, full.getVerdict());
        assertSame(compatible, full.getForwardResult());
        assertSame(incompatible, full.getBackwardResult());
    }

    @Test
    void fullResultIndeterminateIfEitherDirectionIndeterminateAndNeitherIncompatible() {
        var compatible = new CompatibilityResult(List.of(), List.of(), List.of());
        var indeterminate = new CompatibilityResult(
                List.of(finding(FindingCode.SCHEMA_UNRESOLVED, FindingImpact.UNRESOLVED)),
                List.of(), List.of());
        var full = new FullCompatibilityResult(compatible, indeterminate);
        assertEquals(CompatibilityVerdict.INDETERMINATE, full.getVerdict());
    }

    @Test
    void fullResultCompatibleWhenBothDirectionsAreCompatible() {
        var compatibleA = new CompatibilityResult(List.of(), List.of(), List.of());
        var compatibleB = new CompatibilityResult(List.of(), List.of(), List.of());
        var full = new FullCompatibilityResult(compatibleA, compatibleB);
        assertEquals(CompatibilityVerdict.COMPATIBLE, full.getVerdict());
    }

    @Test
    void findingsAreSortedDeterministicallyByInteractionId() {
        var findingB = finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, "op-b");
        var findingA = finding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, "op-a");
        var result = new CompatibilityResult(List.of(findingB, findingA), List.of(), List.of());
        assertEquals("op-a", result.getFindings().get(0).getInteractionId());
        assertEquals("op-b", result.getFindings().get(1).getInteractionId());
    }

    @Test
    void coverageAndAssumptionsAreDefensivelyCopied() {
        List<String> coverage = new ArrayList<>();
        coverage.add("routing");
        List<String> assumptions = new ArrayList<>();
        assumptions.add("negotiation-stability");

        var result = new CompatibilityResult(List.of(), coverage, assumptions);
        coverage.clear();
        assumptions.clear();

        assertEquals(List.of("routing"), result.getCoverage());
        assertEquals(List.of("negotiation-stability"), result.getAssumptions());

        List<String> returnedCoverage = result.getCoverage();
        returnedCoverage.clear();
        assertTrue(result.getCoverage().contains("routing"));
    }
}
