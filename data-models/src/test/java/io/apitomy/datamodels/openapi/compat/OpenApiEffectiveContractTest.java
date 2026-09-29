package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.openapi.compat.contract.ContractDocument;
import io.apitomy.datamodels.openapi.compat.contract.ContractInterpreter;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveParameter;
import io.apitomy.datamodels.openapi.compat.contract.SchemaUsage;

/**
 * {@code ContractInterpreter}/{@code EffectiveInteraction}/{@code SchemaUsage} (T11):
 * effective parameter inheritance and override, component extraction, absent
 * versus empty security, server precedence, 2.0 legacy consumes/produces
 * overrides, component-only documents, duplicate-declaration recognition, and
 * webhook role independence.
 */
class OpenApiEffectiveContractTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String RESOURCE_URI = "urn:test:doc";

    private static JsonNode fixtureCase(String id) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/effective-contract.json");
        assertNotNull(in, "effective-contract.json fixture not found");
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
        return ContractInterpreter.interpret((ObjectNode) documentJson, RESOURCE_URI);
    }

    private static EffectiveInteraction onlyInteraction(ContractDocument document) {
        assertEquals(1, document.getInteractions().size());
        return document.getInteractions().get(0);
    }

    private static EffectiveParameter findParameter(EffectiveInteraction interaction, String name, String in) {
        List<EffectiveParameter> parameters = interaction.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            EffectiveParameter parameter = parameters.get(i);
            if (parameter.getName().equals(name) && parameter.getIn().equals(in)) {
                return parameter;
            }
        }
        return null;
    }

    @Test
    void movingAParameterBetweenOperationAndPathLevelDoesNotChangeEffectiveBehavior() throws Exception {
        JsonNode testCase = fixtureCase("parameter-moved-operation-to-path-same-effective-behavior");
        EffectiveInteraction fromOperation = onlyInteraction(interpret(testCase.get("operationLevel")));
        EffectiveInteraction fromPath = onlyInteraction(interpret(testCase.get("pathLevel")));

        EffectiveParameter opLevel = findParameter(fromOperation, "limit", "query");
        EffectiveParameter pathLevel = findParameter(fromPath, "limit", "query");
        assertNotNull(opLevel);
        assertNotNull(pathLevel);
        assertEquals(opLevel.isRequired(), pathLevel.isRequired());
        assertEquals("integer", io.apitomy.datamodels.models.util.JsonUtil.getProperty(
                (com.fasterxml.jackson.databind.node.ObjectNode) opLevel.getSchema().getNode(), "type").asText());
        assertEquals("integer", io.apitomy.datamodels.models.util.JsonUtil.getProperty(
                (com.fasterxml.jackson.databind.node.ObjectNode) pathLevel.getSchema().getNode(), "type").asText());
    }

    @Test
    void reorderingParametersDoesNotChangeTheEffectiveSet() throws Exception {
        JsonNode testCase = fixtureCase("parameter-list-reordering-same-effective-set");
        EffectiveInteraction fromOperation = onlyInteraction(interpret(testCase.get("operationLevel")));
        EffectiveInteraction fromPath = onlyInteraction(interpret(testCase.get("pathLevel")));

        assertEquals(2, fromOperation.getParameters().size());
        assertEquals(2, fromPath.getParameters().size());
        assertNotNull(findParameter(fromOperation, "a", "query"));
        assertNotNull(findParameter(fromOperation, "b", "query"));
        assertNotNull(findParameter(fromPath, "a", "query"));
        assertNotNull(findParameter(fromPath, "b", "query"));
    }

    @Test
    void aComponentParameterReferenceResolvesLikeAnInlineDeclaration() throws Exception {
        JsonNode testCase = fixtureCase("component-extraction-parameter-ref");
        EffectiveInteraction interaction = onlyInteraction(interpret(testCase.get("document")));
        EffectiveParameter limit = findParameter(interaction, "limit", "query");
        assertNotNull(limit);
        assertTrue(limit.isRequired());
    }

    @Test
    void anOperationLevelParameterOverridesTheSameIdentityPathParameter() throws Exception {
        JsonNode testCase = fixtureCase("operation-level-override-of-path-parameter");
        EffectiveInteraction interaction = onlyInteraction(interpret(testCase.get("document")));
        EffectiveParameter limit = findParameter(interaction, "limit", "query");
        assertNotNull(limit);
        assertTrue(limit.isDeclaredAtOperationLevel());
        assertEquals("integer", io.apitomy.datamodels.models.util.JsonUtil.getProperty(
                (com.fasterxml.jackson.databind.node.ObjectNode) limit.getSchema().getNode(), "type").asText());
    }

    @Test
    void absentSecurityInheritsRootWhileAnExplicitEmptyArrayMeansNoSecurity() throws Exception {
        JsonNode testCase = fixtureCase("security-absent-inherits-root-empty-overrides-to-none");
        ContractDocument document = interpret(testCase.get("document"));

        EffectiveInteraction inherits = null;
        EffectiveInteraction overridesToNone = null;
        for (int i = 0; i < document.getInteractions().size(); i++) {
            EffectiveInteraction interaction = document.getInteractions().get(i);
            if ("/inherits".equals(interaction.getPathTemplate())) {
                inherits = interaction;
            } else if ("/overrides-to-none".equals(interaction.getPathTemplate())) {
                overridesToNone = interaction;
            }
        }
        assertNotNull(inherits);
        assertNotNull(overridesToNone);
        assertEquals(1, inherits.getSecurity().size());
        assertTrue(inherits.getSecurity().get(0).containsKey("apiKey"));
        assertEquals(0, overridesToNone.getSecurity().size());
    }

    @Test
    void serverPrecedenceIsOperationThenPathThenRoot() throws Exception {
        JsonNode testCase = fixtureCase("server-precedence-operation-then-path-then-root");
        ContractDocument document = interpret(testCase.get("document"));

        Map<String, EffectiveInteraction> byPath = new java.util.LinkedHashMap<String, EffectiveInteraction>();
        for (int i = 0; i < document.getInteractions().size(); i++) {
            EffectiveInteraction interaction = document.getInteractions().get(i);
            byPath.put(interaction.getPathTemplate(), interaction);
        }

        assertEquals(java.util.Arrays.asList("https://root.example.com"), byPath.get("/root-only").getServers());
        assertEquals(java.util.Arrays.asList("https://path.example.com"), byPath.get("/path-level").getServers());
        assertEquals(java.util.Arrays.asList("https://operation.example.com"), byPath.get("/operation-level").getServers());
    }

    @Test
    void legacyConsumesProducesOverrideAtTheOperationLevel() throws Exception {
        JsonNode testCase = fixtureCase("legacy-consumes-produces-schemes-operation-override");
        ContractDocument document = interpret(testCase.get("document"));

        EffectiveInteraction inherits = null;
        EffectiveInteraction overrides = null;
        for (int i = 0; i < document.getInteractions().size(); i++) {
            EffectiveInteraction interaction = document.getInteractions().get(i);
            if ("/inherits".equals(interaction.getPathTemplate())) {
                inherits = interaction;
            } else if ("/overrides".equals(interaction.getPathTemplate())) {
                overrides = interaction;
            }
        }
        assertNotNull(inherits);
        assertNotNull(overrides);
        assertTrue(inherits.getRequestBody().getContentByMediaType().containsKey("application/json"));
        assertFalse(inherits.getRequestBody().getContentByMediaType().containsKey("application/xml"));
        assertTrue(overrides.getRequestBody().getContentByMediaType().containsKey("application/xml"));
        assertFalse(overrides.getRequestBody().getContentByMediaType().containsKey("application/json"));
    }

    @Test
    void aComponentOnlyDocumentIsAnEmptyApiSurfaceWithACoverageNote() throws Exception {
        JsonNode testCase = fixtureCase("component-only-document-empty-surface");
        ContractDocument document = interpret(testCase.get("document"));

        assertTrue(document.isComponentOnly());
        assertTrue(document.getInteractions().isEmpty());
        assertNotNull(document.getCoverageNote());
    }

    @Test
    void aDuplicateParameterDeclarationIsRecognizedAndTheFirstWins() throws Exception {
        JsonNode testCase = fixtureCase("duplicate-parameter-declaration-recognized");
        ContractDocument document = interpret(testCase.get("document"));
        EffectiveInteraction interaction = onlyInteraction(document);

        assertFalse(document.getProblems().isEmpty());
        assertEquals(1, interaction.getParameters().size());
        EffectiveParameter id = findParameter(interaction, "id", "query");
        assertNotNull(id);
        assertEquals("string", io.apitomy.datamodels.models.util.JsonUtil.getProperty(
                (com.fasterxml.jackson.databind.node.ObjectNode) id.getSchema().getNode(), "type").asText());
    }

    @Test
    void unreferencedRootDefinitionsAreNotGloballyApplied() throws Exception {
        JsonNode testCase = fixtureCase("unreferenced-root-definitions-not-globally-applied");
        ContractDocument document = interpret(testCase.get("document"));
        EffectiveInteraction interaction = onlyInteraction(document);

        assertFalse(document.isComponentOnly());
        assertTrue(interaction.getParameters().isEmpty());
    }

    @Test
    void webhookUsagesGetOppositeProviderRolesFromOrdinaryUsagesForTheSameSchema() throws Exception {
        JsonNode testCase = fixtureCase("webhook-request-and-response-roles-independent-of-ordinary");
        ContractDocument document = interpret(testCase.get("document"));

        EffectiveInteraction ordinary = null;
        EffectiveInteraction webhook = null;
        for (int i = 0; i < document.getInteractions().size(); i++) {
            EffectiveInteraction interaction = document.getInteractions().get(i);
            if (interaction.isWebhook()) {
                webhook = interaction;
            } else {
                ordinary = interaction;
            }
        }
        assertNotNull(ordinary);
        assertNotNull(webhook);
        assertNull(ordinary.getWebhookName());
        assertEquals("widgetCreated", webhook.getWebhookName());
        assertNull(webhook.getPathTemplate());

        assertRequestResponseRoles(ordinary.getSchemaUsages(), ProviderRole.INPUT, ProviderRole.OUTPUT);
        assertRequestResponseRoles(webhook.getSchemaUsages(), ProviderRole.OUTPUT, ProviderRole.INPUT);
    }

    private static void assertRequestResponseRoles(List<SchemaUsage> usages, ProviderRole expectedRequestRole,
            ProviderRole expectedResponseRole) {
        boolean sawRequest = false;
        boolean sawResponse = false;
        for (int i = 0; i < usages.size(); i++) {
            SchemaUsage usage = usages.get(i);
            if (usage.getHttpRole() == HttpRole.REQUEST) {
                assertEquals(expectedRequestRole, usage.getProviderRole());
                sawRequest = true;
            } else {
                assertEquals(expectedResponseRole, usage.getProviderRole());
                sawResponse = true;
            }
        }
        assertTrue(sawRequest, "Expected at least one request-side usage");
        assertTrue(sawResponse, "Expected at least one response-side usage");
    }
}
