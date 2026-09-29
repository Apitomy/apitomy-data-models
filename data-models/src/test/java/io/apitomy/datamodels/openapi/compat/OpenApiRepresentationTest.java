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
import io.apitomy.datamodels.openapi.compat.rules.RepresentationRules;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;

/**
 * {@code RepresentationRules} (T14): omitted-vs-explicit-default style/explode
 * equivalence, a CSV-to-repeated-field query encoding change (breaking for an
 * array value), an irrelevant explode change on a scalar, a content/schema
 * serialization switch, and format changes (byte/binary breaking; two
 * unrecognized formats unresolved).
 */
class OpenApiRepresentationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/representations.json");
        assertNotNull(in, "representations.json fixture not found");
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
        return RepresentationRules.compare(originalInteraction, updatedInteraction, context);
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
    void omittedVersusExplicitDefaultStyleExplodeIsEquivalent() throws Exception {
        List<CompatibilityFinding> findings = evaluate("omitted-vs-explicit-default-style-explode-is-equivalent");
        assertFalse(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.BREAKING));
        assertFalse(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED));
    }

    @Test
    void csvToRepeatedQueryEncodingIsBreakingForAnArrayValue() throws Exception {
        List<CompatibilityFinding> findings = evaluate("csv-to-repeated-query-encoding-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.BREAKING));
    }

    @Test
    void anExplodeChangeOnAScalarIsIrrelevant() throws Exception {
        List<CompatibilityFinding> findings = evaluate("explode-change-on-scalar-is-irrelevant");
        assertFalse(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.BREAKING));
        assertFalse(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED));
    }

    @Test
    void aContentToSchemaSwitchIsUnresolved() throws Exception {
        List<CompatibilityFinding> findings = evaluate("content-to-schema-switch-is-unresolved");
        assertTrue(hasFinding(findings, FindingCode.SERIALIZATION_CHANGED, FindingImpact.UNRESOLVED));
    }

    @Test
    void byteToBinaryFormatChangeIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("byte-to-binary-format-change-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING));
    }

    @Test
    void unknownFormatPairChangeIsUnresolved() throws Exception {
        List<CompatibilityFinding> findings = evaluate("unknown-format-pair-change-is-unresolved");
        assertTrue(hasFinding(findings, FindingCode.FORMAT_RELATION_UNKNOWN, FindingImpact.UNRESOLVED));
    }
}
