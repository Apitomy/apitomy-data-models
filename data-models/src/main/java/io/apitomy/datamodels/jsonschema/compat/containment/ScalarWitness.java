package io.apitomy.datamodels.jsonschema.compat.containment;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Builds simple, concrete candidate values for {@link ScalarContainment}'s
 * proof rules to hand to {@link WitnessValidator}. A value built here is
 * never trusted on its own -- every caller re-validates it against both
 * schemas before treating it as a counterexample, so an imprecise witness
 * only ever costs a proof (falls back to {@link ContainmentVerdict#UNKNOWN}),
 * never produces a wrong verdict.
 */
final class ScalarWitness {

    private ScalarWitness() {
    }

    /** A representative instance of the given JSON Schema type name, or {@code null} for a type this builds no example for. */
    static JsonNode exampleOfType(String type) {
        if (ScalarTypes.STRING.equals(type)) {
            return JsonUtil.toJsonNode("");
        }
        if (ScalarTypes.NUMBER.equals(type)) {
            return JsonUtil.toJsonNode(Double.valueOf(0.5));
        }
        if (ScalarTypes.INTEGER.equals(type)) {
            return JsonUtil.toJsonNode(Integer.valueOf(0));
        }
        if (ScalarTypes.BOOLEAN.equals(type)) {
            return JsonUtil.toJsonNode(Boolean.TRUE);
        }
        if (ScalarTypes.NULL.equals(type)) {
            return null;
        }
        return null;
    }

    /** A JSON number value equal to {@code value}, for schemas whose bounds are within double precision. */
    static JsonNode numberValue(ExactDecimal value) {
        return JsonUtil.toJsonNode(Double.valueOf(Double.parseDouble(value.toString())));
    }

    /** A JSON number value strictly less than {@code bound}, for schemas whose bounds are within double precision. */
    static JsonNode numberValueBelow(ExactDecimal bound) {
        double value = Double.parseDouble(bound.toString());
        return JsonUtil.toJsonNode(Double.valueOf(value - 1.0));
    }

    /** A JSON number value strictly greater than {@code bound}, for schemas whose bounds are within double precision. */
    static JsonNode numberValueAbove(ExactDecimal bound) {
        double value = Double.parseDouble(bound.toString());
        return JsonUtil.toJsonNode(Double.valueOf(value + 1.0));
    }

    /** A string of exactly {@code length} ASCII characters. */
    static JsonNode stringOfLength(int length) {
        if (length < 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append('a');
        }
        return JsonUtil.toJsonNode(result.toString());
    }
}
