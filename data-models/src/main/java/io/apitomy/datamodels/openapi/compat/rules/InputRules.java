package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.HttpRole;
import io.apitomy.datamodels.openapi.compat.ProviderRole;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveParameter;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveRequestBody;
import io.apitomy.datamodels.openapi.compat.contract.SchemaUsage;

/**
 * Input capabilities, parameter values, and body presence for one matched
 * pair of ordinary (non-webhook) interactions: whether documented parameters
 * and request body presence are still guaranteed, and whether retained
 * parameter/body schemas still accept what old requests relied on.
 * <p>
 * Scoped to ordinary interactions: a webhook's "request" is provider output
 * (the receiver, not the caller, supplies the effective role for it), and its
 * capability-presence semantics are reversed from the rules implemented here
 * -- that reversal is T17's "reverse interactions" concern, not this one's.
 * {@link #compare} returns no findings for a webhook interaction.
 * <p>
 * Direction-aware like {@link RoutingRules}: {@link CheckDirection#BACKWARD}
 * treats the original document's requests as "old" (what existing consumers
 * already send); {@link CheckDirection#FORWARD} treats the updated
 * document's as "old". A capability an old request relied on but a new
 * request cannot supply is breaking; a capability only a new request adds is
 * safe by itself (its value contract, if retained, is still schema-checked).
 */
public final class InputRules {

    private InputRules() {
    }

    public static List<CompatibilityFinding> compare(EffectiveInteraction original, EffectiveInteraction updated,
            RuleContext context) {
        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();
        if (original.isWebhook() || updated.isWebhook()) {
            return findings;
        }
        boolean backward = context.getDirection() == CheckDirection.BACKWARD;
        EffectiveInteraction oldInteraction = backward ? original : updated;
        EffectiveInteraction newInteraction = backward ? updated : original;

        compareParameters(original, updated, oldInteraction, newInteraction, context, findings);
        compareRequestBody(original, updated, oldInteraction, newInteraction, context, findings);
        return findings;
    }

    private static void compareParameters(EffectiveInteraction original, EffectiveInteraction updated,
            EffectiveInteraction oldInteraction, EffectiveInteraction newInteraction, RuleContext context,
            List<CompatibilityFinding> findings) {
        Map<String, EffectiveParameter> oldByIdentity = byIdentity(oldInteraction.getParameters());
        Map<String, EffectiveParameter> newByIdentity = byIdentity(newInteraction.getParameters());
        Map<String, EffectiveParameter> originalByIdentity = byIdentity(original.getParameters());
        Map<String, EffectiveParameter> updatedByIdentity = byIdentity(updated.getParameters());

        List<String> identities = new ArrayList<String>();
        identities.addAll(oldByIdentity.keySet());
        for (String identity : newByIdentity.keySet()) {
            if (!identities.contains(identity)) {
                identities.add(identity);
            }
        }

        for (int i = 0; i < identities.size(); i++) {
            String identity = identities.get(i);
            EffectiveParameter oldParameter = oldByIdentity.get(identity);
            EffectiveParameter newParameter = newByIdentity.get(identity);
            EffectiveParameter originalParameter = originalByIdentity.get(identity);
            EffectiveParameter updatedParameter = updatedByIdentity.get(identity);

            if (oldParameter != null && newParameter == null) {
                findings.add(new CompatibilityFinding(FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING,
                        original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                        "Documented parameter '" + identity + "' is no longer present"));
                continue;
            }
            if (oldParameter == null && newParameter != null) {
                if (newParameter.isRequired()) {
                    findings.add(new CompatibilityFinding(FindingCode.PARAMETER_REQUIRED_ADDED, FindingImpact.BREAKING,
                            original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                            "Parameter '" + identity + "' is newly required, but old requests do not supply it"));
                }
                continue;
            }
            if (!oldParameter.isRequired() && newParameter.isRequired()) {
                findings.add(new CompatibilityFinding(FindingCode.PARAMETER_REQUIRED_ADDED, FindingImpact.BREAKING,
                        original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                        "Parameter '" + identity + "' became required without an equivalent old guarantee"));
                continue;
            }
            if (originalParameter != null && updatedParameter != null) {
                addSchemaFindingIfPresent(original, updated, originalParameter.getSchema(), updatedParameter.getSchema(),
                        "parameter:" + identity, context, findings);
            }
        }
    }

