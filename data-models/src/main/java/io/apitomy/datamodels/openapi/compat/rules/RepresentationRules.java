package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.jsonschema.compat.containment.FormatRegistry;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.HttpRole;
import io.apitomy.datamodels.openapi.compat.ProviderRole;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveParameter;

/**
 * Wire representation and serialization compatibility for a matched pair of
 * ordinary (non-webhook) interactions' parameters: {@code style}/{@code explode}
 * changes that actually alter the wire encoding of an array- or object-shaped
 * value, a switch between schema-based and content-based serialization, and
 * a changed {@code format} whose representation is known to be incompatible
 * or unresolved (validation-level containment does not read {@code format}
 * at all -- it is a representation annotation, not an assertion -- so this is
 * the only layer that catches, for example, a {@code byte} <-> {@code binary}
 * change).
 * <p>
 * Scoped like {@link InputRules}: ordinary (non-webhook) request parameters
 * only in this task; response representation availability is T15's concern
 * (alongside response selection), and XML/multipart/streaming encoding depth
 * beyond a schema-shape check is intentionally not attempted here -- an
 * ambiguous-shape {@code style}/{@code explode} change is reported Unresolved,
 * never guessed at either way.
 */
public final class RepresentationRules {

    private RepresentationRules() {
    }

    public static List<CompatibilityFinding> compare(EffectiveInteraction original, EffectiveInteraction updated,
            RuleContext context) {
        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();
        if (original.isWebhook() || updated.isWebhook()) {
            return findings;
        }
        Map<String, EffectiveParameter> originalByIdentity = byIdentity(original.getParameters());
        Map<String, EffectiveParameter> updatedByIdentity = byIdentity(updated.getParameters());

        List<String> identities = new ArrayList<String>(originalByIdentity.keySet());
        for (int i = 0; i < identities.size(); i++) {
            String identity = identities.get(i);
            EffectiveParameter originalParameter = originalByIdentity.get(identity);
            EffectiveParameter updatedParameter = updatedByIdentity.get(identity);
            if (updatedParameter == null) {
                continue;
            }
            compareContentSchemaSwitch(original, identity, originalParameter, updatedParameter, findings);
            compareStyleExplode(original, identity, originalParameter, updatedParameter, findings);
            compareFormat(original, identity, originalParameter.getSchema(), updatedParameter.getSchema(), findings);
        }
        return findings;
    }

    private static Map<String, EffectiveParameter> byIdentity(List<EffectiveParameter> parameters) {
        Map<String, EffectiveParameter> result = new LinkedHashMap<String, EffectiveParameter>();
        for (int i = 0; i < parameters.size(); i++) {
            EffectiveParameter parameter = parameters.get(i);
            result.put(InputRules.identity(parameter.getIn(), parameter.getName()), parameter);
        }
        return result;
    }

    private static void compareContentSchemaSwitch(EffectiveInteraction original, String identity,
            EffectiveParameter originalParameter, EffectiveParameter updatedParameter, List<CompatibilityFinding> findings) {
        boolean originalUsesContent = originalParameter.getSchema() == null && originalParameter.getMediaType() != null;
        boolean updatedUsesContent = updatedParameter.getSchema() == null && updatedParameter.getMediaType() != null;
        if (originalUsesContent != updatedUsesContent) {
            findings.add(new CompatibilityFinding(FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED,
                    original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                    "Parameter '" + identity + "' switched between schema-based and content-based serialization"));
        }
    }

    private static void compareStyleExplode(EffectiveInteraction original, String identity,
            EffectiveParameter originalParameter, EffectiveParameter updatedParameter, List<CompatibilityFinding> findings) {
        boolean styleChanged = !equalsNullable(originalParameter.getStyle(), updatedParameter.getStyle());
        boolean explodeChanged = originalParameter.isExplode() != updatedParameter.isExplode();
        if (!styleChanged && !explodeChanged) {
            return;
        }
        String originalShape = shapeOf(originalParameter.getSchema());
        String updatedShape = shapeOf(updatedParameter.getSchema());
        if ("scalar".equals(originalShape) && "scalar".equals(updatedShape)) {
            // A scalar has nothing to explode; a style/explode difference has no wire effect.
            return;
        }
        FindingImpact impact = "unknown".equals(originalShape) || "unknown".equals(updatedShape) ? FindingImpact.UNRESOLVED
                : FindingImpact.BREAKING;
        findings.add(new CompatibilityFinding(FindingCode.SERIALIZATION_CHANGED, impact, original.getInteractionId(), null,
                null, HttpRole.REQUEST, ProviderRole.INPUT,
                "Parameter '" + identity + "' serialization changed (style/explode), which changes its wire encoding for a "
                        + (originalShape.equals(updatedShape) ? originalShape : originalShape + "-to-" + updatedShape) + " value"));
    }

    /** {@code "array"}, {@code "object"}, {@code "scalar"}, or {@code "unknown"} when the effective type cannot be pinned down to exactly one of those. */
    private static String shapeOf(SchemaView schema) {
        if (schema == null || !schema.isObject()) {
            return "unknown";
        }
        JsonNode typeNode = schema.getKeyword("type");
        if (typeNode == null) {
            return "unknown";
        }
        if (JsonUtil.isArray(typeNode)) {
            List<JsonNode> types = JsonUtil.toList(typeNode);
            if (types.size() != 1) {
                return "unknown";
            }
            return shapeOfTypeName(JsonUtil.toString(types.get(0)));
        }
        return shapeOfTypeName(JsonUtil.toString(typeNode));
    }

    private static String shapeOfTypeName(String type) {
        if ("array".equals(type)) {
            return "array";
        }
        if ("object".equals(type)) {
            return "object";
        }
        return "scalar";
    }

    private static void compareFormat(EffectiveInteraction original, String identity, SchemaView originalSchema,
            SchemaView updatedSchema, List<CompatibilityFinding> findings) {
        if (originalSchema == null || updatedSchema == null) {
            return;
        }
        String originalFormat = textKeyword(originalSchema, "format");
        String updatedFormat = textKeyword(updatedSchema, "format");
        if (equalsNullable(originalFormat, updatedFormat)) {
            return;
        }
        FormatRegistry.FormatRelation relation = FormatRegistry.relate(originalFormat, updatedFormat);
        if (relation == FormatRegistry.FormatRelation.INCOMPATIBLE) {
            findings.add(new CompatibilityFinding(FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING,
                    original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                    "Parameter '" + identity + "' format changed from '" + originalFormat + "' to '" + updatedFormat
                            + "', which is a known-incompatible representation change"));
        } else if (relation == FormatRegistry.FormatRelation.UNKNOWN) {
            findings.add(new CompatibilityFinding(FindingCode.FORMAT_RELATION_UNKNOWN, FindingImpact.UNRESOLVED,
                    original.getInteractionId(), null, null, HttpRole.REQUEST, ProviderRole.INPUT,
                    "Parameter '" + identity + "' format changed from '" + originalFormat + "' to '" + updatedFormat
                            + "', and their relationship is not established"));
        }
    }

    private static String textKeyword(SchemaView schema, String keyword) {
        JsonNode node = schema.getKeyword(keyword);
        if (node == null) {
            return null;
        }
        return JsonUtil.toString(node);
    }

    private static boolean equalsNullable(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
