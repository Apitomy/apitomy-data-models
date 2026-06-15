package io.apitomy.datamodels;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.io.IoTestCase;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Tests that cloning a document via {@link Library#cloneDocument(Document)} produces
 * a deep copy whose JSON serialization matches the original.
 */
public class CloneTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reuses the IO test fixtures so every supported document type is exercised.
     */
    static Stream<Named<IoTestCase>> provideTestCases() throws IOException {
        URL testsJsonUrl = Thread.currentThread().getContextClassLoader().getResource("fixtures/io/tests.json");
        List<IoTestCase> allTests = mapper.readValue(testsJsonUrl,
                mapper.getTypeFactory().constructCollectionType(List.class, IoTestCase.class));
        return allTests.stream().map(tc -> Named.of(tc.getName(), tc));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void cloneProducesIdenticalJson(IoTestCase child) throws Throwable {
        String testCP = "fixtures/io/" + child.getTest();
        URL testUrl = Thread.currentThread().getContextClassLoader().getResource(testCP);
        Assertions.assertNotNull(testUrl, "Test file not found on classpath: " + testCP);

        String original = IOUtils.toString(testUrl, "UTF-8");
        ObjectNode originalParsed = (ObjectNode) JsonUtil.parseJSON(original);
        Document doc = Library.readDocument(originalParsed);
        Assertions.assertNotNull(doc);

        Document clone = Library.cloneDocument(doc);
        Assertions.assertNotNull(clone);
        Assertions.assertNotSame(doc, clone, "Clone must be a distinct object");

        ObjectNode cloneJson = Library.writeDocument(clone);
        String cloneStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cloneJson);

        JSONAssert.assertEquals(original, cloneStr, true);
    }

}
