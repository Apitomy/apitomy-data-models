package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.util.CollectionUtil;

/**
 * Matches interactions between two {@link ContractDocument}s: an operation
 * corresponds to another operation of the same method, at the same address,
 * across a version change -- not merely one with the same {@code operationId}
 * (kept as explanatory metadata only, never routing authority) and not one
 * merely reachable by coincidentally similar-looking JSON.
 * <p>
 * Matching happens in two passes: exact correspondence (identical method and
 * path template, or identical method and webhook name) first, so a moved
 * address never masquerades as an unrelated removal-then-addition; then, for
 * whatever remains unmatched, structural placeholder correspondence (same
 * method, same literal path segments in the same positions, differing only in
 * a placeholder's own name) -- for example, {@code /pets/{id}} and
 * {@code /pets/{petId}}. A structural candidate with more than one equally
 * eligible pairing on either side is never guessed at: it is left unmatched
 * and recorded as ambiguous instead.
 */
public final class InteractionMatcher {

    private InteractionMatcher() {
    }

    /** One corresponding pair of interactions, across the original and updated document. */
    public static final class Match {
        private final EffectiveInteraction original;
        private final EffectiveInteraction updated;
        private final boolean placeholderRenamed;

        Match(EffectiveInteraction original, EffectiveInteraction updated, boolean placeholderRenamed) {
            this.original = original;
            this.updated = updated;
            this.placeholderRenamed = placeholderRenamed;
        }

        public EffectiveInteraction getOriginal() {
            return original;
        }

        public EffectiveInteraction getUpdated() {
            return updated;
        }

        /** True if this pair was matched only by structural placeholder correspondence -- the path template itself differs by a placeholder's name. */
        public boolean isPlaceholderRenamed() {
            return placeholderRenamed;
        }
    }

    /** The outcome of matching every interaction in {@code original} against every interaction in {@code updated}. */
    public static final class MatchResult {
        private final List<Match> matched;
        private final List<EffectiveInteraction> removed;
        private final List<EffectiveInteraction> added;
        private final List<String> ambiguities;

        MatchResult(List<Match> matched, List<EffectiveInteraction> removed, List<EffectiveInteraction> added,
                List<String> ambiguities) {
            this.matched = CollectionUtil.copyOfList(matched);
            this.removed = CollectionUtil.copyOfList(removed);
            this.added = CollectionUtil.copyOfList(added);
            this.ambiguities = CollectionUtil.copyOfList(ambiguities);
        }

        /** Every original interaction paired with the updated interaction it corresponds to. */
        public List<Match> getMatched() {
            return CollectionUtil.copyOfList(matched);
        }

        /** Every original interaction with no corresponding updated interaction. */
        public List<EffectiveInteraction> getRemoved() {
            return CollectionUtil.copyOfList(removed);
        }

        /** Every updated interaction with no corresponding original interaction. */
        public List<EffectiveInteraction> getAdded() {
            return CollectionUtil.copyOfList(added);
        }

        /** Diagnostic notes for every candidate pairing rejected as ambiguous (more than one equally eligible match). */
        public List<String> getAmbiguities() {
            return CollectionUtil.copyOfList(ambiguities);
        }
    }

    /** Matches every reachable interaction in {@code original} against every reachable interaction in {@code updated}. */
    public static MatchResult match(ContractDocument original, ContractDocument updated) {
        List<EffectiveInteraction> originalRemaining = new ArrayList<EffectiveInteraction>(original.getInteractions());
        List<EffectiveInteraction> updatedRemaining = new ArrayList<EffectiveInteraction>(updated.getInteractions());
        List<Match> matched = new ArrayList<Match>();
        List<String> ambiguities = new ArrayList<String>();

        matchExact(originalRemaining, updatedRemaining, matched);
        matchStructural(originalRemaining, updatedRemaining, matched, ambiguities);

        return new MatchResult(matched, originalRemaining, updatedRemaining, ambiguities);
    }

