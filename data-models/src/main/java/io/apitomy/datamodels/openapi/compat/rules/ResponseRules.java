package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.HttpRole;
import io.apitomy.datamodels.openapi.compat.ProviderRole;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveParameter;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveResponse;
import io.apitomy.datamodels.openapi.compat.contract.ResponseSelector;
import io.apitomy.datamodels.openapi.compat.contract.SchemaUsage;

/**
 * Response selection, output guarantees, and retained representations for a
 * matched pair of ordinary (non-webhook) interactions.
 * <p>
 * Compares effective status <em>partitions</em> (every status code or range
 * either document mentions, resolved through {@link ResponseSelector} against
 * <em>both</em> documents) rather than a map-key diff: a status newly listed
 * explicitly but already covered by the old {@code default} is not a new
 * capability (compare its definition instead); a status previously listed
 * explicitly but now only reachable through an equivalent {@code default} is
 * a safe, redundant-branch removal, not a withdrawal, provided that
 * equivalent coverage actually holds -- an incompatible refinement under the
 * same {@code default} is still reported.
 * <p>
 * Scoped like {@link InputRules}/{@link RepresentationRules}: ordinary
 * (non-webhook) interactions only; a webhook response's provider/consumer
 * roles are reversed (T17's concern).
 */
public final class ResponseRules {

    private ResponseRules() {
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

        List<Integer> partitions = partitionsFor(original, updated);
        for (int i = 0; i < partitions.size(); i++) {
            int status = partitions.get(i);
            EffectiveResponse oldResponse = ResponseSelector.select(oldInteraction, status);
            EffectiveResponse newResponse = ResponseSelector.select(newInteraction, status);
            comparePartition(original, status, oldResponse, newResponse, context, findings);
        }
        return findings;
    }

    /** Every literal status code, plus one representative code per range (e.g. {@code "4XX"} -&gt; {@code 400}), either document's responses mention. Excludes {@code "default"}, which is not a partition of its own -- it is the fallback {@link ResponseSelector} already applies within each partition. */
    private static List<Integer> partitionsFor(EffectiveInteraction original, EffectiveInteraction updated) {
        Set<Integer> statuses = new LinkedHashSet<Integer>();
        collectStatuses(original, statuses);
        collectStatuses(updated, statuses);
        return new ArrayList<Integer>(statuses);
    }

    private static void collectStatuses(EffectiveInteraction interaction, Set<Integer> statuses) {
        List<String> keys = new ArrayList<String>(interaction.getResponses().keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if ("default".equals(key)) {
                continue;
            }
            if (key.length() == 3 && key.endsWith("XX")) {
                char first = key.charAt(0);
                if (first >= '1' && first <= '5') {
                    statuses.add((first - '0') * 100);
                }
                continue;
            }
            try {
                statuses.add(Integer.valueOf(key));
            } catch (NumberFormatException e) {
                // Not a status this checker recognizes; skip rather than guess a partition for it.
            }
        }
    }

    private static void comparePartition(EffectiveInteraction original, int status, EffectiveResponse oldResponse,
            EffectiveResponse newResponse, RuleContext context, List<CompatibilityFinding> findings) {
        if (oldResponse == null && newResponse != null) {
            findings.add(new CompatibilityFinding(FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING,
                    original.getInteractionId(), null, null, HttpRole.RESPONSE, ProviderRole.OUTPUT,
                    "Status " + status + " is newly documented with no covering old definition"));
            return;
        }
        if (oldResponse != null && newResponse == null) {
            findings.add(new CompatibilityFinding(FindingCode.RESPONSE_CAPABILITY_REMOVED, FindingImpact.BREAKING,
                    original.getInteractionId(), null, null, HttpRole.RESPONSE, ProviderRole.OUTPUT,
                    "Status " + status + " is no longer covered by any documented response"));
            return;
        }
        if (oldResponse == null) {
            return;
        }
        compareContent(original, status, oldResponse, newResponse, context, findings);
        compareHeaders(original, status, oldResponse, newResponse, context, findings);
    }

