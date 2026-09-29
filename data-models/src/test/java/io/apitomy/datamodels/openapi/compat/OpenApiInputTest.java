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
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;

/**
 * {@code InputRules}/{@code SchemaRules} (T13): removed/newly-required/added
 * parameters, same-name-different-location and case-only header identity,
 * narrowing enum on a retained parameter, and body presence (missing vs.
 * required-added vs. removed) independent of whether the body's own schema
 * happens to accept null or an empty object.
 */
class OpenApiInputTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/inputs.json");
        assertNotNull(in, "inputs.json fixture not found");
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
        return InputRules.compare(originalInteraction, updatedInteraction, context);
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
    void removedOptionalQueryInputIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("removed-optional-query-input");
        assertTrue(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING));
    }

    @Test
    void newlyRequiredParameterIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("newly-required-parameter");
        assertTrue(hasFinding(findings, FindingCode.PARAMETER_REQUIRED_ADDED, FindingImpact.BREAKING));
    }

    @Test
    void optionalParameterAdditionIsSafe() throws Exception {
        List<CompatibilityFinding> findings = evaluate("optional-parameter-addition-is-safe");
        assertFalse(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING));
        assertFalse(hasFinding(findings, FindingCode.PARAMETER_REQUIRED_ADDED, FindingImpact.BREAKING));
    }

    @Test
    void sameNameDifferentLocationParametersAreDistinctIdentities() throws Exception {
        List<CompatibilityFinding> findings = evaluate("same-name-different-location-are-distinct");
        // The query 'id' is retained (compatible); the new header 'id' is a safe addition.
        assertFalse(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING));
    }

    @Test
    void caseOnlyHeaderRenameIsNotARemoval() throws Exception {
        List<CompatibilityFinding> findings = evaluate("case-only-header-rename-is-not-a-removal");
        assertFalse(hasFinding(findings, FindingCode.PARAMETER_REMOVED, FindingImpact.BREAKING));
    }

    @Test
    void narrowingEnumOnARetainedParameterIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("narrowing-enum-on-retained-parameter-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.SCHEMA_INPUT_NARROWED, FindingImpact.BREAKING));
    }

    @Test
    void missingBodyToRequiredBodyIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("missing-body-vs-required-body-added");
        assertTrue(hasFinding(findings, FindingCode.REQUEST_BODY_REQUIRED_ADDED, FindingImpact.BREAKING));
    }

    @Test
    void requiredBodyStaysRequiredRegardlessOfNullOrEmptyObjectAcceptance() throws Exception {
        List<CompatibilityFinding> findings = evaluate("required-body-stays-required-even-if-schema-accepts-null-or-empty-object");
        assertTrue(hasFinding(findings, FindingCode.REQUEST_BODY_REQUIRED_ADDED, FindingImpact.BREAKING),
                "Becoming required is breaking regardless of what the new schema's value contract additionally accepts");
    }

    @Test
    void requiredBodyRemovedIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("required-body-removed");
        assertTrue(hasFinding(findings, FindingCode.REQUEST_BODY_REMOVED, FindingImpact.BREAKING));
    }
}
