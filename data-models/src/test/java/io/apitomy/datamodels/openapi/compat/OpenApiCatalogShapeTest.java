package io.apitomy.datamodels.openapi.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the bundled compatibility case catalog is well-formed, and
 * that malformed catalogs (missing expectations, duplicate ids, unknown
 * finding codes, an empty case list) are rejected rather than silently
 * accepted as passing or skipped tests.
 */
class OpenApiCatalogShapeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void bundledCatalogIsWellFormed() {
        JsonNode root = OpenApiCaseSupport.readCatalog();
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertTrue(problems.isEmpty(), "Catalog has shape problems: " + problems);
    }

    @Test
    void rejectsEmptyCaseList() {
        JsonNode root = parse("{\"cases\": []}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("non-empty"));
    }

    @Test
    void rejectsMissingCasesArray() {
        JsonNode root = parse("{}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("non-empty"));
    }

    @Test
    void rejectsDuplicateIds() {
        JsonNode root = parse("{\"cases\": [" + minimalCase("dup") + ", " + minimalCase("dup") + "]}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertTrue(problems.stream().anyMatch(p -> p.contains("duplicate case id: dup")));
    }

    @Test
    void rejectsMissingExpectations() {
        JsonNode root = parse("{\"cases\": [{"
                + "\"id\": \"no-expectations\", \"family\": \"3.0\","
                + "\"original\": {}, \"updated\": {}, \"reason\": \"x\","
                + "\"expected\": {}"
                + "}]}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertTrue(problems.stream().anyMatch(p -> p.contains("at least one of")));
    }

    @Test
    void rejectsUnknownFindingCodes() {
        JsonNode root = parse("{\"cases\": [{"
                + "\"id\": \"bad-code\", \"family\": \"3.0\","
                + "\"original\": {}, \"updated\": {}, \"reason\": \"x\","
                + "\"expected\": {\"backward\": {\"verdict\": \"COMPATIBLE\","
                + "\"breakingCodes\": [\"NOT_A_REAL_CODE\"], \"unresolvedCodes\": []}}"
                + "}]}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertTrue(problems.stream().anyMatch(p -> p.contains("unknown finding code")));
    }

    @Test
    void rejectsUnknownVerdict() {
        JsonNode root = parse("{\"cases\": [{"
                + "\"id\": \"bad-verdict\", \"family\": \"3.0\","
                + "\"original\": {}, \"updated\": {}, \"reason\": \"x\","
                + "\"expected\": {\"backward\": {\"verdict\": \"MAYBE\","
                + "\"breakingCodes\": [], \"unresolvedCodes\": []}}"
                + "}]}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertTrue(problems.stream().anyMatch(p -> p.contains("'verdict' must be one of")));
    }

    @Test
    void rejectsUnknownFamily() {
        JsonNode root = parse("{\"cases\": [{"
                + "\"id\": \"bad-family\", \"family\": \"4.0\","
                + "\"original\": {}, \"updated\": {}, \"reason\": \"x\","
                + "\"expected\": {\"backward\": {\"verdict\": \"COMPATIBLE\","
                + "\"breakingCodes\": [], \"unresolvedCodes\": []}}"
                + "}]}");
        List<String> problems = OpenApiCaseSupport.validateCatalog(root);
        assertTrue(problems.stream().anyMatch(p -> p.contains("'family' must be one of")));
    }

    private static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String minimalCase(String id) {
        return "{\"id\": \"" + id + "\", \"family\": \"3.0\", \"original\": {}, \"updated\": {},"
                + "\"reason\": \"x\", \"expected\": {\"backward\": {\"verdict\": \"COMPATIBLE\","
                + "\"breakingCodes\": [], \"unresolvedCodes\": []}}}";
    }
}
