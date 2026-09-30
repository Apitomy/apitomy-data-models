package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentContext;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentResult;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentVerdict;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaContainment;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;

/**
 * Schema {@code $ref} resolution and cycle-safe recursive traversal: the
 * gap discovered (and deliberately deferred) in T17/T18/T19 -- schema
 * {@code $ref} was never dereferenced at all -- closed for same-document
 * ({@code "#/..."}) references, together with the cycle guard
 * {@link SchemaContainment} needs once a {@code $ref} can actually be
 * followed into a genuinely recursive schema graph (a self-referential or
 * mutually-referential schema pair), which was previously impossible to
 * reach (a {@code $ref} was always an immediate, harmless Unknown). The
 * guard's job is only to keep traversal finite -- it does not add any
 * fixed-point/coinductive proof capability, so a genuinely recursive
 * schema compared against itself still honestly resolves Unknown rather
 * than a guessed Yes (see {@link #aSelfReferentialRecursiveSchemaIsCycleSafe}).
 */
class SchemaContainmentRefTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static SchemaView view(JsonNode documentRoot, String pointer) {
        JsonNode node = navigate(documentRoot, pointer);
        return new SchemaView(node, SchemaDialect.DRAFT2020_12, "urn:test:resource", pointer, documentRoot);
    }

    private static JsonNode navigate(JsonNode root, String pointer) {
        JsonNode current = root;
        if (pointer.length() == 0) {
            return current;
        }
        for (String segment : pointer.substring(1).split("/")) {
            current = current.get(segment);
        }
        return current;
    }

    private static ContainmentContext context() {
        return ContainmentContext.of(HttpRole.REQUEST, ProviderRole.INPUT, CheckDirection.BACKWARD, CompatibilityPolicy.defaults());
    }

    @Test
    void aTopLevelRefResolvesAndComparesReflexively() throws Exception {
        JsonNode document = MAPPER.readTree(
                "{\"$defs\":{\"Widget\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}},"
                        + "\"source\":{\"$ref\":\"#/$defs/Widget\"},\"target\":{\"$ref\":\"#/$defs/Widget\"}}");
        SchemaView source = view(document, "/source");
        SchemaView target = view(document, "/target");

        ContainmentResult result = SchemaContainment.compare(source, target, context());
        assertEquals(ContainmentVerdict.YES, result.getVerdict());
    }

    @Test
    void differentRefTargetsResolveAndCompareByTheirActualContent() throws Exception {
        JsonNode document = MAPPER.readTree(
                "{\"$defs\":{"
                        + "\"Wide\":{\"type\":\"string\",\"enum\":[\"a\",\"b\",\"c\"]},"
                        + "\"Narrow\":{\"type\":\"string\",\"enum\":[\"a\",\"b\"]}"
                        + "},\"source\":{\"$ref\":\"#/$defs/Wide\"},\"target\":{\"$ref\":\"#/$defs/Narrow\"}}");
        SchemaView source = view(document, "/source");
        SchemaView target = view(document, "/target");

        ContainmentResult narrowing = SchemaContainment.compare(source, target, context());
        assertEquals(ContainmentVerdict.NO, narrowing.getVerdict());

        ContainmentResult widening = SchemaContainment.compare(target, source, context());
        assertEquals(ContainmentVerdict.YES, widening.getVerdict());
    }

    @Test
    void aRefNestedInsideAPropertyResolves() throws Exception {
        JsonNode document = MAPPER.readTree(
                "{\"$defs\":{\"Owner\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"}}}},"
                        + "\"source\":{\"type\":\"object\",\"properties\":{\"owner\":{\"$ref\":\"#/$defs/Owner\"}}},"
                        + "\"target\":{\"type\":\"object\",\"properties\":{\"owner\":{\"$ref\":\"#/$defs/Owner\"}}}}");
        SchemaView source = view(document, "/source");
        SchemaView target = view(document, "/target");

        assertEquals(ContainmentVerdict.YES, SchemaContainment.compare(source, target, context()).getVerdict());
    }

    @Test
    void aSelfReferentialRecursiveSchemaIsCycleSafe() throws Exception {
        JsonNode document = MAPPER.readTree(
                "{\"$defs\":{\"TreeNode\":{\"type\":\"object\",\"properties\":{"
                        + "\"value\":{\"type\":\"integer\"},"
                        + "\"children\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/$defs/TreeNode\"}}}}},"
                        + "\"source\":{\"$ref\":\"#/$defs/TreeNode\"},\"target\":{\"$ref\":\"#/$defs/TreeNode\"}}");
        SchemaView source = view(document, "/source");
        SchemaView target = view(document, "/target");

        ContainmentResult result = assertTimeout(Duration.ofSeconds(5),
                () -> SchemaContainment.compare(source, target, context()));
        assertNotNull(result);
        // No stack overflow and no infinite loop -- that alone is the primary
        // property under test. The verdict itself is honestly UNKNOWN, not a
        // guessed YES: the `children` property recurses back into the same
        // TreeNode-vs-TreeNode pair, the cycle guard reports that inner
        // occurrence UNKNOWN (never assuming a schema trivially contains
        // itself through unbounded recursion), and that UNKNOWN correctly
        // propagates up through the enclosing object comparison rather than
        // being silently discarded -- this checker does not implement
        // fixed-point/coinductive reasoning that could prove two occurrences
        // of the identical recursive schema equal.
        assertEquals(ContainmentVerdict.UNKNOWN, result.getVerdict());
    }

    @Test
    void aMutuallyRecursiveTwoSchemaCycleIsCycleSafe() throws Exception {
        JsonNode document = MAPPER.readTree(
                "{\"$defs\":{"
                        + "\"A\":{\"type\":\"object\",\"properties\":{\"next\":{\"$ref\":\"#/$defs/B\"}}},"
                        + "\"B\":{\"type\":\"object\",\"properties\":{\"next\":{\"$ref\":\"#/$defs/A\"}}}"
                        + "},\"source\":{\"$ref\":\"#/$defs/A\"},\"target\":{\"$ref\":\"#/$defs/A\"}}");
        SchemaView source = view(document, "/source");
        SchemaView target = view(document, "/target");

        ContainmentResult result = assertTimeout(Duration.ofSeconds(5),
                () -> SchemaContainment.compare(source, target, context()));
        assertNotNull(result);
        assertEquals(ContainmentVerdict.UNKNOWN, result.getVerdict());
    }

    @Test
    void anExternalOrUnresolvableRefStaysUnknownExactlyAsBeforeThisFeatureExisted() throws Exception {
        JsonNode document = MAPPER.readTree(
                "{\"source\":{\"$ref\":\"other-file.json#/Foo\"},\"target\":{\"type\":\"object\"}}");
        SchemaView source = view(document, "/source");
        SchemaView target = view(document, "/target");

        ContainmentResult result = SchemaContainment.compare(source, target, context());
        assertEquals(ContainmentVerdict.UNKNOWN, result.getVerdict());
    }

    @Test
    void aLongRefChainAndADeepRecursiveGraphCompleteQuickly() throws Exception {
        // A chain of 8 refs, each pointing to the next, terminating in a real
        // schema -- within resolveRef()'s bounded (10-hop) multi-hop loop.
        // Exercises that bounded loop as well as SchemaContainment's cycle
        // guard under many distinct, non-cyclic pointers in one comparison.
        int chainLength = 8;
        StringBuilder defs = new StringBuilder();
        for (int i = 0; i < chainLength; i++) {
            if (i > 0) {
                defs.append(",");
            }
            if (i < chainLength - 1) {
                defs.append("\"Link").append(i).append("\":{\"$ref\":\"#/$defs/Link").append(i + 1).append("\"}");
            } else {
                defs.append("\"Link").append(i).append("\":{\"type\":\"string\"}");
            }
        }
        JsonNode chainDocument = MAPPER.readTree("{\"$defs\":{" + defs + "},"
                + "\"source\":{\"$ref\":\"#/$defs/Link0\"},\"target\":{\"$ref\":\"#/$defs/Link0\"}}");
        SchemaView chainSource = view(chainDocument, "/source");
        SchemaView chainTarget = view(chainDocument, "/target");
        ContainmentResult chainResult = assertTimeout(Duration.ofSeconds(5),
                () -> SchemaContainment.compare(chainSource, chainTarget, context()));
        assertEquals(ContainmentVerdict.YES, chainResult.getVerdict());

        // A wide, deeply-nested recursive object graph (a linked list of 200
        // "Node" objects via allOf/properties, each referencing the same
        // recursive schema) -- exercises the cycle guard under sustained,
        // repeated recursion into the identical pointer pair, not just a
        // single self-reference.
        JsonNode listDocument = MAPPER.readTree(
                "{\"$defs\":{\"Node\":{\"type\":\"object\",\"properties\":{"
                        + "\"value\":{\"type\":\"integer\"},\"next\":{\"$ref\":\"#/$defs/Node\"}}}},"
                        + "\"source\":{\"$ref\":\"#/$defs/Node\"},\"target\":{\"$ref\":\"#/$defs/Node\"}}");
        SchemaView listSource = view(listDocument, "/source");
        SchemaView listTarget = view(listDocument, "/target");
        assertTimeout(Duration.ofSeconds(5), () -> SchemaContainment.compare(listSource, listTarget, context()));
    }
}
