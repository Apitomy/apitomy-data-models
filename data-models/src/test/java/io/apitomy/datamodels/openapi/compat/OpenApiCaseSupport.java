package io.apitomy.datamodels.openapi.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared helpers for reading and validating the OpenAPI compatibility case
 * catalog ({@code fixtures/openapi-compat/cases.json}) used by both the Java
 * and TypeScript test suites.
 * <p>
 * The catalog is the single source of truth for expected verdicts: expectations
 * are written independently of any checker implementation, so a case that
 * regresses (either into a false pass or a spurious failure) is caught here
 * rather than silently accepted because the checker happens to agree with
 * itself.
 * <p>
 * Until a public checker exists (see the OpenAPI compatibility implementation
 * plan, task T18), {@link #assertCase(String)} is not yet defined: dispatching
 * a case to the checker requires a checker to dispatch to. Individual rule
 * tasks add focused unit coverage in the meantime; {@code assertCase} is
 * introduced alongside the first connected rule runner.
 */
final class OpenApiCaseSupport {

    private static final String CATALOG_RESOURCE = "fixtures/openapi-compat/cases.json";
    private static final Set<String> KNOWN_FAMILIES = knownFamilies();
    private static final Set<String> KNOWN_VERDICTS = knownVerdicts();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OpenApiCaseSupport() {
    }

    private static Set<String> knownFamilies() {
        Set<String> families = new HashSet<>();
        families.add("2.0");
        families.add("3.0");
        families.add("3.1");
        families.add("3.2");
        return families;
    }

    private static Set<String> knownVerdicts() {
        Set<String> verdicts = new HashSet<>();
        for (CompatibilityVerdict verdict : CompatibilityVerdict.values()) {
            verdicts.add(verdict.name());
        }
        return verdicts;
    }

    /** Reads and parses the catalog from the test classpath. */
    static JsonNode readCatalog() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(CATALOG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Catalog resource not found: " + CATALOG_RESOURCE);
            }
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read catalog: " + CATALOG_RESOURCE, e);
        }
    }

    /**
     * Validates the catalog's shape: a non-empty, duplicate-free {@code cases}
     * array where every case has an expectation for at least one direction,
     * and every expectation names only real {@link FindingCode} constants and
     * a known verdict. Returns the list of problems found (empty if the
     * catalog is well-formed).
     */
    static List<String> validateCatalog(JsonNode root) {
        List<String> problems = new ArrayList<>();
        JsonNode cases = root.get("cases");
        if (cases == null || !cases.isArray() || cases.isEmpty()) {
            problems.add("catalog must contain a non-empty 'cases' array");
            return problems;
        }

        Set<String> seenIds = new HashSet<>();
        for (JsonNode testCase : cases) {
            String id = textOrNull(testCase, "id");
            if (id == null || id.isBlank()) {
                problems.add("case is missing a non-blank 'id'");
                continue;
            }
            if (!seenIds.add(id)) {
                problems.add("duplicate case id: " + id);
            }
            validateCase(id, testCase, problems);
        }
        return problems;
    }

    private static void validateCase(String id, JsonNode testCase, List<String> problems) {
        String family = textOrNull(testCase, "family");
        if (family == null || !KNOWN_FAMILIES.contains(family)) {
            problems.add(id + ": 'family' must be one of " + KNOWN_FAMILIES + ", was: " + family);
        }
        if (testCase.get("original") == null || !testCase.get("original").isObject()) {
            problems.add(id + ": 'original' must be a JSON object");
        }
        if (testCase.get("updated") == null || !testCase.get("updated").isObject()) {
            problems.add(id + ": 'updated' must be a JSON object");
        }
        String reason = textOrNull(testCase, "reason");
        if (reason == null || reason.isBlank()) {
            problems.add(id + ": 'reason' must be a non-blank explanation");
        }

        JsonNode expected = testCase.get("expected");
        if (expected == null || !expected.isObject()) {
            problems.add(id + ": 'expected' must be an object");
            return;
        }
        JsonNode backward = expected.get("backward");
        JsonNode forward = expected.get("forward");
        if (backward == null && forward == null) {
            problems.add(id + ": 'expected' must include at least one of 'backward'/'forward'");
        }
        if (backward != null) {
            validateDirection(id, "backward", backward, problems);
        }
        if (forward != null) {
            validateDirection(id, "forward", forward, problems);
        }
    }

    private static void validateDirection(String id, String direction, JsonNode dirNode,
            List<String> problems) {
        String verdict = textOrNull(dirNode, "verdict");
        if (verdict == null || !KNOWN_VERDICTS.contains(verdict)) {
            problems.add(id + " (" + direction + "): 'verdict' must be one of " + KNOWN_VERDICTS
                    + ", was: " + verdict);
        }
        validateCodeList(id, direction, "breakingCodes", dirNode.get("breakingCodes"), problems);
        validateCodeList(id, direction, "unresolvedCodes", dirNode.get("unresolvedCodes"), problems);
    }

    private static void validateCodeList(String id, String direction, String field, JsonNode list,
            List<String> problems) {
        if (list == null) {
            problems.add(id + " (" + direction + "): missing '" + field
                    + "' (use an empty array for none)");
            return;
        }
        if (!list.isArray()) {
            problems.add(id + " (" + direction + "): '" + field + "' must be an array");
            return;
        }
        for (JsonNode codeNode : list) {
            String code = codeNode.asText();
            try {
                FindingCode.valueOf(code);
            } catch (IllegalArgumentException e) {
                problems.add(id + " (" + direction + "): unknown finding code in '" + field + "': " + code);
            }
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual()) {
            return null;
        }
        return value.asText();
    }
}
