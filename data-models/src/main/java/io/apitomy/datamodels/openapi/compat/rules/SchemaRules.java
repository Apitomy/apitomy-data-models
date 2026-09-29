package io.apitomy.datamodels.openapi.compat.rules;

import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentContext;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentResult;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentVerdict;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaContainment;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.ProviderRole;
import io.apitomy.datamodels.openapi.compat.SourceLocation;
import io.apitomy.datamodels.openapi.compat.contract.SchemaUsage;

/**
 * Translates the direction-neutral {@link SchemaContainment} engine into
 * usage-specific {@link CompatibilityFinding}s, without ever reversing which
 * side is "original" and which is "updated" in the reported diagnostic --
 * only the internal source/target roles fed to the containment engine change
 * with {@link io.apitomy.datamodels.openapi.compat.CheckDirection} and
 * {@link ProviderRole}.
 * <p>
 * Per the design contract's replacement-direction table: for whichever
 * document plays "old" for the check direction in effect, a
 * {@link ProviderRole#INPUT} usage requires the old schema's permitted values
 * to be a subset of the new schema's (old accepted &sube; new accepted); a
 * {@link ProviderRole#OUTPUT} usage requires the reverse (new emitted &sube;
 * old permitted). {@link CheckDirection#BACKWARD} treats the original
 * document as "old"; {@link CheckDirection#FORWARD} treats the updated
 * document as "old" -- this is the only thing direction changes here.
 */
public final class SchemaRules {

    private SchemaRules() {
    }

    /**
     * Compares one pair of corresponding schema usages (the same interaction,
     * the same {@link io.apitomy.datamodels.openapi.compat.HttpRole}/{@link ProviderRole}/location on both
     * sides), producing exactly one finding.
     */
    public static CompatibilityFinding compareUsage(SchemaUsage originalUsage, SchemaUsage updatedUsage, RuleContext context) {
        boolean backward = context.getDirection() == CheckDirection.BACKWARD;
        SchemaView oldSchema = backward ? originalUsage.getSchema() : updatedUsage.getSchema();
        SchemaView newSchema = backward ? updatedUsage.getSchema() : originalUsage.getSchema();

        SchemaView source;
        SchemaView target;
        if (originalUsage.getProviderRole() == ProviderRole.INPUT) {
            source = oldSchema;
            target = newSchema;
        } else {
            source = newSchema;
            target = oldSchema;
        }

        ContainmentContext containmentContext = ContainmentContext.of(originalUsage.getHttpRole(),
                originalUsage.getProviderRole(), context.getDirection(), context.getPolicy());
        ContainmentResult result = SchemaContainment.compare(source, target, containmentContext);
        return translate(originalUsage, updatedUsage, result, context);
    }

    private static CompatibilityFinding translate(SchemaUsage originalUsage, SchemaUsage updatedUsage,
            ContainmentResult result, RuleContext context) {
        SourceLocation originalLocation = locationOf(context, true, originalUsage);
        SourceLocation updatedLocation = locationOf(context, false, updatedUsage);

        FindingCode code;
        FindingImpact impact;
        if (result.getVerdict() == ContainmentVerdict.YES) {
            code = FindingCode.SCHEMA_COMPATIBLE;
            impact = FindingImpact.COMPATIBLE;
        } else if (result.getVerdict() == ContainmentVerdict.NO) {
            code = originalUsage.getProviderRole() == ProviderRole.INPUT ? FindingCode.SCHEMA_INPUT_NARROWED
                    : FindingCode.SCHEMA_OUTPUT_WIDENED;
            impact = FindingImpact.BREAKING;
        } else {
            code = FindingCode.SCHEMA_CONTAINMENT_INDETERMINATE;
            impact = FindingImpact.UNRESOLVED;
        }

        String message = describe(originalUsage, result);
        return new CompatibilityFinding(code, impact, originalUsage.getInteractionId(), originalLocation, updatedLocation,
                originalUsage.getHttpRole(), originalUsage.getProviderRole(), message);
    }

    private static SourceLocation locationOf(RuleContext context, boolean original, SchemaUsage usage) {
        String pointer = usage.getSchema().getPointer();
        if (pointer == null) {
            return null;
        }
        return original ? context.originalLocation(pointer) : context.updatedLocation(pointer);
    }

    private static String describe(SchemaUsage usage, ContainmentResult result) {
        StringBuilder message = new StringBuilder();
        message.append("Schema usage '").append(usage.getLocation()).append("' (");
        message.append(usage.getHttpRole()).append("/").append(usage.getProviderRole()).append(")");
        if (result.getVerdict() == ContainmentVerdict.YES) {
            message.append(" remains compatible");
        } else if (result.getVerdict() == ContainmentVerdict.NO) {
            message.append(" is no longer compatible");
        } else {
            message.append("'s compatibility could not be established");
        }
        if (!result.getEvidence().isEmpty()) {
            message.append(": ").append(result.getEvidence().get(0).getMessage());
        }
        return message.toString();
    }
}
