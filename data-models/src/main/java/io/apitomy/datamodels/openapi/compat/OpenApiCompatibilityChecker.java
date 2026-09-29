package io.apitomy.datamodels.openapi.compat;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.openapi.compat.contract.ContractDocument;
import io.apitomy.datamodels.openapi.compat.contract.ContractInterpreter;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.InteractionMatcher;
import io.apitomy.datamodels.openapi.compat.contract.SecuritySchemeCatalog;
import io.apitomy.datamodels.openapi.compat.rules.InputRules;
import io.apitomy.datamodels.openapi.compat.rules.RelationshipRules;
import io.apitomy.datamodels.openapi.compat.rules.RepresentationRules;
import io.apitomy.datamodels.openapi.compat.rules.ResponseRules;
import io.apitomy.datamodels.openapi.compat.rules.ReverseInteractionRules;
import io.apitomy.datamodels.openapi.compat.rules.RoutingRules;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;
import io.apitomy.datamodels.openapi.compat.rules.SecurityRules;

/**
 * The public, synchronous entry point for OpenAPI compatibility checking:
 * snapshot -&gt; interpret -&gt; match -&gt; rules -&gt; aggregate, run over
 * caller-supplied model instances or JSON text with no external resource
 * acquisition (see {@link OpenApiAsyncCompatibilityChecker} for that).
 * <p>
 * Every check operates on an immediate snapshot of its inputs: the caller's
 * document is serialized to JSON once at the start of the call ({@link Library#writeDocument}),
 * and every later step reads only that captured JSON, never the caller's
 * live model object -- later mutation of the object the caller passed in
 * cannot affect an in-progress or completed check.
 * <p>
 * A full check runs both replacement directions over the very same captured
 * snapshots (not two independent re-reads), so both directions' results
 * describe the same instant.
 */
public final class OpenApiCompatibilityChecker {

    private final CompatibilityPolicy policy;

    OpenApiCompatibilityChecker(CompatibilityPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        this.policy = policy;
    }

    public static OpenApiCompatibilityCheckerBuilder builder() {
        return new OpenApiCompatibilityCheckerBuilder();
    }

    /** Can {@code updated} replace {@code original} for consumers of the original, documented contract? */
    public CompatibilityResult checkBackward(RootCapable original, RootCapable updated, CheckOptions options) {
        return check(original, updated, options, CheckDirection.BACKWARD);
    }

    /** Can {@code original} replace {@code updated} for consumers of the updated, documented contract? */
    public CompatibilityResult checkForward(RootCapable original, RootCapable updated, CheckOptions options) {
        return check(original, updated, options, CheckDirection.FORWARD);
    }

    /** Both replacement directions, evaluated over the same captured snapshots. */
    public FullCompatibilityResult checkFull(RootCapable original, RootCapable updated, CheckOptions options) {
        ObjectNode originalJson = snapshot(original);
        ObjectNode updatedJson = snapshot(updated);
        return new FullCompatibilityResult(checkJson(originalJson, updatedJson, options, CheckDirection.BACKWARD),
                checkJson(originalJson, updatedJson, options, CheckDirection.FORWARD));
    }

    /** {@link #checkBackward}, from raw JSON text. */
    public CompatibilityResult checkBackwardJson(String originalJson, String updatedJson, CheckOptions options) {
        return checkJson(parse(originalJson), parse(updatedJson), options, CheckDirection.BACKWARD);
    }

    /** {@link #checkForward}, from raw JSON text. */
    public CompatibilityResult checkForwardJson(String originalJson, String updatedJson, CheckOptions options) {
        return checkJson(parse(originalJson), parse(updatedJson), options, CheckDirection.FORWARD);
    }

