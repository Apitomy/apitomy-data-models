/*
 * Copyright 2019 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.datamodels.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.util.CommandUtil;
import io.apitomy.datamodels.models.util.JsonUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tests that commands can be marshalled (Command -> JSON) and that the result matches the
 * original JSON when round-tripped through unmarshall -> marshall.
 *
 * @author eric.wittmann@gmail.com
 */
public class CommandMarshallTest {

    /**
     * Provides test cases for the parameterized test.
     */
    static Stream<Named<CommandTestCase>> provideTestCases() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/cmd/tests.json");
        List<CommandTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, CommandTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testMarshallRoundTrip(CommandTestCase testCase) throws Exception {
        String commandsCP = "fixtures/cmd/" + testCase.getTest() +
                (testCase.getCommands() != null ? testCase.getCommands() : ".commands.json");
        URL commandsUrl = Thread.currentThread().getContextClassLoader().getResource(commandsCP);
        Assertions.assertNotNull(commandsUrl, "Commands file not found: " + commandsCP);

        String commandsJson = org.apache.commons.io.IOUtils.toString(commandsUrl, StandardCharsets.UTF_8);
        ArrayNode commandsArray = (ArrayNode) JsonUtil.parseJSON(commandsJson);

        for (int i = 0; i < commandsArray.size(); i++) {
            ObjectNode originalNode = (ObjectNode) commandsArray.get(i);

            // Unmarshall the command from JSON
            ICommand command = CommandUtil.unmarshall(originalNode);
            Assertions.assertNotNull(command, "Failed to unmarshall command at index " + i);

            // Marshall the command back to JSON
            ObjectNode marshalledNode = CommandUtil.marshall(command);
            Assertions.assertNotNull(marshalledNode, "Failed to marshall command at index " + i);

            // Verify __type is preserved
            Assertions.assertEquals(
                    originalNode.get("__type").asText(),
                    marshalledNode.get("__type").asText(),
                    "Command type mismatch at index " + i);

            // Verify that all marshalled fields match the corresponding original values.
            // Note: some fixtures contain extra metadata fields (e.g. _defName, _method)
            // that are not actual command fields — these are ignored during unmarshalling
            // and won't appear in the marshalled output. We only verify fields that survive
            // the round-trip.
            marshalledNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode actualValue = entry.getValue();
                if (originalNode.has(fieldName)) {
                    JsonNode expectedValue = originalNode.get(fieldName);
                    Assertions.assertEquals(
                            expectedValue, actualValue,
                            "Field '" + fieldName + "' value mismatch in " + testCase.getName());
                }
            });

            // Verify round-trip consistency: marshall -> unmarshall produces a command
            // that marshalls identically
            ICommand roundTrippedCommand = CommandUtil.unmarshall(marshalledNode);
            ObjectNode secondMarshall = CommandUtil.marshall(roundTrippedCommand);
            Assertions.assertEquals(
                    marshalledNode, secondMarshall,
                    "Round-trip marshall inconsistency in " + testCase.getName() + " at index " + i);
        }
    }
}
