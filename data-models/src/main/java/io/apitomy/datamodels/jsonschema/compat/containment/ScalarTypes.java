package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * JSON Schema instance type names ({@code "string"}, {@code "number"}, ...)
 * and how they relate to a concrete JSON value, without ever calling a
 * {@link JsonNode} instance method directly (see {@link PortableSchemaUtil}
 * for why).
 */
final class ScalarTypes {

    static final String NULL = "null";
    static final String BOOLEAN = "boolean";
    static final String OBJECT = "object";
    static final String ARRAY = "array";
    static final String STRING = "string";
    static final String NUMBER = "number";
    static final String INTEGER = "integer";

    private ScalarTypes() {
    }

    /** The JSON Schema instance type name of {@code value}: {@code "integer"} only for a numeric value with no fractional part. */
    static String nameOf(JsonNode value) {
        if (value == null) {
            return NULL;
        }
        if (JsonUtil.isObject(value)) {
            return OBJECT;
        }
        if (JsonUtil.isArray(value)) {
            return ARRAY;
        }
        if (JsonUtil.isString(value)) {
            return STRING;
        }
        if (JsonUtil.isBoolean(value)) {
            return BOOLEAN;
        }
        if (JsonUtil.isNumber(value)) {
            double doubleValue = JsonUtil.toNumber(value).doubleValue();
            if (!Double.isNaN(doubleValue) && !Double.isInfinite(doubleValue) && doubleValue == Math.floor(doubleValue)) {
                return INTEGER;
            }
            return NUMBER;
        }
        return NULL;
    }

    /** True if a value of type {@code candidateType} is admitted by the JSON Schema {@code type} value {@code declaredType}. */
    static boolean matches(String candidateType, String declaredType) {
        if (candidateType.equals(declaredType)) {
            return true;
        }
        // Every integer instance is also a number instance.
        return INTEGER.equals(candidateType) && NUMBER.equals(declaredType);
    }

    /** True if a value of type {@code candidateType} is admitted by at least one of {@code declaredTypes}. */
    static boolean matchesAny(String candidateType, List<String> declaredTypes) {
        for (int i = 0; i < declaredTypes.size(); i++) {
            if (matches(candidateType, declaredTypes.get(i))) {
                return true;
            }
        }
        return false;
    }
}
