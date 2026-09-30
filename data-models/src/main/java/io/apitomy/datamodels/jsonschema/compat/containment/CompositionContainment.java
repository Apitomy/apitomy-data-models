package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Composition containment: {@code allOf}/{@code anyOf}/{@code oneOf}/{@code not}.
 * Implements exactly the sound sufficient rules below, falling back to
 * {@link ContainmentVerdict#UNKNOWN} whenever a rule's premises cannot be
 * proved -- a failed sufficient proof is never treated as a disproof:
 *
 * <pre>
 * S subset (T1 AND T2) if S subset T1 and S subset T2
 * (S1 OR S2) subset T if S1 subset T and S2 subset T
 * S subset (T1 OR T2) if S subset either branch
 * (S1 AND S2) subset T if either source conjunct is a subset of T
 * NOT S subset NOT T if T subset S
 * </pre>
 * <p>
 * {@code oneOf} on the target side is deliberately not given the same
 * "subset of either branch" treatment as {@code anyOf}: adding an overlapping
 * branch to a {@code oneOf} can invalidate values that used to match exactly
 * one branch, so branch-count widening must never prove YES here. A
 * {@code oneOf} target is only resolved when it reduces to zero or one
 * branch (where the exclusivity requirement is vacuous); anything more is
 * {@link ContainmentVerdict#UNKNOWN}. {@code if}/{@code then}/{@code else}
 * conditionals are not analyzed at all.
 */
public final class CompositionContainment {

    private CompositionContainment() {
    }

    public static ContainmentResult compare(SchemaView source, SchemaView target, ContainmentContext context, Set<String> visited) {
        if (source.hasKeyword("if") || target.hasKeyword("if")) {
            return ContainmentResult.unknown(evidence(source, target, "conditional-unsupported",
                    "if/then/else conditional composition is not analyzed"));
        }

        if (target.hasKeyword("allOf")) {
            return compareTargetAllOf(source, target, context, visited);
        }

        if (source.hasKeyword("not") && target.hasKeyword("not")
                && !hasAnyOtherComposition(source) && !hasAnyOtherComposition(target)) {
            ContainmentResult contrapositive = tryContrapositive(source, target, context, visited);
            if (contrapositive != null) {
                return contrapositive;
            }
        }

        if (target.hasKeyword("anyOf")) {
            ContainmentResult result = compareTargetOrGroup(source, target, "anyOf", context, visited);
            if (result != null) {
                return result;
            }
        }

        if (target.hasKeyword("oneOf")) {
            List<SchemaView> branches = branchesOf(target, "oneOf");
            if (branches.size() <= 1) {
                ContainmentResult result = compareTargetOrGroup(source, target, "oneOf", context, visited);
                if (result != null) {
                    return result;
                }
            }
        }

        if (source.hasKeyword("allOf")) {
            ContainmentResult result = compareSourceAllOf(source, target, context, visited);
            if (result != null) {
                return result;
            }
        }

        if (source.hasKeyword("anyOf")) {
            ContainmentResult result = compareSourceOrGroup(source, target, "anyOf", context, visited);
            if (result != null) {
                return result;
            }
        }

        if (source.hasKeyword("oneOf")) {
            ContainmentResult result = compareSourceOrGroup(source, target, "oneOf", context, visited);
            if (result != null) {
                return result;
            }
        }

        return ContainmentResult.unknown(evidence(source, target, "composition-unresolved",
                "No sufficient composition rule proved containment for this schema shape"));
    }

    private static boolean hasAnyOtherComposition(SchemaView schema) {
        return schema.hasKeyword("allOf") || schema.hasKeyword("anyOf") || schema.hasKeyword("oneOf");
    }

    /** Rule: S subset (T1 AND T2) if S subset T1 and S subset T2 -- here T1..Tn are `rest` plus every allOf branch. */
    private static ContainmentResult compareTargetAllOf(SchemaView source, SchemaView target, ContainmentContext context, Set<String> visited) {
        List<SchemaView> parts = new ArrayList<SchemaView>();
        parts.add(withoutKeyword(target, "allOf"));
        parts.addAll(branchesOf(target, "allOf"));

        boolean unresolved = false;
        for (int i = 0; i < parts.size(); i++) {
            ContainmentResult partResult = SchemaContainment.compare(source, parts.get(i), context, visited);
            if (partResult.getVerdict() == ContainmentVerdict.NO) {
                JsonNode witness = partResult.getWitness();
                if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                        && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
                    return ContainmentResult.no(evidence(source, target, "allOf-conjunct-rejected",
                            "A witness satisfying the source schema fails one of the target schema's `allOf` "
                                    + "conjuncts"), witness);
                }
                unresolved = true;
                continue;
            }
            if (partResult.getVerdict() == ContainmentVerdict.UNKNOWN) {
                unresolved = true;
            }
        }
        if (unresolved) {
            return ContainmentResult.unknown(evidence(source, target, "allOf-unresolved",
                    "Could not prove the source schema is contained by every conjunct of the target schema's `allOf`"));
        }
        return ContainmentResult.yes(evidence(source, target, "allOf-all-conjuncts-contained",
                "The source schema is contained by every conjunct of the target schema's `allOf`"));
    }

    /**
     * Rule: S subset (T1 OR T2) if S subset either branch -- used for `anyOf`, and for `oneOf` only when it has
     * reduced to zero or one branch (see the class Javadoc on why `oneOf` is not given this treatment in general).
     * The `rest` of the target (its own keywords alongside the OR-group) is combined with AND semantics: if the
     * source fails to be contained by `rest` with a confirmed witness, that witness also fails the whole target.
     */
    private static ContainmentResult compareTargetOrGroup(SchemaView source, SchemaView target, String keyword,
            ContainmentContext context, Set<String> visited) {
        SchemaView rest = withoutKeyword(target, keyword);
        ContainmentResult restResult = SchemaContainment.compare(source, rest, context, visited);
        if (restResult.getVerdict() == ContainmentVerdict.NO) {
            JsonNode witness = restResult.getWitness();
            if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                    && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
                return ContainmentResult.no(evidence(source, target, keyword + "-rest-rejected",
                        "A witness satisfying the source schema fails the target schema's own constraints "
                                + "alongside its `" + keyword + "`"), witness);
            }
            return null;
        }
        if (restResult.getVerdict() != ContainmentVerdict.YES) {
            return null;
        }
        List<SchemaView> branches = branchesOf(target, keyword);
        for (int i = 0; i < branches.size(); i++) {
            ContainmentResult branchResult = SchemaContainment.compare(source, branches.get(i), context, visited);
            if (branchResult.getVerdict() == ContainmentVerdict.YES) {
                return ContainmentResult.yes(evidence(source, target, keyword + "-branch-contained",
                        "The source schema is contained by one branch of the target schema's `" + keyword + "`"));
            }
        }
        // Per the plan: do not infer non-containment merely because sufficient
        // branch matching failed for every branch; additions/removals alone are
        // insufficient evidence of incompatibility.
        return null;
    }

    /** Rule: (S1 AND S2) subset T if either source conjunct is a subset of T -- here S1..Sn are `rest` plus every allOf branch. */
    private static ContainmentResult compareSourceAllOf(SchemaView source, SchemaView target, ContainmentContext context, Set<String> visited) {
        SchemaView rest = withoutKeyword(source, "allOf");
        if (SchemaContainment.compare(rest, target, context, visited).getVerdict() == ContainmentVerdict.YES) {
            return ContainmentResult.yes(evidence(source, target, "allOf-rest-sufficient",
                    "The source schema's own constraints alongside its `allOf` already suffice to prove containment"));
        }
        List<SchemaView> branches = branchesOf(source, "allOf");
        for (int i = 0; i < branches.size(); i++) {
            if (SchemaContainment.compare(branches.get(i), target, context, visited).getVerdict() == ContainmentVerdict.YES) {
                return ContainmentResult.yes(evidence(source, target, "allOf-conjunct-sufficient",
                        "One conjunct of the source schema's `allOf` already suffices to prove containment"));
            }
        }
        return null;
    }

    /** Rule: (S1 OR S2) subset T if S1 subset T and S2 subset T -- used for `anyOf` and `oneOf` as the source. */
    private static ContainmentResult compareSourceOrGroup(SchemaView source, SchemaView target, String keyword,
            ContainmentContext context, Set<String> visited) {
        SchemaView rest = withoutKeyword(source, keyword);
        if (SchemaContainment.compare(rest, target, context, visited).getVerdict() == ContainmentVerdict.YES) {
            return ContainmentResult.yes(evidence(source, target, keyword + "-rest-sufficient",
                    "The source schema's own constraints alongside its `" + keyword + "` already suffice to "
                            + "prove containment"));
        }
        List<SchemaView> branches = branchesOf(source, keyword);
        if (branches.isEmpty()) {
            return null;
        }
        for (int i = 0; i < branches.size(); i++) {
            if (SchemaContainment.compare(branches.get(i), target, context, visited).getVerdict() != ContainmentVerdict.YES) {
                return null;
            }
        }
        return ContainmentResult.yes(evidence(source, target, keyword + "-all-branches-contained",
                "Every branch of the source schema's `" + keyword + "` is contained by the target schema"));
    }

    /** Rule: NOT S subset NOT T if T subset S -- only attempted when neither side mixes `not` with another composition keyword. */
    private static ContainmentResult tryContrapositive(SchemaView source, SchemaView target, ContainmentContext context, Set<String> visited) {
        SchemaView sourceInner = source.childView("not");
        SchemaView targetInner = target.childView("not");
        if (sourceInner == null || targetInner == null) {
            return null;
        }
        SchemaView sourceRest = withoutKeyword(source, "not");
        SchemaView targetRest = withoutKeyword(target, "not");
        if (!isEmptySchema(sourceRest) || !isEmptySchema(targetRest)) {
            return null;
        }
        ContainmentResult reversed = SchemaContainment.compare(targetInner, sourceInner, context, visited);
        if (reversed.getVerdict() == ContainmentVerdict.YES) {
            return ContainmentResult.yes(evidence(source, target, "not-contrapositive",
                    "The target's negated schema is contained by the source's negated schema, so the negations "
                            + "are contained in reverse"));
        }
        return null;
    }

    private static boolean isEmptySchema(SchemaView schema) {
        return schema.isObject() && JsonUtil.keys(JsonUtil.toObject(schema.getNode())).isEmpty();
    }

    private static List<SchemaView> branchesOf(SchemaView schema, String keyword) {
        List<SchemaView> result = new ArrayList<SchemaView>();
        JsonNode node = schema.getKeyword(keyword);
        if (node == null || !JsonUtil.isArray(node)) {
            return result;
        }
        List<JsonNode> items = JsonUtil.toList(node);
        for (int i = 0; i < items.size(); i++) {
            result.add(childOf(schema, items.get(i), keyword + "/" + i));
        }
        return result;
    }

    private static SchemaView withoutKeyword(SchemaView schema, String keyword) {
        ObjectNode result = JsonUtil.objectNode();
        if (schema.isObject()) {
            ObjectNode object = JsonUtil.toObject(schema.getNode());
            List<String> keys = JsonUtil.keys(object);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                if (!key.equals(keyword)) {
                    JsonUtil.setProperty(result, key, JsonUtil.getProperty(object, key));
                }
            }
        }
        String pointer = schema.getPointer() != null ? schema.getPointer() + "/-" + keyword : null;
        return new SchemaView(result, schema.getDialect(), schema.getResourceUri(), pointer, schema.getDocumentRoot());
    }

    private static SchemaView childOf(SchemaView parent, JsonNode node, String pointerSuffix) {
        String pointer = parent.getPointer() != null ? parent.getPointer() + "/" + pointerSuffix : null;
        return new SchemaView(node, parent.getDialect(), parent.getResourceUri(), pointer, parent.getDocumentRoot()).resolveRef();
    }

    private static SchemaEvidence evidence(SchemaView source, SchemaView target, String rule, String message) {
        return new SchemaEvidence(source.getPointer(), target.getPointer(), rule, message);
    }
}
