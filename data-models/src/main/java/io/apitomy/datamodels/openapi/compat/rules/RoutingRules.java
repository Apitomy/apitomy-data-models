package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.contract.AddressSet;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.InteractionMatcher;

/**
 * Operation correspondence, deployment addresses, and route overlap: which
 * documented operations still exist, whether their effective addresses are
 * still reachable, and whether a newly added literal route risks silently
 * intercepting requests a still-existing templated route used to handle.
 * <p>
 * Every judgement here is direction-aware through {@link RuleContext#getDirection()}:
 * for a {@link CheckDirection#BACKWARD} check, an operation/address present in
 * the original but missing from the updated document is breaking (existing
 * consumers lose it); for {@link CheckDirection#FORWARD}, the reverse holds
 * (an operation/address the updated document added, which the original
 * cannot serve, is breaking for that direction).
 */
public final class RoutingRules {

    private RoutingRules() {
    }

    public static List<CompatibilityFinding> evaluate(RuleContext context) {
        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();
        boolean backward = context.getDirection() == CheckDirection.BACKWARD;
        InteractionMatcher.MatchResult matchResult = InteractionMatcher.match(context.getOriginalDocument(),
                context.getUpdatedDocument());

        evaluateRemovedAndAdded(matchResult, context, backward, findings);
        evaluateAmbiguities(matchResult, findings);
        evaluateAddressCoverage(matchResult, context, backward, findings);
        evaluateRouteShadowing(matchResult, backward, findings);

        return findings;
    }

    private static void evaluateRemovedAndAdded(InteractionMatcher.MatchResult matchResult, RuleContext context,
            boolean backward, List<CompatibilityFinding> findings) {
        List<EffectiveInteraction> removed = matchResult.getRemoved();
        for (int i = 0; i < removed.size(); i++) {
            EffectiveInteraction interaction = removed.get(i);
            FindingImpact impact = backward ? FindingImpact.BREAKING : FindingImpact.INFORMATIONAL;
            findings.add(new CompatibilityFinding(FindingCode.OPERATION_REMOVED, impact, interaction.getInteractionId(),
                    context.originalLocation(interaction.getDeclarationPointer()), null, null, null,
                    "Operation '" + interaction.getInteractionId() + "' is no longer present"));
        }
        List<EffectiveInteraction> added = matchResult.getAdded();
        for (int i = 0; i < added.size(); i++) {
            EffectiveInteraction interaction = added.get(i);
            FindingImpact impact = backward ? FindingImpact.INFORMATIONAL : FindingImpact.BREAKING;
            findings.add(new CompatibilityFinding(FindingCode.OPERATION_ADDED, impact, interaction.getInteractionId(),
                    null, context.updatedLocation(interaction.getDeclarationPointer()), null, null,
                    "Operation '" + interaction.getInteractionId() + "' is newly documented"));
        }
    }

    private static void evaluateAmbiguities(InteractionMatcher.MatchResult matchResult, List<CompatibilityFinding> findings) {
        List<String> ambiguities = matchResult.getAmbiguities();
        for (int i = 0; i < ambiguities.size(); i++) {
            findings.add(new CompatibilityFinding(FindingCode.MATCH_AMBIGUOUS, FindingImpact.UNRESOLVED, null, null, null,
                    null, null, ambiguities.get(i)));
        }
    }

