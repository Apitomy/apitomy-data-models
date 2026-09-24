package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.jsonschema.compat.containment.ExactDecimal;
import io.apitomy.datamodels.jsonschema.compat.containment.NumericProvenance;
import io.apitomy.datamodels.jsonschema.compat.containment.PortableSchemaUtil;
import io.apitomy.datamodels.util.NumberUtil;

/**
 * Exact decimal comparison/divisibility, numeric provenance, portable JSON
 * equality, and a lexical numeric-token scan -- against
 * {@code numeric-precision.json} -- plus characterization tests showing what
 * the pre-existing double-based comparator gets wrong, which is exactly what
 * this engine exists to avoid.
 */
class OpenApiPortableValuesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode fixture() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/numeric-precision.json");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        return root;
    }

    @Test
    void exactComparisonsAndDivisibilityMatchTheFixture() throws Exception {
        JsonNode cases = fixture().get("exactComparisons");
        for (JsonNode testCase : cases) {
            String description = testCase.get("description").asText();
            ExactDecimal a = ExactDecimal.parse(testCase.get("a").asText());
            ExactDecimal b = ExactDecimal.parse(testCase.get("b").asText());
            if (testCase.has("isIntegralMultipleOf")) {
                assertEquals(testCase.get("isIntegralMultipleOf").asBoolean(), a.isIntegralMultipleOf(b), description);
            }
            if (testCase.has("compareTo")) {
                int expected = testCase.get("compareTo").asInt();
                int actual = a.compareTo(b);
                assertEquals(Integer.signum(expected), Integer.signum(actual), description);
            }
        }
    }

    @Test
    void multipleOfZeroIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ExactDecimal.parse("5").isIntegralMultipleOf(ExactDecimal.parse("0")));
    }

    @Test
    void toStringRoundTripsToAnEquivalentCanonicalForm() {
        assertEquals(ExactDecimal.parse("100"), ExactDecimal.parse(ExactDecimal.parse("1e2").toString()));
        assertEquals("0.3", ExactDecimal.parse("0.3").toString());
        assertEquals("-5", ExactDecimal.parse("-5.0").toString());
    }

    @Test
    void jsonEqualityMatchesTheFixture() throws Exception {
        JsonNode cases = fixture().get("jsonEquality");
        for (JsonNode testCase : cases) {
            String description = testCase.get("description").asText();
            boolean expected = testCase.get("equal").asBoolean();
            assertEquals(expected, PortableSchemaUtil.jsonEquals(testCase.get("a"), testCase.get("b")), description);
        }
    }

    @Test
    void missingIsNeverEqualToExplicitNull() {
        JsonNode explicitNull = MAPPER.nullNode();
        assertFalse(PortableSchemaUtil.jsonEquals(null, explicitNull));
        assertTrue(PortableSchemaUtil.jsonEquals(explicitNull, explicitNull));
    }

    @Test
    void numericTokenScanRecoversExactSourceTextByPointerHonoringEscapesStringsArraysAndExponents() throws Exception {
        JsonNode scanCase = fixture().get("numericTokenScan");
        String document = scanCase.get("document").asText();
        Map<String, String> tokens = PortableSchemaUtil.scanNumericTokens(document);

        JsonNode expected = scanCase.get("expected");
        java.util.Iterator<String> fieldNames = expected.fieldNames();
        while (fieldNames.hasNext()) {
            String pointer = fieldNames.next();
            assertEquals(expected.get(pointer).asText(), tokens.get(pointer), "token at " + pointer);
        }
        assertEquals(5, tokens.size(), "the number-looking text inside the string value must not be scanned");
    }

    @Test
    void numericProvenanceIsExactOnlyForFiniteSafeIntegers() {
        assertTrue(NumericProvenance.fromParsedNumber(42).isExact());
        assertEquals("42", NumericProvenance.fromParsedNumber(42).getSourceText());

        assertTrue(NumericProvenance.fromParsedNumber(NumericProvenance.MAX_SAFE_INTEGER).isExact());
        assertFalse(NumericProvenance.fromParsedNumber((double) NumericProvenance.MAX_SAFE_INTEGER + 2.0).isExact());
        assertFalse(NumericProvenance.fromParsedNumber(1.5).isExact());
        assertFalse(NumericProvenance.uncertain().isExact());
    }

    // -- Characterization tests: what the pre-existing double-based comparator gets wrong. --

    @Test
    void characterization_doubleComparatorCollapsesAdjacentIntegersAboveTheSafeRange() {
        long unsafeA = NumericProvenance.MAX_SAFE_INTEGER + 2; // 9007199254740993
        long unsafeB = NumericProvenance.MAX_SAFE_INTEGER + 1; // 9007199254740992
        assertEquals(0, NumberUtil.compare(unsafeA, unsafeB),
                "baseline: the existing double-based comparator cannot tell these apart");
        assertTrue(ExactDecimal.parse(Long.toString(unsafeA)).compareTo(ExactDecimal.parse(Long.toString(unsafeB))) > 0,
                "new engine: exact decimal comparison does tell them apart");
    }

    @Test
    void characterization_toleranceBasedDivisibilityAcceptsANonintegralNearIntegerQuotient() {
        double value = 0.30000000000000004; // the actual double nearest the literal 0.3
        double divisor = 0.1;
        double quotient = value / divisor;
        boolean baselineAcceptsIt = Math.abs(quotient - Math.round(quotient)) < 1e-9;
        assertTrue(baselineAcceptsIt, "baseline: this happens to still pass for this particular pair");

        // The point of the new engine is that it does not need the double division
        // that produces that quotient at all: exact digit-string arithmetic gives
        // the same correct answer without a tolerance, and does not accept a
        // genuinely-nonintegral quotient that happens to land within 1e-9 of an
        // integer, as the baseline tolerance would.
        assertTrue(ExactDecimal.parse("0.3").isIntegralMultipleOf(ExactDecimal.parse("0.1")));

        double near = 1.0 + 1e-10; // within the baseline's 1e-9 tolerance of 1, but not equal to it
        double nearQuotient = near / 1.0;
        boolean baselineAcceptsNear = Math.abs(nearQuotient - Math.round(nearQuotient)) < 1e-9;
        assertTrue(baselineAcceptsNear, "baseline: a near-integer quotient within tolerance is accepted");
        assertFalse(ExactDecimal.parse("1.0000000001").isIntegralMultipleOf(ExactDecimal.parse("1")),
                "new engine: the same near-integer value is correctly rejected as non-exact");
    }
}
