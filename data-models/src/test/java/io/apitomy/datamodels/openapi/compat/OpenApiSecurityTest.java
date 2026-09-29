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

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.openapi.compat.contract.ContractDocument;
import io.apitomy.datamodels.openapi.compat.contract.ContractInterpreter;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.SecuritySchemeCatalog;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;
import io.apitomy.datamodels.openapi.compat.rules.SecurityRules;

/**
 * {@code SecurityRules} (T16): OR alternatives, redundant alternative
 * removal, required-scope widening, anonymous access removal, scheme
 * renaming resolved by semantic identity (not local component name),
 * case-insensitive HTTP scheme comparison, an AND requirement demanding an
 * extra scheme, and a withdrawn OAuth2 credential-acquisition flow.
 */
class OpenApiSecurityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/security.json");
        assertNotNull(in, "security.json fixture not found");
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
        ObjectNode originalJson = (ObjectNode) testCase.get("original");
        ObjectNode updatedJson = (ObjectNode) testCase.get("updated");

        ContractDocument original = ContractInterpreter.interpret(originalJson, ORIGINAL_URI);
        ContractDocument updated = ContractInterpreter.interpret(updatedJson, UPDATED_URI);
        RootCapable originalRoot = Library.readRoot((ObjectNode) JsonUtil.clone(originalJson));
        RootCapable updatedRoot = Library.readRoot((ObjectNode) JsonUtil.clone(updatedJson));
        SecuritySchemeCatalog originalSchemes = SecuritySchemeCatalog.from(originalRoot);
        SecuritySchemeCatalog updatedSchemes = SecuritySchemeCatalog.from(updatedRoot);

        RuleContext context = new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, CheckDirection.BACKWARD,
                CompatibilityPolicy.defaults());
        EffectiveInteraction originalInteraction = original.getInteractions().get(0);
        EffectiveInteraction updatedInteraction = updated.getInteractions().get(0);
        return SecurityRules.compare(originalInteraction, updatedInteraction, context, originalSchemes, updatedSchemes);
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
    void orAlternativesAllRetainedIsCompatible() throws Exception {
        List<CompatibilityFinding> findings = evaluate("or-alternatives-retained");
        assertTrue(findings.isEmpty());
    }

    @Test
    void redundantAlternativeRemovalIsSafe() throws Exception {
        List<CompatibilityFinding> findings = evaluate("redundant-alternative-removed-is-safe");
        assertFalse(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING));
    }

    @Test
    void requiredScopesWidenedIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("required-scopes-widened-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING));
    }

    @Test
    void anonymousAccessRemovedIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("anonymous-access-removed-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING));
    }

    @Test
    void schemeRenamedWithUpdatedReferenceIsCompatible() throws Exception {
        List<CompatibilityFinding> findings = evaluate("scheme-renamed-with-updated-reference-is-compatible");
        assertTrue(findings.isEmpty());
    }

    @Test
    void httpSchemeNameIsComparedCaseInsensitively() throws Exception {
        List<CompatibilityFinding> findings = evaluate("http-scheme-name-compared-case-insensitively");
        assertTrue(findings.isEmpty());
    }

    @Test
    void anAndRequirementDemandingAnExtraSchemeIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("and-schemes-requiring-an-extra-scheme-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.SECURITY_REQUIREMENT_STRENGTHENED, FindingImpact.BREAKING));
    }

    @Test
    void aWithdrawnCredentialFlowIsBreaking() throws Exception {
        List<CompatibilityFinding> findings = evaluate("credential-flow-removed-is-breaking");
        assertTrue(hasFinding(findings, FindingCode.CREDENTIAL_FLOW_REMOVED, FindingImpact.BREAKING));
    }
}
