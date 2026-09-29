package io.apitomy.datamodels.openapi.compat;

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
import io.apitomy.datamodels.openapi.compat.rules.RelationshipRules;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;

/**
 * {@code RelationshipRules} (T17): an {@code operationId} rename, a
 * deprecation-flag change, and a tags change are all informational
 * (METADATA_CHANGED); no metadata change produces no findings.
 */
class OpenApiRelationshipTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/relationships.json");
        assertNotNull(in, "relationships.json fixture not found");
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
        return RelationshipRules.compare(originalInteraction, updatedInteraction, context);
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
    void operationIdRenameIsInformational() throws Exception {
        List<CompatibilityFinding> findings = evaluate("operation-id-rename-is-informational");
        assertTrue(hasFinding(findings, FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL));
    }

    @Test
    void deprecationFlagChangeIsInformational() throws Exception {
        List<CompatibilityFinding> findings = evaluate("deprecation-flag-change-is-informational");
        assertTrue(hasFinding(findings, FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL));
    }

    @Test
    void tagsChangeIsInformational() throws Exception {
        List<CompatibilityFinding> findings = evaluate("tags-change-is-informational");
        assertTrue(hasFinding(findings, FindingCode.METADATA_CHANGED, FindingImpact.INFORMATIONAL));
    }

    @Test
    void noMetadataChangeProducesNoFindings() throws Exception {
        List<CompatibilityFinding> findings = evaluate("no-metadata-change-produces-no-findings");
        assertTrue(findings.isEmpty());
    }
}
