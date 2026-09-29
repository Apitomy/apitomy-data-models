package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@code OpenApiCompatibilityChecker} (T18): end-to-end assembly across all
 * six synchronous entry points, all four OpenAPI families, patch-version
 * tolerance, cross-family rejection, invalid JSON, and an independently
 * unresolved operation alongside a known break.
 */
class OpenApiCheckerTest {

    private static final String ORIGINAL_30 = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"parameters\":[{\"name\":\"limit\",\"in\":\"query\",\"schema\":{\"type\":\"integer\"}}],"
            + "\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    private static final String UPDATED_30_BREAKING = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    @Test
    void checkBackwardJsonDetectsARemovedParameterAsIncompatible() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder()
                .policy(CompatibilityPolicy.defaults())
                .build();
        CompatibilityResult result = checker.checkBackwardJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
    }

    @Test
    void checkForwardJsonOfTheSamePairIsCompatible() {
        // Forward: can the original (which lacks the parameter) replace the updated
        // document? The updated side never required it either (it's optional), so
        // nothing the updated document's consumers rely on is lost by the rollback.
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult result = checker.checkForwardJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
    }

    @Test
    void checkFullJsonPreservesBothDirectionsIndependently() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        FullCompatibilityResult result = checker.checkFullJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getBackwardResult().getVerdict());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getForwardResult().getVerdict());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
    }

    @Test
    void modelInstanceEntryPointsMatchTheirJsonCounterparts() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        io.apitomy.datamodels.models.RootCapable original = (io.apitomy.datamodels.models.RootCapable)
                io.apitomy.datamodels.Library.readRootFromJSONString(ORIGINAL_30);
        io.apitomy.datamodels.models.RootCapable updated = (io.apitomy.datamodels.models.RootCapable)
                io.apitomy.datamodels.Library.readRootFromJSONString(UPDATED_30_BREAKING);

        CompatibilityResult backward = checker.checkBackward(original, updated, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, backward.getVerdict());

        CompatibilityResult forward = checker.checkForward(original, updated, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.COMPATIBLE, forward.getVerdict());

        FullCompatibilityResult full = checker.checkFull(original, updated, CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, full.getVerdict());
    }

    @Test
    void allFourOpenApiFamiliesAreAccepted() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        String[] versions = { "2.0", "3.0.4", "3.1.0", "3.2.0" };
        for (int i = 0; i < versions.length; i++) {
            String doc = documentFor(versions[i]);
            CompatibilityResult result = checker.checkBackwardJson(doc, doc, CheckOptions.defaults());
            assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict(), "version " + versions[i]);
        }
    }

    private static String documentFor(String version) {
        if ("2.0".equals(version)) {
            return "{\"swagger\":\"2.0\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
                    + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";
        }
        return "{\"openapi\":\"" + version + "\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
                + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";
    }

    @Test
    void patchVersionDifferencesAreAcceptedAsTheSameFamily() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult result = checker.checkBackwardJson(documentFor("3.0.0"), documentFor("3.0.4"), CheckOptions.defaults());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
    }

    @Test
    void crossFamilyComparisonIsAnExplicitError() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> checker.checkBackwardJson(documentFor("2.0"), documentFor("3.0.4"), CheckOptions.defaults()));
    }

    @Test
    void invalidJsonIsAnExplicitError() {
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        assertThrows(RuntimeException.class,
                () -> checker.checkBackwardJson("{ not valid json", documentFor("3.0.4"), CheckOptions.defaults()));
    }

    @Test
    void anIndependentlyUnresolvedOperationDoesNotHideAKnownBreak() {
        String original = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
                + "\"paths\":{"
                + "\"/known\":{\"get\":{\"parameters\":[{\"name\":\"id\",\"in\":\"query\",\"schema\":{\"type\":\"integer\"}}],"
                + "\"responses\":{\"200\":{\"description\":\"ok\"}}}},"
                + "\"/unknown\":{\"get\":{\"parameters\":[{\"name\":\"filter\",\"in\":\"query\","
                + "\"schema\":{\"type\":\"string\",\"pattern\":\"^a\"}}],\"responses\":{\"200\":{\"description\":\"ok\"}}}}"
                + "}}";
        String updated = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
                + "\"paths\":{"
                + "\"/known\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}},"
                + "\"/unknown\":{\"get\":{\"parameters\":[{\"name\":\"filter\",\"in\":\"query\","
                + "\"schema\":{\"type\":\"string\",\"pattern\":\"^b\"}}],\"responses\":{\"200\":{\"description\":\"ok\"}}}}"
                + "}}";
        OpenApiCompatibilityChecker checker = OpenApiCompatibilityChecker.builder().build();
        CompatibilityResult result = checker.checkBackwardJson(original, updated, CheckOptions.defaults());
        // /known's parameter removal is a definite break; /unknown's differing regex
        // patterns are Unknown (no regex-language-inclusion claim) -- both must be
        // present, and the aggregate verdict is still Incompatible (Breaking wins).
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
        boolean sawBreaking = false;
        boolean sawUnresolved = false;
        for (CompatibilityFinding finding : result.getFindings()) {
            if (finding.getImpact() == FindingImpact.BREAKING) {
                sawBreaking = true;
            }
            if (finding.getImpact() == FindingImpact.UNRESOLVED) {
                sawUnresolved = true;
            }
        }
        assertTrue(sawBreaking, "Expected the /known parameter removal to be reported as Breaking");
        assertTrue(sawUnresolved, "Expected the /unknown pattern change to be reported as Unresolved");
    }
}
