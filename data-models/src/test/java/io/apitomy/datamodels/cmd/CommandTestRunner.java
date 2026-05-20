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
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eric.wittmann@gmail.com
 */
public class CommandTestRunner extends ParentRunner<CommandTestCase> {

    private Class<?> testClass;
    private List<CommandTestCase> children;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
     */
    public CommandTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.children = loadTests();
    }

    private List<CommandTestCase> loadTests() throws InitializationError {
        try {
            URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/cmd/tests.json");
            List<CommandTestCase> allTests = mapper.readValue(testsJsonUrl, mapper.getTypeFactory().constructCollectionType(List.class, CommandTestCase.class));
            return allTests;
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    /**
     * @see ParentRunner#getChildren()
     */
    @Override
    protected List<CommandTestCase> getChildren() {
        return children;
    }

    /**
     * @see ParentRunner#describeChild(Object)
     */
    @Override
    protected Description describeChild(CommandTestCase child) {
        return Description.createTestDescription(this.testClass, child.getName());
    }

    /**
     * @see ParentRunner#runChild(Object, RunNotifier)
     */
    @Override
    protected void runChild(CommandTestCase child, RunNotifier notifier) {
        Description description = this.describeChild(child);
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LoggerUtil.info("------------------------");
                LoggerUtil.info("[CommandTestRunner] Running Test: %s", child.getName());
                String beforeCP = "fixtures/cmd/" + child.getTest() + ".before.json";
                String afterCP = "fixtures/cmd/" + child.getTest() + ".after.json";
                String commandsCP = "fixtures/cmd/" + child.getTest() + ".commands.json";
                
                if (child.getCommands() != null) {
                    commandsCP = "fixtures/cmd/" + child.getTest() + child.getCommands();
                }
                
                URL beforeUrl = Thread.currentThread().getContextClassLoader().getResource(beforeCP);
                URL afterUrl = Thread.currentThread().getContextClassLoader().getResource(afterCP);
                URL commandsUrl = Thread.currentThread().getContextClassLoader().getResource(commandsCP);

                Assert.assertNotNull("Test file not found on classpath: " + beforeCP, beforeUrl);
                Assert.assertNotNull("Test file not found on classpath: " + afterCP, afterUrl);
                Assert.assertNotNull("Test file not found on classpath: " + commandsCP, commandsUrl);

                // Read the test source
                String beforeJson = loadResource(beforeUrl);
                String afterJson = loadResource(afterUrl);
                String commandsJson = loadResource(commandsUrl);

                Assert.assertNotNull(beforeJson);
                Assert.assertNotNull(afterJson);
                Assert.assertNotNull(commandsJson);
                
                // Parse into a Json object
                ObjectNode beforeJs = (ObjectNode) JsonUtil.parseJSON(beforeJson);
                ArrayNode commandsJs = (ArrayNode) JsonUtil.parseJSON(commandsJson);

                Assert.assertNotNull(beforeJs);
                Assert.assertNotNull(commandsJs);

                // Read the before doc into a Document
                Document document = Library.readDocument(beforeJs);
                Assert.assertNotNull(document);
                
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
        };
        runLeaf(statement, description, notifier);
    }

    /**
     * Loads a resource as a string (reads the content at the URL).
     * @param testResource
     * @throws IOException
     */
    static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, StandardCharsets.UTF_8);
    }

    /**
     * Compares two JSON strings.
     * @param expected
     * @param actual
     * @throws JSONException
     */
    static void assertJsonEquals(String context, String expected, String actual) throws JSONException {
        // JSONAssert provides understandable validation of the equality of the JSON structure, but it doesn't currently
        // support verification of strict ordering of entries - https://github.com/skyscreamer/JSONassert/issues/81
        final boolean strict = true;
        JSONAssert.assertEquals(context, expected, actual, strict);
        
        // If the JSONAssert assertion passes, then the two JSON objects are strictly equivalent, though may have different
        // ordering. Round tripping the expected JSON with the same pretty printer used for the actual document allows for
        // a reasonable exact string comparison to be made. 
        final String expectedString = Library.writeDocumentToJSONString(Library.readDocumentFromJSONString(expected));
        Assert.assertEquals(context + " expected exact match for JSON string representations", expectedString, actual);
    }
}
