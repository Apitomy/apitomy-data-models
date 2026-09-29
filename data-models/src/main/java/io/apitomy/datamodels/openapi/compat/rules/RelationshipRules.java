package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;

/**
 * Behavioral and documentation metadata for a matched pair of interactions:
 * changes that are informational by policy (an {@code operationId} rename,
 * a deprecation-flag change, a tags change) rather than a proof this checker
 * establishes as a compatibility obligation.
 * <p>
 * Per the design contract: an {@code operationId} rename alone is
 * informational if all effective relationships remain valid -- this module
 * only reports the rename itself; whether some other relationship (a Link
 * target, a discriminator mapping) actually depended on the old
 * {@code operationId} is not established here (see the class-level gap note
 * below).
 * <p>
 * <b>Not attempted at this task's depth</b> (a documented gap, not a silent
 * assumption of equivalence): discriminator mapping dispatch-equivalence,
 * Link target resolution/broken-Link detection, callback runtime-destination
 * expression comparison, and unrecognized-extension change detection
 * ({@code EXTENSION_SEMANTICS_UNKNOWN}). Each requires either the
 * reference-graph plumbing built in T3/T4 (not yet threaded into
 * {@link RuleContext}) or vendor-extension capture that T11's effective
 * contract does not yet carry; closing these is future work, not claimed
 * complete here.
 */
public final class RelationshipRules {

    private RelationshipRules() {
    }

    public static List<CompatibilityFinding> compare(EffectiveInteraction original, EffectiveInteraction updated,
            RuleContext context) {
        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();

        if (!equalsNullable(original.getOperationId(), updated.getOperationId())) {
            findings.add(new CompatibilityFinding(FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL,
                    original.getInteractionId(), null, null, null, null,
                    "operationId changed from '" + original.getOperationId() + "' to '" + updated.getOperationId() + "'"));
        }
        if (original.isDeprecated() != updated.isDeprecated()) {
            findings.add(new CompatibilityFinding(FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL,
                    original.getInteractionId(), null, null, null, null,
                    "deprecated changed from " + original.isDeprecated() + " to " + updated.isDeprecated()));
        }
        if (!sameTags(original.getTags(), updated.getTags())) {
            findings.add(new CompatibilityFinding(FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL,
                    original.getInteractionId(), null, null, null, null, "tags changed"));
        }
        return findings;
    }

    private static boolean sameTags(List<String> a, List<String> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!b.contains(a.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsNullable(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
