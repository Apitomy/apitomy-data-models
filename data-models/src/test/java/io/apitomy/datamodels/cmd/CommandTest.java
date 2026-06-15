/*
 * Copyright 2019 JBoss Inc
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author eric.wittmann@gmail.com
 */
public class CommandTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<CommandTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/cmd/tests.json");
        List<CommandTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, CommandTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(CommandTestCase child) throws Throwable {
        LoggerUtil.info("------------------------");
        LoggerUtil.info("[CommandTest] Running Test: %s", child.getName());
        String beforeCP = "fixtures/cmd/" + child.getTest() + ".before.json";
        String afterCP = "fixtures/cmd/" + child.getTest() + ".after.json";
        String commandsCP = "fixtures/cmd/" + child.getTest() + ".commands.json";

        if (child.getCommands() != null) {
            commandsCP = "fixtures/cmd/" + child.getTest() + child.getCommands();
        }

        URL beforeUrl = Thread.currentThread().getContextClassLoader().getResource(beforeCP);
        URL afterUrl = Thread.currentThread().getContextClassLoader().getResource(afterCP);
        URL commandsUrl = Thread.currentThread().getContextClassLoader().getResource(commandsCP);

        Assertions.assertNotNull(beforeUrl, "Test file not found on classpath: " + beforeCP);
        Assertions.assertNotNull(afterUrl, "Test file not found on classpath: " + afterCP);
        Assertions.assertNotNull(commandsUrl, "Test file not found on classpath: " + commandsCP);

        // Read the test source
        String beforeJson = loadResource(beforeUrl);
        String afterJson = loadResource(afterUrl);
        String commandsJson = loadResource(commandsUrl);

        Assertions.assertNotNull(beforeJson);
        Assertions.assertNotNull(afterJson);
        Assertions.assertNotNull(commandsJson);

        // Parse into a Json object
        ObjectNode beforeJs = (ObjectNode) JsonUtil.parseJSON(beforeJson);
        ArrayNode commandsJs = (ArrayNode) JsonUtil.parseJSON(commandsJson);

        Assertions.assertNotNull(beforeJs);
        Assertions.assertNotNull(commandsJs);

        // Read the before doc into a Document
        Document document = Library.readDocument(beforeJs);
        Assertions.assertNotNull(document);

        // Load all the commands to apply.
        List<ICommand> commands = new ArrayList<>();
        ArrayNode allCommands = commandsJs;
        for (int cidx = 0; cidx < allCommands.size(); cidx++) {
            ObjectNode commandNode = (ObjectNode) allCommands.get(cidx);
            ICommand command = CommandFactory.unmarshall(commandNode);
            commands.add(command);
        }

        // Apply all the commands to the Document (modifying the document).
        commands.forEach(command -> {
            command.execute(document);
        });

        // Check that the resulting (modified) document is what we expected.
        String actual = Library.writeDocumentToJSONString(document);
        String expected = afterJson;
        assertJsonEquals("After commands", expected, actual);

        // If there was only ONE command, then undo it and make sure
        // that results in the original document.
        if (commands.size() == 1) {
            commands.get(0).undo(document);

            actual = Library.writeDocumentToJSONString(document);
            expected = beforeJson;

            assertJsonEquals("After undo", expected, actual);
        }
    }

    /**
     * Loads a resource as a string (reads the content at the URL).
     * @param testResource the URL to load
     */
    private static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, StandardCharsets.UTF_8);
    }

    /**
     * Compares two JSON strings.
     * @param context a label for assertion messages
     * @param expected the expected JSON string
     * @param actual the actual JSON string
     */
    private static void assertJsonEquals(String context, String expected, String actual) throws JSONException {
        // JSONAssert provides understandable validation of the equality of the JSON structure, but it doesn't currently
        // support verification of strict ordering of entries - https://github.com/skyscreamer/JSONassert/issues/81
        final boolean strict = true;
        JSONAssert.assertEquals(context, expected, actual, strict);

        // If the JSONAssert assertion passes, then the two JSON objects are strictly equivalent, though may have different
        // ordering. Round tripping the expected JSON with the same pretty printer used for the actual document allows for
        // a reasonable exact string comparison to be made.
        final String expectedString = Library.writeDocumentToJSONString(Library.readDocumentFromJSONString(expected));
        Assertions.assertEquals(expectedString, actual,
                context + " expected exact match for JSON string representations");
    }
}
