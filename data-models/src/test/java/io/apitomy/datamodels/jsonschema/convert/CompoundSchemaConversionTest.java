package io.apitomy.datamodels.jsonschema.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.io.ModelReader;
import io.apitomy.datamodels.models.io.ModelReaderFactory;
import io.apitomy.datamodels.models.io.ModelWriter;
import io.apitomy.datamodels.models.io.ModelWriterFactory;
import io.apitomy.datamodels.util.ModelTypeUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CompoundSchemaConversionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testConversionTestData() throws Exception {
        var testData = readResource("conversion-test-data.json");
        var root = MAPPER.readTree(testData);
        var tests = root.get("tests");

        var failed = new ArrayList<String>();
        var passed = 0;

        for (var testCase : tests) {
            var id = testCase.get("id").asText();
            var modelTypeStr = testCase.get("modelType").asText();
            var inputNode = testCase.get("input");
            var expectedNode = testCase.get("expected");

            try {
                ModelType modelType = ModelTypeUtil.fromString(modelTypeStr);

                // Read the input as the source schema
                ModelReader reader = ModelReaderFactory.createModelReader(modelType);
                JsonSchema source = (JsonSchema) reader.readRoot(inputNode);

                // Convert to compound
                JsonSchema compound = CompoundSchemaConverter.toCompound(source, modelType);
                Assertions.assertNotNull(compound, id + ": conversion returned null");

                // Write the compound schema back to JSON
                ModelWriter writer = ModelWriterFactory.createModelWriter(ModelType.JC);
                JsonNode actualNode = writer.writeRoot((RootCapable) compound);

                // Compare
                String expected = MAPPER.writeValueAsString(expectedNode);
                String actual = MAPPER.writeValueAsString(actualNode);

                if (expectedNode.isBoolean() || expectedNode.isValueNode()) {
                    Assertions.assertEquals(expected, actual, id);
                } else {
                    JSONAssert.assertEquals(id, expected, actual, false);
                }
                passed++;
            } catch (Throwable e) {
                failed.add(id + ": " + e.getMessage());
            }
        }

        if (!failed.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(failed.size()).append(" test(s) failed out of ").append(passed + failed.size()).append(":\n");
            failed.forEach(f -> sb.append("  - ").append(f).append("\n"));
            Assertions.fail(sb.toString());
        }
    }

    private String readResource(String name) throws IOException {
        var stream = getClass().getResourceAsStream(name);
        Assertions.assertNotNull(stream, "Resource not found: " + name);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
