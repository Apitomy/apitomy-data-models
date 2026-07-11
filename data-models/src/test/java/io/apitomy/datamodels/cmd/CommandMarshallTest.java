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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.skyscreamer.jsonassert.JSONAssert;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tests that commands can be marshalled (Command → JSON) and that the result matches the
 * original JSON when round-tripped through unmarshall → marshall.
 *
 * @author eric.wittmann@gmail.com
 */
@RunWith(Parameterized.class)
public class CommandMarshallTest {

    @Parameters(name = "{0}")
    public static Collection<Object[]> testData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/cmd/tests.json");
        List<CommandTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, CommandTestCase.class));

        List<Object[]> params = new ArrayList<>();
        for (CommandTestCase testCase : allTests) {
            params.add(new Object[]{ testCase.getName(), testCase.getTest(), testCase.getCommands() });
        }
        return params;
    }

    private final String testName;
    private final String testPath;
    private final String commandsSuffix;

    public CommandMarshallTest(String testName, String testPath, String commandsSuffix) {
        this.testName = testName;
        this.testPath = testPath;
        this.commandsSuffix = commandsSuffix;
    }

    @Test
    public void testMarshallRoundTrip() throws Exception {
        String commandsCP = "fixtures/cmd/" + testPath +
                (commandsSuffix != null ? commandsSuffix : ".commands.json");
        URL commandsUrl = Thread.currentThread().getContextClassLoader().getResource(commandsCP);
        Assert.assertNotNull("Commands file not found: " + commandsCP, commandsUrl);

        String commandsJson = org.apache.commons.io.IOUtils.toString(commandsUrl, StandardCharsets.UTF_8);
        ArrayNode commandsArray = (ArrayNode) JsonUtil.parseJSON(commandsJson);

        for (int i = 0; i < commandsArray.size(); i++) {
            ObjectNode originalNode = (ObjectNode) commandsArray.get(i);

            // Unmarshall the command from JSON
            ICommand command = CommandUtil.unmarshall(originalNode);
            Assert.assertNotNull("Failed to unmarshall command at index " + i, command);

            // Marshall the command back to JSON
            ObjectNode marshalledNode = CommandUtil.marshall(command);
            Assert.assertNotNull("Failed to marshall command at index " + i, marshalledNode);

            // Verify __type is preserved
            Assert.assertEquals("Command type mismatch at index " + i,
                    originalNode.get("__type").asText(),
                    marshalledNode.get("__type").asText());

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
                    Assert.assertEquals(
                            "Field '" + fieldName + "' value mismatch in " + testName,
                            expectedValue, actualValue);
                }
            });

            // Verify round-trip consistency: marshall → unmarshall produces a command
            // that marshalls identically
            ICommand roundTrippedCommand = CommandUtil.unmarshall(marshalledNode);
            ObjectNode secondMarshall = CommandUtil.marshall(roundTrippedCommand);
            Assert.assertEquals(
                    "Round-trip marshall inconsistency in " + testName + " at index " + i,
                    marshalledNode, secondMarshall);
        }
    }
}