    private static Map<String, EffectiveParameter> byIdentity(List<EffectiveParameter> parameters) {
        Map<String, EffectiveParameter> result = new LinkedHashMap<String, EffectiveParameter>();
        for (int i = 0; i < parameters.size(); i++) {
            EffectiveParameter parameter = parameters.get(i);
            result.put(identity(parameter.getIn(), parameter.getName()), parameter);
        }
        return result;
    }

    /** Parameter identity for matching: {@code in} plus name, header names case-folded (HTTP headers are case-insensitive); every other location compared exactly. */
    static String identity(String in, String name) {
        String normalized = "header".equals(in) ? name.toLowerCase() : name;
        return in + ":" + normalized;
    }

    private static void compareRequestBody(EffectiveInteraction original, EffectiveInteraction updated,
            EffectiveInteraction oldInteraction, EffectiveInteraction newInteraction, RuleContext context,
            List<CompatibilityFinding> findings) {
        EffectiveRequestBody oldBody = oldInteraction.getRequestBody();
        EffectiveRequestBody newBody = newInteraction.getRequestBody();

        if (oldBody != null && oldBody.isRequired() && newBody == null) {
            findings.add(new CompatibilityFinding(FindingCode.REQUEST_BODY_REMOVED, FindingImpact.BREAKING,
                    original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                    "A required request body is no longer documented"));
            return;
        }
        if (oldBody == null && newBody != null && newBody.isRequired()) {
            findings.add(new CompatibilityFinding(FindingCode.REQUEST_BODY_REQUIRED_ADDED, FindingImpact.BREAKING,
                    original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                    "A request body is now required, but old requests do not supply one"));
            return;
        }
        if (oldBody != null && newBody != null) {
            // Requiredness is a presence obligation independent of whether the body's
            // schema happens to accept null or an empty object -- a schema that
            // tolerates an empty payload does not make sending one optional.
            if (!oldBody.isRequired() && newBody.isRequired()) {
                findings.add(new CompatibilityFinding(FindingCode.REQUEST_BODY_REQUIRED_ADDED, FindingImpact.BREAKING,
                        original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                        "A request body became required without an equivalent old guarantee"));
            }
            Map<String, SchemaView> originalContent = original.getRequestBody() == null ? new LinkedHashMap<String, SchemaView>()
                    : original.getRequestBody().getContentByMediaType();
            Map<String, SchemaView> updatedContent = updated.getRequestBody() == null ? new LinkedHashMap<String, SchemaView>()
                    : updated.getRequestBody().getContentByMediaType();
            List<String> mediaTypes = new ArrayList<String>(originalContent.keySet());
            for (String mediaType : updatedContent.keySet()) {
                if (!mediaTypes.contains(mediaType)) {
                    mediaTypes.add(mediaType);
                }
            }
            for (int i = 0; i < mediaTypes.size(); i++) {
                String mediaType = mediaTypes.get(i);
                SchemaView originalSchema = originalContent.get(mediaType);
                SchemaView updatedSchema = updatedContent.get(mediaType);
                if (originalSchema != null && updatedSchema != null) {
                    addSchemaFindingIfPresent(original, updated, originalSchema, updatedSchema,
                            "requestBody:" + mediaType, context, findings);
                } else if (originalSchema != null) {
                    findings.add(new CompatibilityFinding(FindingCode.REQUEST_MEDIA_TYPE_REMOVED, FindingImpact.BREAKING,
                            original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                            "Request media type '" + mediaType + "' is no longer accepted"));
                }
            }
        }
    }

    private static void addSchemaFindingIfPresent(EffectiveInteraction original, EffectiveInteraction updated,
            SchemaView originalSchema, SchemaView updatedSchema, String location, RuleContext context,
            List<CompatibilityFinding> findings) {
        if (originalSchema == null || updatedSchema == null) {
            return;
        }
        SchemaUsage originalUsage = new SchemaUsage(originalSchema, original.getInteractionId(), HttpRole.REQUEST,
                ProviderRole.INPUT, null, location);
        SchemaUsage updatedUsage = new SchemaUsage(updatedSchema, updated.getInteractionId(), HttpRole.REQUEST,
                ProviderRole.INPUT, null, location);
        findings.add(SchemaRules.compareUsage(originalUsage, updatedUsage, context));
    }
}