    private static void compareContent(EffectiveInteraction original, int status, EffectiveResponse oldResponse,
            EffectiveResponse newResponse, RuleContext context, List<CompatibilityFinding> findings) {
        Map<String, SchemaView> oldContent = oldResponse.getContentByMediaType();
        Map<String, SchemaView> newContent = newResponse.getContentByMediaType();
        List<String> mediaTypes = new ArrayList<String>(oldContent.keySet());
        for (String mediaType : newContent.keySet()) {
            if (!mediaTypes.contains(mediaType)) {
                mediaTypes.add(mediaType);
            }
        }
        for (int i = 0; i < mediaTypes.size(); i++) {
            String mediaType = mediaTypes.get(i);
            SchemaView oldSchema = oldContent.get(mediaType);
            SchemaView newSchema = newContent.get(mediaType);
            if (oldSchema != null && newSchema != null) {
                SchemaUsage originalUsage = new SchemaUsage(oldSchema, original.getInteractionId(), HttpRole.RESPONSE,
                        ProviderRole.OUTPUT, mediaType, "response:" + status + ":" + mediaType);
                SchemaUsage updatedUsage = new SchemaUsage(newSchema, original.getInteractionId(), HttpRole.RESPONSE,
                        ProviderRole.OUTPUT, mediaType, "response:" + status + ":" + mediaType);
                findings.add(SchemaRules.compareUsage(originalUsage, updatedUsage, context));
            } else if (oldSchema != null) {
                findings.add(new CompatibilityFinding(FindingCode.RESPONSE_MEDIA_TYPE_REMOVED, FindingImpact.BREAKING,
                        original.getInteractionId(), null, null, HttpRole.RESPONSE, ProviderRole.OUTPUT,
                        "Response media type '" + mediaType + "' for status " + status + " is no longer available"));
            } else {
                findings.add(new CompatibilityFinding(FindingCode.RESPONSE_MEDIA_TYPE_ADDED, FindingImpact.INFORMATIONAL,
                        original.getInteractionId(), null, null, HttpRole.RESPONSE, ProviderRole.OUTPUT,
                        "Response media type '" + mediaType + "' for status " + status + " was added"));
            }
        }
    }

    private static void compareHeaders(EffectiveInteraction original, int status, EffectiveResponse oldResponse,
            EffectiveResponse newResponse, RuleContext context, List<CompatibilityFinding> findings) {
        Map<String, EffectiveParameter> oldHeaders = byLowercaseName(oldResponse.getHeaders());
        Map<String, EffectiveParameter> newHeaders = byLowercaseName(newResponse.getHeaders());
        List<String> names = new ArrayList<String>(oldHeaders.keySet());
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if ("content-type".equals(name)) {
                // Content-Type is delegated to the content map, not compared as a header here.
                continue;
            }
            EffectiveParameter oldHeader = oldHeaders.get(name);
            EffectiveParameter newHeader = newHeaders.get(name);
            if (newHeader == null) {
                findings.add(new CompatibilityFinding(FindingCode.RESPONSE_HEADER_GUARANTEE_REMOVED, FindingImpact.BREAKING,
                        original.getInteractionId(), null, null, HttpRole.RESPONSE, ProviderRole.OUTPUT,
                        "Response header '" + oldHeader.getName() + "' for status " + status + " is no longer guaranteed"));
                continue;
            }
            if (oldHeader.getSchema() != null && newHeader.getSchema() != null) {
                SchemaUsage originalUsage = new SchemaUsage(oldHeader.getSchema(), original.getInteractionId(),
                        HttpRole.RESPONSE, ProviderRole.OUTPUT, null, "response:" + status + ":header:" + name);
                SchemaUsage updatedUsage = new SchemaUsage(newHeader.getSchema(), original.getInteractionId(),
                        HttpRole.RESPONSE, ProviderRole.OUTPUT, null, "response:" + status + ":header:" + name);
                findings.add(SchemaRules.compareUsage(originalUsage, updatedUsage, context));
            }
        }
    }

    private static Map<String, EffectiveParameter> byLowercaseName(List<EffectiveParameter> headers) {
        Map<String, EffectiveParameter> result = new LinkedHashMap<String, EffectiveParameter>();
        for (int i = 0; i < headers.size(); i++) {
            EffectiveParameter header = headers.get(i);
            result.put(header.getName().toLowerCase(), header);
        }
        return result;
    }
}