    private static void evaluateAddressCoverage(InteractionMatcher.MatchResult matchResult, RuleContext context,
            boolean backward, List<CompatibilityFinding> findings) {
        List<InteractionMatcher.Match> matches = matchResult.getMatched();
        for (int i = 0; i < matches.size(); i++) {
            InteractionMatcher.Match match = matches.get(i);
            EffectiveInteraction original = match.getOriginal();
            EffectiveInteraction updated = match.getUpdated();
            if (original.isWebhook() || original.getPathTemplate() == null) {
                continue;
            }
            AddressSet originalAddresses = AddressSet.of(original.getServers(), original.getPathTemplate());
            AddressSet updatedAddresses = AddressSet.of(updated.getServers(), updated.getPathTemplate());

            AddressSet obligation = backward ? originalAddresses : updatedAddresses;
            AddressSet coverer = backward ? updatedAddresses : originalAddresses;
            List<AddressSet.Address> unmatched = obligation.findUnmatched(coverer);
            for (int j = 0; j < unmatched.size(); j++) {
                AddressSet.Address address = unmatched.get(j);
                FindingImpact impact = address.isUnresolved() || address.isOpen() ? FindingImpact.UNRESOLVED : FindingImpact.BREAKING;
                findings.add(new CompatibilityFinding(FindingCode.ADDRESS_REMOVED, impact, original.getInteractionId(),
                        context.originalLocation(original.getDeclarationPointer()),
                        context.updatedLocation(updated.getDeclarationPointer()), null, null,
                        "Address '" + address.getOrigin() + address.getPathTemplate() + "' is no longer available"));
            }
        }
    }

    /**
     * Flags a newly added literal (placeholder-free) route that a still-existing
     * templated route's own pattern could already have matched: an old request
     * that was feasible under the templated route (a path parameter value equal
     * to the new literal segment) now risks being routed to the new operation
     * instead, and neither this checker nor the source documents establish
     * which one a real router would actually choose.
     */
    private static void evaluateRouteShadowing(InteractionMatcher.MatchResult matchResult, boolean backward,
            List<CompatibilityFinding> findings) {
        List<EffectiveInteraction> newInteractions = backward ? matchResult.getAdded() : matchResult.getRemoved();
        List<InteractionMatcher.Match> matches = matchResult.getMatched();
        for (int i = 0; i < newInteractions.size(); i++) {
            EffectiveInteraction added = newInteractions.get(i);
            if (added.isWebhook() || added.getPathTemplate() == null || containsPlaceholder(added.getPathTemplate())) {
                continue;
            }
            for (int j = 0; j < matches.size(); j++) {
                InteractionMatcher.Match match = matches.get(j);
                EffectiveInteraction existing = backward ? match.getOriginal() : match.getUpdated();
                if (existing.isWebhook() || existing.getPathTemplate() == null) {
                    continue;
                }
                if (!existing.getHttpMethod().equals(added.getHttpMethod())) {
                    continue;
                }
                if (!containsPlaceholder(existing.getPathTemplate())) {
                    continue;
                }
                if (couldShadow(added.getPathTemplate(), existing.getPathTemplate())) {
                    findings.add(new CompatibilityFinding(FindingCode.ROUTE_SHADOWING, FindingImpact.UNRESOLVED,
                            existing.getInteractionId(), null, null, null, null,
                            "New route '" + added.getHttpMethod() + " " + added.getPathTemplate()
                                    + "' may shadow requests previously handled by '" + existing.getHttpMethod() + " "
                                    + existing.getPathTemplate() + "'"));
                }
            }
        }
    }

    private static boolean containsPlaceholder(String pathTemplate) {
        return pathTemplate.indexOf('{') >= 0;
    }

    /**
     * True if {@code literalPath} is a value {@code placeholderPath}'s own
     * pattern could already have matched: the same number of segments, with
     * every literal segment of {@code placeholderPath} equal to the
     * corresponding segment of {@code literalPath} (a placeholder segment in
     * {@code placeholderPath} accepts any single literal segment).
     */
    static boolean couldShadow(String literalPath, String placeholderPath) {
        String[] literalSegments = literalPath.split("/", -1);
        String[] placeholderSegments = placeholderPath.split("/", -1);
        if (literalSegments.length != placeholderSegments.length) {
            return false;
        }
        for (int i = 0; i < literalSegments.length; i++) {
            String placeholderSegment = placeholderSegments[i];
            boolean isPlaceholder = placeholderSegment.length() >= 2 && placeholderSegment.charAt(0) == '{'
                    && placeholderSegment.charAt(placeholderSegment.length() - 1) == '}';
            if (!isPlaceholder && !placeholderSegment.equals(literalSegments[i])) {
                return false;
            }
        }
        return true;
    }
}
