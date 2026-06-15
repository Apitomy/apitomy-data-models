/*
 * Copyright 2022 Red Hat
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

package io.apitomy.datamodels.paths;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.apitomy.datamodels.Library;

/**
 * @author eric.wittmann@gmail.com
 */
public class NodePathIoTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Provides test cases loaded from the fixtures JSON file.
     */
    static Stream<Named<NodePathIoTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/paths/io-tests.json");
        List<NodePathIoTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, NodePathIoTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void test(NodePathIoTestCase child) throws Throwable {
        String path = child.getPath();

        // Parse the path
        NodePath np = Library.parseNodePath(path);

        // Write that path back out to a string
        String actual = np.toString(true);

        // Compare
        String expected = path;
        Assertions.assertEquals(expected, actual);

        // Get the sequence of segments from the path
        List<String> segments = np.toSegments();
        List<String> expectedSegments = child.getSegments();
        Assertions.assertEquals(expectedSegments, segments);
    }

}