    private static void matchExact(List<EffectiveInteraction> originalRemaining, List<EffectiveInteraction> updatedRemaining,
            List<Match> matched) {
        Map<String, EffectiveInteraction> byKey = new LinkedHashMap<String, EffectiveInteraction>();
        for (int i = 0; i < updatedRemaining.size(); i++) {
            EffectiveInteraction interaction = updatedRemaining.get(i);
            byKey.put(exactKey(interaction), interaction);
        }
        List<EffectiveInteraction> stillUnmatchedOriginal = new ArrayList<EffectiveInteraction>();
        for (int i = 0; i < originalRemaining.size(); i++) {
            EffectiveInteraction interaction = originalRemaining.get(i);
            EffectiveInteraction counterpart = byKey.get(exactKey(interaction));
            if (counterpart != null && updatedRemaining.contains(counterpart)) {
                matched.add(new Match(interaction, counterpart, false));
                updatedRemaining.remove(counterpart);
            } else {
                stillUnmatchedOriginal.add(interaction);
            }
        }
        originalRemaining.clear();
        originalRemaining.addAll(stillUnmatchedOriginal);
    }

    private static String exactKey(EffectiveInteraction interaction) {
        if (interaction.isWebhook()) {
            return "webhook:" + interaction.getHttpMethod() + ":" + interaction.getWebhookName();
        }
        return "path:" + interaction.getHttpMethod() + ":" + interaction.getPathTemplate();
    }

    private static void matchStructural(List<EffectiveInteraction> originalRemaining,
            List<EffectiveInteraction> updatedRemaining, List<Match> matched, List<String> ambiguities) {
        List<EffectiveInteraction> stillUnmatchedOriginal = new ArrayList<EffectiveInteraction>();
        for (int i = 0; i < originalRemaining.size(); i++) {
            EffectiveInteraction originalInteraction = originalRemaining.get(i);
            if (originalInteraction.isWebhook() || originalInteraction.getPathTemplate() == null) {
                stillUnmatchedOriginal.add(originalInteraction);
                continue;
            }
            List<EffectiveInteraction> candidates = new ArrayList<EffectiveInteraction>();
            for (int j = 0; j < updatedRemaining.size(); j++) {
                EffectiveInteraction updatedInteraction = updatedRemaining.get(j);
                if (!updatedInteraction.isWebhook() && updatedInteraction.getPathTemplate() != null
                        && originalInteraction.getHttpMethod().equals(updatedInteraction.getHttpMethod())
                        && structurallyCorresponds(originalInteraction.getPathTemplate(), updatedInteraction.getPathTemplate())) {
                    candidates.add(updatedInteraction);
                }
            }
            if (candidates.size() == 1) {
                EffectiveInteraction counterpart = candidates.get(0);
                matched.add(new Match(originalInteraction, counterpart, true));
                updatedRemaining.remove(counterpart);
            } else if (candidates.size() > 1) {
                ambiguities.add(originalInteraction.getHttpMethod() + " " + originalInteraction.getPathTemplate()
                        + " structurally corresponds to " + candidates.size()
                        + " updated path templates; no pairing was assumed");
                stillUnmatchedOriginal.add(originalInteraction);
            } else {
                stillUnmatchedOriginal.add(originalInteraction);
            }
        }
        originalRemaining.clear();
        originalRemaining.addAll(stillUnmatchedOriginal);
    }

    /**
     * True if {@code a} and {@code b} have the same number of {@code /}-separated
     * segments, every literal (non-placeholder) segment is identical at the
     * same position, and every placeholder position in one is a placeholder in
     * the other -- regardless of the placeholder's own name.
     */
    static boolean structurallyCorresponds(String a, String b) {
        String[] segmentsA = a.split("/", -1);
        String[] segmentsB = b.split("/", -1);
        if (segmentsA.length != segmentsB.length) {
            return false;
        }
        for (int i = 0; i < segmentsA.length; i++) {
            boolean placeholderA = isPlaceholder(segmentsA[i]);
            boolean placeholderB = isPlaceholder(segmentsB[i]);
            if (placeholderA != placeholderB) {
                return false;
            }
            if (!placeholderA && !segmentsA[i].equals(segmentsB[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPlaceholder(String segment) {
        return segment.length() >= 2 && segment.charAt(0) == '{' && segment.charAt(segment.length() - 1) == '}';
    }
}
