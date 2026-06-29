package io.apitomy.umg.pipe.java.method;

import static java.util.Map.entry;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Shared helpers for mapping concept-model {@link Type}s to Java type names
 * and to the corresponding {@code JsonUtil} consume/set method variants.
 * Extracted from duplicated code across reader, writer, and cloner blocks.
 */
public final class PrimitiveTypeUtil {

    public static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP = Map.ofEntries(
            entry("string", String.class),
            entry("boolean", Boolean.class),
            entry("number", Number.class),
            entry("integer", Integer.class),
            entry("object", ObjectNode.class),
            entry("any", JsonNode.class));

    private PrimitiveTypeUtil() {
    }

    /**
     * Maps a primitive concept type to its corresponding Java class.
     *
     * @param type the primitive type
     * @return the Java class
     * @throws UnsupportedOperationException if the type is not primitive or has no mapping
     */
    public static Class<?> primitiveTypeToClass(Type type) {
        if (!type.isPrimitiveType()) {
            throw new UnsupportedOperationException("Property type not primitive: " + type);
        }
        Class<?> rval = PRIMITIVE_TYPE_MAP.get(type.getName());
        if (rval == null) {
            throw new UnsupportedOperationException("Primitive-to-class mapping not found for: " + type.getName());
        }
        return rval;
    }

    /**
     * Determines the Java data type name for a primitive (or list/map-of-primitive) type.
     * Any required import is added to {@code classSource}.
     *
     * @return the simple type name (e.g. "String", "List&lt;Integer&gt;") or "Object" as fallback
     */
    public static String determineValueType(Type type, CodeGenContext ctx, JavaClassSource classSource) {
        if (type.isPrimitiveType()) {
            Class<?> _class = primitiveTypeToClass(type);
            if (_class != null) {
                classSource.addImport(_class);
                return _class.getSimpleName();
            }
        }

        if (type.isListType()) {
            Type listValueType = ((ListType) type).getValueType();
            if (listValueType.isPrimitiveType()) {
                Class<?> _class = primitiveTypeToClass(listValueType);
                if (_class != null) {
                    classSource.addImport(_class);
                    return "List<" + _class.getSimpleName() + ">";
                }
            }
        }

        if (type.isMapType()) {
            Type mapValueType = ((MapType) type).getValueType();
            if (mapValueType.isPrimitiveType()) {
                Class<?> _class = primitiveTypeToClass(mapValueType);
                if (_class != null) {
                    classSource.addImport(_class);
                    return "Map<String, " + _class.getSimpleName() + ">";
                }
            }
        }

        return "Object";
    }

    /**
     * Determines the {@code JsonUtil.isXxx} type-check method name for a primitive type.
     * E.g. String → "isString", Boolean → "isBoolean", ObjectNode → "isObject", JsonNode → "isJsonNode".
     */
    public static String determineIsCheckMethod(Type primitiveType, CodeGenContext ctx, JavaClassSource classSource) {
        Class<?> _class = primitiveTypeToClass(primitiveType);
        if (ObjectNode.class.equals(_class)) {
            classSource.addImport(_class);
            return "isObject";
        } else if (JsonNode.class.equals(_class)) {
            classSource.addImport(_class);
            return "isJsonNode";
        } else if (String.class.equals(_class)) {
            return "isString";
        } else if (Boolean.class.equals(_class)) {
            return "isBoolean";
        } else if (Number.class.equals(_class) || Integer.class.equals(_class)) {
            return "isNumber";
        }
        return "isJsonNode";
    }

    /**
     * Determines the {@code JsonUtil.toXxx} conversion method name for a primitive type.
     * E.g. String → "toString", Boolean → "toBoolean", ObjectNode → "toObject", JsonNode → "toJsonNode".
     */
    public static String determineToConversionMethod(Type primitiveType, CodeGenContext ctx, JavaClassSource classSource) {
        Class<?> _class = primitiveTypeToClass(primitiveType);
        if (ObjectNode.class.equals(_class)) {
            classSource.addImport(_class);
            return "toObject";
        } else if (JsonNode.class.equals(_class)) {
            classSource.addImport(_class);
            return "toJsonNode";
        } else if (String.class.equals(_class)) {
            return "toString";
        } else if (Boolean.class.equals(_class)) {
            return "toBoolean";
        } else if (Integer.class.equals(_class)) {
            return "toInteger";
        } else if (Number.class.equals(_class)) {
            return "toNumber";
        }
        return "toJsonNode";
    }

    /**
     * Determines the expected type string for {@code JsonUtil.allMatch} / {@code JsonUtil.allValuesMatch}.
     * E.g. String → "string", Boolean → "boolean", ObjectNode → "object", JsonNode → "any".
     */
    public static String determineExpectedTypeString(Type primitiveType, CodeGenContext ctx) {
        Class<?> _class = primitiveTypeToClass(primitiveType);
        if (ObjectNode.class.equals(_class)) {
            return "object";
        } else if (JsonNode.class.equals(_class)) {
            return "any";
        } else if (String.class.equals(_class)) {
            return "string";
        } else if (Boolean.class.equals(_class)) {
            return "boolean";
        } else if (Number.class.equals(_class)) {
            return "number";
        } else if (Integer.class.equals(_class)) {
            return "number";
        }
        return "any";
    }

    /**
     * Returns true if the primitive type maps directly to a JsonNode subtype (ObjectNode or JsonNode),
     * meaning no wrapping via toJsonNode() is needed for writers and no conversion is needed for readers.
     */
    public static boolean isJsonNodeType(Type primitiveType, CodeGenContext ctx) {
        Class<?> _class = primitiveTypeToClass(primitiveType);
        return ObjectNode.class.equals(_class) || JsonNode.class.equals(_class);
    }
}
