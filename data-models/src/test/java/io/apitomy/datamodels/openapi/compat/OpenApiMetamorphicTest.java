package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceLoader;
import io.apitomy.datamodels.openapi.compat.resource.ResourceDocument;
import io.apitomy.datamodels.openapi.compat.resource.ResourceRequest;

/**
 * Metamorphic properties (T19/V5) that must hold regardless of which
 * concrete fixture is used: forward/backward symmetry, full preserving both
 * directions, effective-verdict invariance under harmless textual
 * permutation (parameter reordering, an added unused component), and
 * sync/async equivalence for a snapshot with no external references.
 * <p>
 * Not attempted here: "extracting an inline schema into an equivalent
 * reference preserves verdicts" -- blocked by the known, already-documented
 * gap that schema {@code $ref} is not dereferenced at all yet (see T17's
 * and T18's Javadoc); a $ref'd schema usage is Unknown regardless of what
 * it points to, so this property does not yet hold and asserting it would
 * misrepresent a real limitation as tested behavior.
 */
class OpenApiMetamorphicTest {

    private static final String WITH_PARAM_A_THEN_B = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"parameters\":["
            + "{\"name\":\"a\",\"in\":\"query\",\"schema\":{\"type\":\"string\"}},"
            + "{\"name\":\"b\",\"in\":\"query\",\"schema\":{\"type\":\"string\"}}"
            + "],\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    private static final String WITH_PARAM_B_THEN_A = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"parameters\":["
            + "{\"name\":\"b\",\"in\":\"query\",\"schema\":{\"type\":\"string\"}},"
            + "{\"name\":\"a\",\"in\":\"query\",\"schema\":{\"type\":\"string\"}}"
            + "],\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    private static final String ORIGINAL = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"parameters\":[{\"name\":\"id\",\"in\":\"query\",\"required\":true,"
            + "\"schema\":{\"type\":\"string\"}}],\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    private static final String UPDATED_BREAKING = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    private static List<String> codesAndImpacts(CompatibilityResult result) {
        List<String> pairs = new ArrayList<String>();
        List<CompatibilityFinding> findings = result.getFindings();
        for (int i = 0; i < findings.size(); i++) {
            pairs.add(findings.get(i).getCode() + ":" + findings.get(i).getImpact());
        }
        return pairs;
    }

    @Test
    void forwardOfAAndBMatchesBackwardOfBAndA() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult forwardAB = checker.checkForwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());
        CompatibilityResult backwardBA = checker.checkBackwardJson(UPDATED_BREAKING, ORIGINAL, CheckOptions.defaults());

        assertEquals(forwardAB.getVerdict(), backwardBA.getVerdict());
        assertEquals(codesAndImpacts(forwardAB), codesAndImpacts(backwardBA));
    }

    @Test
    void fullPreservesTheTwoIndividualDirectionalVerdicts() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult backward = checker.checkBackwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());
        CompatibilityResult forward = checker.checkForwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());
        FullCompatibilityResult full = checker.checkFullJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());

        assertEquals(backward.getVerdict(), full.getBackwardResult().getVerdict());
        assertEquals(forward.getVerdict(), full.getForwardResult().getVerdict());
    }

    @Test
    void reorderingParametersPreservesTheEffectiveVerdict() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult forwardOrder = checker.checkBackwardJson(WITH_PARAM_A_THEN_B, WITH_PARAM_B_THEN_A, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.COMPATIBLE, forwardOrder.getVerdict());
    }

    @Test
    void addingAnUnusedComponentPreservesTheVerdict() {
        String withUnusedComponent = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
                + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}},"
                + "\"components\":{\"schemas\":{\"Unused\":{\"type\":\"object\"}}}}";
        String withoutIt = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
                + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult result = checker.checkBackwardJson(withoutIt, withUnusedComponent, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
        assertTrue(result.getFindings().isEmpty());
    }

    @Test
    void syncEqualsAsyncForTheSameSnapshot() throws Exception {
        OpenApiCompatibilityChecker syncChecker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult syncResult = syncChecker.checkBackwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());

        OpenApiAsyncCompatibilityChecker asyncChecker = OpenApiAsyncCompatibilityChecker.builder().build();
        AsyncResourceLoader neverCalled = new AsyncResourceLoader() {
            @Override
            public CompletionStage<ResourceDocument> load(ResourceRequest request) {
                throw new AssertionError("Loader should never be invoked: " + request.getUri());
            }
        };
        CompletionStage<CompatibilityResult> asyncStage = asyncChecker.checkBackwardJson(ORIGINAL, UPDATED_BREAKING,
                CheckOptions.defaults(), neverCalled);
        CompatibilityResult asyncResult = ((CompletableFuture<CompatibilityResult>) asyncStage).get();

        assertEquals(syncResult.getVerdict(), asyncResult.getVerdict());
        assertEquals(codesAndImpacts(syncResult), codesAndImpacts(asyncResult));
    }
}
