package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.apitomy.datamodels.openapi.compat.rules.RoutingRules;
import io.apitomy.datamodels.openapi.compat.rules.RuleContext;

/**
 * {@code InteractionMatcher}/{@code AddressSet}/{@code RoutingRules} (T12):
 * operation removal/addition, removed server URLs, finite server-variable
 * enumeration, placeholder renaming, open-ended (default-only) server
 * variables, and concrete-route shadowing of an existing templated route.
 */
class OpenApiRoutingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/routing.json");
        assertNotNull(in, "routing.json fixture not found");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        for (JsonNode testCase : root.get("cases")) {
            if (testCase.get("id").asText().equals(id)) {
                return testCase;
            }
        }
        throw new IllegalStateException("No fixture case named '" + id + "'");
    }

    private static ContractDocument interpret(JsonNode documentJson) {
        return ContractInterpreter.interpret((ObjectNode) documentJson, ORIGINAL_URI);
    }

    private static RuleContext context(JsonNode testCase, CheckDirection direction) {
        ContractDocument original = interpret(testCase.get("original"));
        ContractDocument updated = interpret(testCase.get("updated"));
        return new RuleContext(original, updated, ORIGINAL_URI, UPDATED_URI, direction, CompatibilityPolicy.defaults());
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
    void operationRemovedIsBreakingBackwardAndOperationAddedIsBreakingForward() throws Exception {
        JsonNode testCase = fixtureCase("operation-removed-and-added");

        List<CompatibilityFinding> backward = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
        assertTrue(hasFinding(backward, FindingCode.OPERATION_REMOVED, FindingImpact.BREAKING),
                "POST /pets removed must be breaking backward");
        assertTrue(hasFinding(backward, FindingCode.OPERATION_ADDED, FindingImpact.INFORMATIONAL),
                "POST /pets/import added must be informational backward");

        List<CompatibilityFinding> forward = RoutingRules.evaluate(context(testCase, CheckDirection.FORWARD));
        assertTrue(hasFinding(forward, FindingCode.OPERATION_ADDED, FindingImpact.BREAKING),
                "POST /pets/import added must be breaking forward (original cannot serve it)");
        assertTrue(hasFinding(forward, FindingCode.OPERATION_REMOVED, FindingImpact.INFORMATIONAL),
                "POST /pets removed must be informational forward");
    }

    @Test
    void aRemovedServerUrlIsBreaking() throws Exception {
        JsonNode testCase = fixtureCase("removed-server-url");
        List<CompatibilityFinding> findings = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
        assertTrue(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.BREAKING));
    }

    @Test
    void narrowingAFiniteServerVariableEnumRemovesAnAddress() throws Exception {
        JsonNode testCase = fixtureCase("finite-variable-enum-narrowed");
        List<CompatibilityFinding> findings = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
        assertTrue(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.BREAKING),
                "The 'prod' environment combination is no longer covered");
    }

    @Test
    void placeholderRenamingIsMatchedNotReportedAsRemovedAndAdded() throws Exception {
        JsonNode testCase = fixtureCase("placeholder-renaming-still-matches");
        List<CompatibilityFinding> findings = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
        assertFalse(hasFinding(findings, FindingCode.OPERATION_REMOVED, FindingImpact.BREAKING));
        assertFalse(hasFinding(findings, FindingCode.OPERATION_ADDED, FindingImpact.INFORMATIONAL));
    }

    @Test
    void anOpenEndedServerVariableProducesUnresolvedNotBreaking() throws Exception {
        JsonNode testCase = fixtureCase("open-ended-server-variable-cannot-be-enumerated");
        List<CompatibilityFinding> findings = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
        assertTrue(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.UNRESOLVED));
        assertFalse(hasFinding(findings, FindingCode.ADDRESS_REMOVED, FindingImpact.BREAKING));
    }

    @Test
    void aNewLiteralRouteShadowingAnExistingTemplatedRouteIsUnresolved() throws Exception {
        JsonNode testCase = fixtureCase("concrete-route-shadowing");
        List<CompatibilityFinding> findings = RoutingRules.evaluate(context(testCase, CheckDirection.BACKWARD));
        assertTrue(hasFinding(findings, FindingCode.ROUTE_SHADOWING, FindingImpact.UNRESOLVED));
        assertEquals(1, countOfCode(findings, FindingCode.ROUTE_SHADOWING));
    }

    private static int countOfCode(List<CompatibilityFinding> findings, FindingCode code) {
        int count = 0;
        for (int i = 0; i < findings.size(); i++) {
            if (findings.get(i).getCode() == code) {
                count++;
            }
        }
        return count;
    }
}