    /** {@link #checkFull}, from raw JSON text. */
    public FullCompatibilityResult checkFullJson(String originalJson, String updatedJson, CheckOptions options) {
        ObjectNode originalNode = parse(originalJson);
        ObjectNode updatedNode = parse(updatedJson);
        return new FullCompatibilityResult(checkJson(originalNode, updatedNode, options, CheckDirection.BACKWARD),
                checkJson(originalNode, updatedNode, options, CheckDirection.FORWARD));
    }

    private CompatibilityResult check(RootCapable original, RootCapable updated, CheckOptions options,
            CheckDirection direction) {
        return checkJson(snapshot(original), snapshot(updated), options, direction);
    }

    private static ObjectNode snapshot(RootCapable root) {
        if (root == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        return Library.writeDocument((Document) root);
    }

    private static ObjectNode parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }
        return (ObjectNode) io.apitomy.datamodels.models.util.JsonUtil.parseJSON(json);
    }

    /** Package-visible for {@link OpenApiAsyncCompatibilityChecker}, which resolves external resources first, then delegates here with the same (now side-complete) snapshots. */
    CompatibilityResult checkJson(ObjectNode originalJson, ObjectNode updatedJson, CheckOptions options,
            CheckDirection direction) {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        RootCapable originalRoot = Library.readRoot((ObjectNode) io.apitomy.datamodels.models.util.JsonUtil.clone(originalJson));
        RootCapable updatedRoot = Library.readRoot((ObjectNode) io.apitomy.datamodels.models.util.JsonUtil.clone(updatedJson));
        requireSameFamily(originalRoot.modelType(), updatedRoot.modelType());

        ContractDocument originalContract = ContractInterpreter.interpret(originalJson, options.getOriginalUri());
        ContractDocument updatedContract = ContractInterpreter.interpret(updatedJson, options.getUpdatedUri());
        RuleContext context = new RuleContext(originalContract, updatedContract, options.getOriginalUri(),
                options.getUpdatedUri(), direction, policy);

        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();
        findings.addAll(RoutingRules.evaluate(context));
        findings.addAll(ReverseInteractionRules.evaluate(context));

        SecuritySchemeCatalog originalSchemes = SecuritySchemeCatalog.from(originalRoot);
        SecuritySchemeCatalog updatedSchemes = SecuritySchemeCatalog.from(updatedRoot);

        InteractionMatcher.MatchResult matchResult = InteractionMatcher.match(originalContract, updatedContract);
        List<InteractionMatcher.Match> matches = matchResult.getMatched();
        List<String> coverage = new ArrayList<String>();
        for (int i = 0; i < matches.size(); i++) {
            InteractionMatcher.Match match = matches.get(i);
            EffectiveInteraction originalInteraction = match.getOriginal();
            EffectiveInteraction updatedInteraction = match.getUpdated();
            coverage.add(originalInteraction.getInteractionId());
            findings.addAll(InputRules.compare(originalInteraction, updatedInteraction, context));
            findings.addAll(RepresentationRules.compare(originalInteraction, updatedInteraction, context));
            findings.addAll(ResponseRules.compare(originalInteraction, updatedInteraction, context));
            findings.addAll(SecurityRules.compare(originalInteraction, updatedInteraction, context, originalSchemes,
                    updatedSchemes));
            findings.addAll(RelationshipRules.compare(originalInteraction, updatedInteraction, context));
        }

        List<String> assumptions = new ArrayList<String>();
        assumptions.add("policy:" + policy.getId());
        assumptions.add("Added response representations are assumed compatible under content-negotiation stability.");
        return new CompatibilityResult(findings, coverage, assumptions);
    }

    private static void requireSameFamily(ModelType originalType, ModelType updatedType) {
        boolean originalIs2 = originalType == ModelType.OPENAPI20;
        boolean updatedIs2 = updatedType == ModelType.OPENAPI20;
        if (originalIs2 != updatedIs2) {
            throw new IllegalArgumentException(
                    "Cross-family comparison is not supported: " + originalType + " vs. " + updatedType);
        }
    }
}
