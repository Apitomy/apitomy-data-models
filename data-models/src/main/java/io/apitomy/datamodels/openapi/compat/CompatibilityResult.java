package io.apitomy.datamodels.openapi.compat;

import io.apitomy.datamodels.util.CollectionUtil;

import java.util.List;

/**
 * Result of a single-direction ({@link CheckDirection#BACKWARD} or
 * {@link CheckDirection#FORWARD}) compatibility check.
 * <p>
 * The {@link #getVerdict()} is derived entirely from {@link #getFindings()}: a
 * {@link FindingImpact#BREAKING} finding makes the result
 * {@link CompatibilityVerdict#INCOMPATIBLE}; otherwise a
 * {@link FindingImpact#UNRESOLVED} finding makes it
 * {@link CompatibilityVerdict#INDETERMINATE}; otherwise it is
 * {@link CompatibilityVerdict#COMPATIBLE}. There is no separate boolean escape
 * hatch that collapses {@code INDETERMINATE} into a pass.
 * <p>
 * Instances are immutable: constructor arguments are defensively copied, and
 * accessors return copies, so a completed result cannot be affected by later
 * mutation of caller-held collections (including the list passed to the
 * constructor) or by mutating a previously returned collection.
 */
public final class CompatibilityResult {

    private final CompatibilityVerdict verdict;
    private final List<CompatibilityFinding> findings;
    private final List<String> coverage;
    private final List<String> assumptions;

    /**
     * @param findings every finding discovered while checking this direction
     * @param coverage identifiers of the constructs that were actually analyzed
     * @param assumptions human-readable descriptions of policy assumptions
     *        that were relied on while producing this result
     */
    public CompatibilityResult(List<CompatibilityFinding> findings, List<String> coverage,
            List<String> assumptions) {
        List<CompatibilityFinding> findingsCopy = CollectionUtil.copyOfList(findings);
        sortFindings(findingsCopy);
        this.findings = findingsCopy;
        this.coverage = CollectionUtil.copyOfList(coverage);
        this.assumptions = CollectionUtil.copyOfList(assumptions);
        this.verdict = aggregate(this.findings);
    }

    /**
     * Derives the overall verdict for a set of findings: any {@code BREAKING}
     * finding makes the result {@code INCOMPATIBLE}; otherwise any
     * {@code UNRESOLVED} finding makes it {@code INDETERMINATE}; otherwise
     * {@code COMPATIBLE}.
     */
    public static CompatibilityVerdict aggregate(List<CompatibilityFinding> findings) {
        boolean anyUnresolved = false;
        for (int i = 0; i < findings.size(); i++) {
            CompatibilityFinding finding = findings.get(i);
            if (finding.getImpact() == FindingImpact.BREAKING) {
                return CompatibilityVerdict.INCOMPATIBLE;
            }
            if (finding.getImpact() == FindingImpact.UNRESOLVED) {
                anyUnresolved = true;
            }
        }
        if (anyUnresolved) {
            return CompatibilityVerdict.INDETERMINATE;
        }
        return CompatibilityVerdict.COMPATIBLE;
    }

    /**
     * Sorts findings in place by interaction id, then code, then original
     * location, then updated location. Implemented as an insertion sort rather
     * than {@code Collections.sort}/streams: finding lists are small per
     * check, and both of those are unavailable to the transpiled TypeScript
     * target.
     */
    private static void sortFindings(List<CompatibilityFinding> findings) {
        for (int i = 1; i < findings.size(); i++) {
            CompatibilityFinding key = findings.get(i);
            int j = i - 1;
            while (j >= 0 && findings.get(j).compareForSorting(key) > 0) {
                findings.set(j + 1, findings.get(j));
                j = j - 1;
            }
            findings.set(j + 1, key);
        }
    }

    /** The overall verdict for this direction, derived from {@link #getFindings()}. */
    public CompatibilityVerdict getVerdict() {
        return verdict;
    }

    /** Every finding discovered while checking this direction, in deterministic order. */
    public List<CompatibilityFinding> getFindings() {
        return CollectionUtil.copyOfList(findings);
    }

    /** Identifiers of the constructs that were actually analyzed for this result. */
    public List<String> getCoverage() {
        return CollectionUtil.copyOfList(coverage);
    }

    /** Human-readable descriptions of policy assumptions relied on for this result. */
    public List<String> getAssumptions() {
        return CollectionUtil.copyOfList(assumptions);
    }
}
