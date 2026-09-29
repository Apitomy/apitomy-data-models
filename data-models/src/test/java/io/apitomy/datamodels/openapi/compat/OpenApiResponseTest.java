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
import io.apitomy.datamodels.openapi.compat.rules.ResponseRules;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;

/**
 * {@code ResponseSelector}/{@code ResponseRules} (T15): a new status with no
 * old coverage, a new status already covered by the old default, an
 * incompatible refinement discovered underneath an unchanged default, safe
 * removal of a redundant explicit entry with equivalent default coverage,
 * removed/added response media types, and a removed response header
 * guarantee.
 */
class OpenApiResponseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/responses.json");
        assertNotNull(in, "responses.json fixture not found");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        for (JsonNode testCase : root.get("cases")) {
            if (testCase.get("id").asText().equals(id)) {
                return testCase;
            }
        }
        throw new IllegalStateException("No fixture case named '" + id + "'");
    }

    private static List<CompatibilityFinding> evaluate(String caseId) throws IOException {
        JsonNode testCase = fixtureCase(caseId);
        ContractDocument original = ContractInterpreter.interpret((ObjectNode) testCase.get("original"), ORIGINAL_URI);
        ContractDocument updated = ContractInterpreter.interpret((ObjectNode) testCase.get("updated"), UPDATED_URI);
        RuleContext context = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD,
                CompatibilityPolicy.defaults());
        EffectiveInteraction originalInteraction = original.getInteractions().get(0);
        EffectiveInteraction updatedInteraction = updated.getInteractions().get(0);
        return ResponseRules.compare(originalInteraction, updatedInteraction, context);
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
    void aNewStatusWithNoOldCoverageIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("new-202-with-only-old-200-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING));
    }

    @Test
    void aNewStatusCoveredByOldDefaultIsNotReportedAsAdded() throws Exception {
        List<CompatibilityFinding> findings = evaluate("new-404-covered-by-old-default-is-not-added");
        assertFalse(hasFinding(findings, FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING));
    }

    @Test
    void anIncompatibleRefinementDiscoveredUnderTheDefaultIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("incompatible-refinement-under-default-is-breaking");
        assertFalse(hasFinding(findings, FindingCode.RESPONSE_STATUS_ADDED, FindingImpact.BREAKING));
        assertTrue(hasFinding(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING));
    }

    @Test
    void redundantExplicitEntryRemovalThroughEquivalentDefaultIsSafe() throws Exception {
        List<CompatibilityFinding> findings = evaluate("redundant-explicit-entry-removal-through-equivalent-default-is-safe");
        assertFalse(hasFinding(findings, FindingCode.RESPONSE_CAPABILITY_REMOVED, FindingImpact.BREAKING));
        assertFalse(hasFinding(findings, FindingCode.SCHEMA_OUTPUT_WIDENED, FindingImpact.BREAKING));
    }

    @Test
    void removedResponseMediaTypeIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("removed-response-media-type-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.RESPONSE_MEDIA_TYPE_REMOVED, FindingImpact.BREAKING));
    }

    @Test
    void addedResponseMediaTypeIsInformational() throws Exception {
        List<CompatibilityFinding> findings = evaluate("added-response-media-type-is-informational");
        assertTrue(hasFinding(findings, FindingCode.RESPONSE_MEDIA_TYPE_ADDED, FindingImpact.INFORMATIONAL));
    }

    @Test
    void removedResponseHeaderGuaranteeIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("removed-response-header-guarantee-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.RESPONSE_HEADER_GUARANTEE_REMOVED, FindingImpact.BREAKING));
    }
}
