package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.util.CollectionUtil;

/**
 * The outcome of {@link SchemaContainment#compare}: a verdict, the evidence
 * that justifies it, and -- for {@link ContainmentVerdict#NO} -- a validated
 * counterexample instance.
 */
public final class ContainmentResult {

    private final ContainmentVerdict verdict;
    private final List<SchemaEvidence> evidence;
    private final JsonNode witness;

    public ContainmentResult(ContainmentVerdict verdict, List<SchemaEvidence> evidence, JsonNode witness) {
        if (verdict == null) {
            throw new IllegalArgumentException("verdict must not be null");
        }
        this.verdict = verdict;
        this.evidence = CollectionUtil.copyOfList(evidence);
        this.witness = witness;
    }

    /** A result with no evidence and no witness. */
    public static ContainmentResult of(ContainmentVerdict verdict) {
        return new ContainmentResult(verdict, new ArrayList<SchemaEvidence>(), null);
    }

    /** A {@link ContainmentVerdict#YES} result justified by a single piece of evidence. */
    public static ContainmentResult yes(SchemaEvidence evidence) {
        List<SchemaEvidence> list = new ArrayList<SchemaEvidence>();
        list.add(evidence);
        return new ContainmentResult(ContainmentVerdict.YES, list, null);
    }

    /** A {@link ContainmentVerdict#NO} result justified by evidence and a validated counterexample. */
    public static ContainmentResult no(SchemaEvidence evidence, JsonNode witness) {
        List<SchemaEvidence> list = new ArrayList<SchemaEvidence>();
        list.add(evidence);
        return new ContainmentResult(ContainmentVerdict.NO, list, witness);
    }

    /** An {@link ContainmentVerdict#UNKNOWN} result, optionally justified by evidence explaining why. */
    public static ContainmentResult unknown(SchemaEvidence evidence) {
        List<SchemaEvidence> list = new ArrayList<SchemaEvidence>();
        if (evidence != null) {
            list.add(evidence);
        }
        return new ContainmentResult(ContainmentVerdict.UNKNOWN, list, null);
    }

    /** The verdict. */
    public ContainmentVerdict getVerdict() {
        return verdict;
    }

    /** The evidence justifying this result, in the order it was produced. */
    public List<SchemaEvidence> getEvidence() {
        return CollectionUtil.copyOfList(evidence);
    }

    /**
     * A validated instance that satisfies the source schema but not the target
     * schema, or {@code null} if {@link #getVerdict()} is not
     * {@link ContainmentVerdict#NO} or no witness was constructed.
     */
    public JsonNode getWitness() {
        return witness;
    }
}
