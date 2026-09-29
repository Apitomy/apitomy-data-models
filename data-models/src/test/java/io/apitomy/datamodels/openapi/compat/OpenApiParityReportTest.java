package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;

/**
 * Produces the canonical Java compatibility-check report for the shared
 * cross-runtime parity corpus ({@code fixtures/openapi-compat/parity.json}),
 * written to {@code target/openapi-compat-java-results.json} (V5). The
 * TypeScript parity test ({@code openapi-compat-parity.test.ts}) reads this
 * file, re-runs the same corpus through the TypeScript checker, and asserts
 * the two runtimes agree.
 * <p>
 * Canonical serialization includes the verdict and every finding's code,
 * impact, interaction id, HTTP/provider role, and message -- deliberately
 * excluding platform stack traces or internal identity hashes, and relying
 * on {@link CompatibilityResult}'s own deterministic finding order rather
 * than re-sorting here.
 */
class OpenApiParityReportTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void writesTheCanonicalReportForTheSharedCorpus() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("fixtures/openapi-compat/parity.json");
        assertNotNull(in, "parity.json fixture not found");
        JsonNode fixture = MAPPER.readTree(in);
        in.close();

        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        ArrayNode casesOut = MAPPER.createArrayNode();
        for (JsonNode testCase : fixture.get("cases")) {
            String id = testCase.get("id").asText();
            String original = testCase.get("original").asText();
            String updated = testCase.get("updated").asText();

            CompatibilityResult backward = checker.checkBackwardJson(original, updated, CheckOptions.defaults());
            CompatibilityResult forward = checker.checkForwardJson(original, updated, CheckOptions.defaults());

            ObjectNode caseOut = MAPPER.createObjectNode();
            caseOut.put("id", id);
            caseOut.set("backward", toJson(backward));
            caseOut.set("forward", toJson(forward));
            casesOut.add(caseOut);
        }

        ObjectNode report = MAPPER.createObjectNode();
        report.set("cases", casesOut);

        File outFile = new File("target/openapi-compat-java-results.json");
        outFile.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(outFile);
        writer.write(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report));
        writer.close();
    }

    private static ObjectNode toJson(CompatibilityResult result) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("verdict", result.getVerdict().name());
        ArrayNode findings = MAPPER.createArrayNode();
        List<CompatibilityFinding> findingList = result.getFindings();
        for (int i = 0; i < findingList.size(); i++) {
            CompatibilityFinding finding = findingList.get(i);
            ObjectNode findingOut = MAPPER.createObjectNode();
            findingOut.put("code", finding.getCode().name());
            findingOut.put("impact", finding.getImpact().name());
            findingOut.put("interactionId", finding.getInteractionId());
            findingOut.put("httpRole", finding.getHttpRole() == null ? null : finding.getHttpRole().name());
            findingOut.put("providerRole", finding.getProviderRole() == null ? null : finding.getProviderRole().name());
            findings.add(findingOut);
        }
        node.set("findings", findings);
        return node;
    }
}
