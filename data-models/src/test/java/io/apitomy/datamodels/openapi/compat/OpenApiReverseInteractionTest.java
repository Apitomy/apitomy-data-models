package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.openapi.compat.contract.ContractDocument;
import io.apitomy.datamodels.openapi.compat.contract.ContractInterpreter;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.rules.InputRules;
import io.apitomy.datamodels.openapi.compat.rules.ResponseRules;
import io.apitomy.datamodels.openapi.compat.rules.ReverseInteractionRules;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;

/**
 * {@code ReverseInteractionRules} (T17): the full four-row HTTP-role/
 * provider-role direction table exercised on one shared schema pair (ordinary
 * request/response, webhook request/response), a newly added webhook
 * defaulting to Indeterminate unless explicitly opted in, and a removed
 * webhook being Breaking.
 */
class OpenApiReverseInteractionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/reverse-interactions.json");
        assertNotNull(in, "reverse-interactions.json fixture not found");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        for (JsonNode testCase : root.get("cases")) {
            if (testCase.get("id").asText().equals(id)) {
                return testCase;
            }
        }
        throw new IllegalStateException("No fixture case named '" + id + "'");
    }

    private static List<CompatibilityFinding> evaluate(String caseId, CompatibilityPolicy policy) throws IOException {
        JsonNode testCase = fixtureCase(caseId);
        ContractDocument original = ContractInterpreter.interpret((ObjectNode) testCase.get("original"), ORIGINAL_URI);
        ContractDocument updated = ContractInterpreter.interpret((ObjectNode) testCase.get("updated"), UPDATED_URI);
        RuleContext context = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD, policy);
        return ReverseInteractionRules.evaluate(context);
    }

    private static boolean hasFindingWithRoles(List<CompatibilityFinding> findings, FindingCode code, FindingImpact impact,
            HttpRole httpRole, ProviderRole providerRole) {
        for (int i = 0; i < findings.size(); i++) {
            CompatibilityFinding finding = findings.get(i);
            if (finding.getCode() == code && finding.getImpact() == impact && finding.getHttpRole() == httpRole
                    && finding.getProviderRole() == providerRole) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFinding(List<CompatibilityFinding> findings, FindingCode code, FindingImpact impact) {
        for (int i = 0; i < findings.size(); i++) {
            CompatibilityFinding finding = findings.get(i);
            if (finding.getCode() == code && finding.getImpact() == impact) {
                return true;
            }
        }
        return false;
    }

    @Test
    void fullFourRowDirectionTableOnASharedNarrowedSchema() throws Exception {
        JsonNode testCase = fixtureCase("four-row-direction-table-narrowed-enum");
        ContractDocument original = ContractInterpreter.interpret((ObjectNode) testCase.get("original"), ORIGINAL_URI);
        ContractDocument updated = ContractInterpreter.interpret((ObjectNode) testCase.get("updated"), UPDATED_URI);
        RuleContext context = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD,
                CompatibilityPolicy.defaults());

        EffectiveInteraction originalOrdinary = findByWebhook(original, false);
        EffectiveInteraction updatedOrdinary = findByWebhook(updated, false);
        List<CompatibilityFinding> findings = new java.util.ArrayList<CompatibilityFinding>();
        findings.addAll(InputRules.compare(originalOrdinary, updatedOrdinary, context));
        findings.addAll(ResponseRules.compare(originalOrdinary, updatedOrdinary, context));
        findings.addAll(ReverseInteractionRules.evaluate(context));

        // Ordinary request (REQUEST/INPUT): narrowing an accepted enum is breaking.
        assertTrue(hasFindingWithRoles(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, HttpRole.REQUEST,
                ProviderRole.INPUT));
        // Ordinary response (RESPONSE/OUTPUT): narrowing emitted values is safe.
        assertFalse(hasFindingWithRoles(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING, HttpRole.RESPONSE,
                ProviderRole.OUTPUT));
        // Webhook request (REQUEST/OUTPUT, provider sends it): narrowing emitted values is safe.
        assertFalse(hasFindingWithRoles(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING, HttpRole.REQUEST,
                ProviderRole.OUTPUT));
        // Webhook response (RESPONSE/INPUT, original consumer receives it back): narrowing accepted values is breaking.
        assertTrue(hasFindingWithRoles(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING, HttpRole.RESPONSE,
                ProviderRole.INPUT));
    }

    private static EffectiveInteraction findByWebhook(ContractDocument document, boolean webhook) {
        List<EffectiveInteraction> interactions = document.getInteractions();
        for (int i = 0; i < interactions.size(); i++) {
            if (interactions.get(i).isWebhook() == webhook) {
                return interactions.get(i);
            }
        }
        throw new IllegalStateException("No " + (webhook ? "webhook" : "ordinary") + " interaction found");
    }

    @Test
    void newWebhookDefaultsToIndeterminateUnlessOptedIn() throws Exception {
        List<CompatibilityFinding> defaultFindings = evaluate("new-webhook-default-indeterminate", CompatibilityPolicy.defaults());
        assertTrue(hasFinding(defaultFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.UNRESOLVED));
        assertFalse(hasFinding(defaultFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.INFORMATIONAL));

        CompatibilityPolicy optedIn = CompatibilityPolicy.defaults().withOptedInReverseInteractionAddition("webhook POST widgetCreated");
        List<CompatibilityFinding> optedInFindings = evaluate("new-webhook-default-indeterminate", optedIn);
        assertTrue(hasFinding(optedInFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.INFORMATIONAL));
        assertFalse(hasFinding(optedInFindings, FindingCode.REVERSE_INTERACTION_ADDED, FindingImpact.UNRESOLVED));
    }

    @Test
    void removedWebhookIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("removed-webhook-is-breaking", CompatibilityPolicy.defaults());
        assertTrue(hasFinding(findings, FindingCode.REVERSE_INTERACTION_REMOVED, FindingImpact.BREAKING));
    }
}
