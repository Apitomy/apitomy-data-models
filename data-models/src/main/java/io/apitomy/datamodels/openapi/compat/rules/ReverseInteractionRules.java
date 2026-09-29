package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.InteractionMatcher;
import io.apitomy.datamodels.openapi.compat.contract.SchemaUsage;

/**
 * Reverse interactions (webhooks): presence and schema comparison for the
 * provider-initiated side of the contract, where {@link io.apitomy.datamodels.openapi.compat.ProviderRole}
 * is reversed from an ordinary interaction's (see {@link EffectiveInteraction#isWebhook()}
 * and the design contract's four-row HTTP-role/provider-role table).
 * <p>
 * Reuses {@link SchemaRules} directly for schema comparison -- every
 * {@link SchemaUsage} an {@link EffectiveInteraction} produces already
 * carries the correct role for its own webhook-ness (T11), so no separate
 * "reverse" schema engine is needed, only this module's presence/addition
 * policy and the wiring to call it. Capability-level parameter/body-presence
 * depth equivalent to {@link InputRules}/{@link ResponseRules} for webhooks
 * specifically is not attempted here (a documented gap, not a silent
 * assumption); only schema-usage containment is compared for a matched pair.
 */
public final class ReverseInteractionRules {

    private ReverseInteractionRules() {
    }

    public static List<CompatibilityFinding> evaluate(RuleContext context) {
        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();
        boolean backward = context.getDirection() == CheckDirection.BACKWARD;
        InteractionMatcher.MatchResult matchResult = InteractionMatcher.match(context.getOriginalDocument(),
                context.getUpdatedDocument());

        List<EffectiveInteraction> removed = matchResult.getRemoved();
        for (int i = 0; i < removed.size(); i++) {
            EffectiveInteraction interaction = removed.get(i);
            if (!interaction.isWebhook()) {
                continue;
            }
            FindingImpact impact = backward ? FindingImpact.BREAKING : FindingImpact.INFORMATIONAL;
            findings.add(new CompatibilityFinding(FindingCode.REVERSE_INTERACTION_REMOVED, impact,
                    interaction.getInteractionId(), context.originalLocation(interaction.getDeclarationPointer()), null, null,
                    null, "Documented reverse interaction '" + interaction.getInteractionId() + "' is no longer present"));
        }

        List<EffectiveInteraction> added = matchResult.getAdded();
        for (int i = 0; i < added.size(); i++) {
            EffectiveInteraction interaction = added.get(i);
            if (!interaction.isWebhook()) {
                continue;
            }
            FindingImpact impact = additionImpact(interaction, context, backward);
            findings.add(new CompatibilityFinding(FindingCode.REVERSE_INTERACTION_ADDED, impact,
                    interaction.getInteractionId(), null, context.updatedLocation(interaction.getDeclarationPointer()), null,
                    null, "Reverse interaction '" + interaction.getInteractionId() + "' is newly documented"));
        }

        List<InteractionMatcher.Match> matches = matchResult.getMatched();
        for (int i = 0; i < matches.size(); i++) {
            InteractionMatcher.Match match = matches.get(i);
            if (!match.getOriginal().isWebhook()) {
                continue;
            }
            compareSchemaUsages(match, context, findings);
        }
        return findings;
    }

    /**
     * Per the design contract, an added callback/webhook defaults to
     * Indeterminate; it is only Compatible/informational when the caller's
     * policy explicitly opts that interaction in (never inferred from the
     * schema alone), and only Breaking for the direction where the addition
     * is the side the check requires the replacement to already provide
     * (never for the direction that merely gained a new optional capability).
     */
    private static FindingImpact additionImpact(EffectiveInteraction interaction, RuleContext context, boolean backward) {
        if (!backward) {
            // Forward: the original cannot provide a reverse interaction the updated
            // document added -- its consumers would lose it on a rollback.
            return FindingImpact.BREAKING;
        }
        if (context.getPolicy().isReverseInteractionAdditionOptedIn(interaction.getInteractionId())) {
            return FindingImpact.INFORMATIONAL;
        }
        return FindingImpact.UNRESOLVED;
    }

    private static void compareSchemaUsages(InteractionMatcher.Match match, RuleContext context,
            List<CompatibilityFinding> findings) {
        Map<String, SchemaUsage> originalUsages = byLocation(match.getOriginal().getSchemaUsages());
        Map<String, SchemaUsage> updatedUsages = byLocation(match.getUpdated().getSchemaUsages());
        List<String> locations = new ArrayList<String>(originalUsages.keySet());
        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            SchemaUsage originalUsage = originalUsages.get(location);
            SchemaUsage updatedUsage = updatedUsages.get(location);
            if (updatedUsage != null) {
                findings.add(SchemaRules.compareUsage(originalUsage, updatedUsage, context));
            }
        }
    }

    private static Map<String, SchemaUsage> byLocation(List<SchemaUsage> usages) {
        Map<String, SchemaUsage> result = new LinkedHashMap<String, SchemaUsage>();
        for (int i = 0; i < usages.size(); i++) {
            SchemaUsage usage = usages.get(i);
            result.put(usage.getLocation(), usage);
        }
        return result;
    }
}
