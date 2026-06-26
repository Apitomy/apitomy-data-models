package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.models.concept.type.Type;

/**
 * Shared helpers for mapping concept-model {@link Type}s to Java type names
 * and to the corresponding {@code JsonUtil} consume/set method variants.
 * Extracted from duplicated code across reader, writer, and cloner blocks.
 */
public final class PrimitiveTypeHelper {

    private PrimitiveTypeHelper() {
    }

    /**
     * Determines the Java data type name for a primitive (or list/map-of-primitive) type.
     * Any required import is added to {@code classSource}.
     *
     * @return the simple type name (e.g. "String", "List&lt;Integer&gt;") or "Object" as fallback
     */
    public static String determineValueType(Type type, CodeGenContext ctx, JavaClassSource classSource) {
        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (_class != null) {
                classSource.addImport(_class);
                return _class.getSimpleName();
            }
        }

        if (type.isListType()) {
            Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) type).getValueType();
            if (listValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(listValueType);
                if (_class != null) {
                    classSource.addImport(_class);
                    return "List<" + _class.getSimpleName() + ">";
                }
            }
        }

        if (type.isMapType()) {
            Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) type).getValueType();
            if (mapValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(mapValueType);
                if (_class != null) {
                    classSource.addImport(_class);
                    return "Map<String, " + _class.getSimpleName() + ">";
                }
            }
        }

        return "Object";
    }

    /**
     * Determines the {@code JsonUtil.consume*} method variant for reading a property of the given type.
     *
     * @return the method name, e.g. "consumeStringProperty", "consumeObjectArrayProperty"
     */
    public static String determineConsumePropertyVariant(Type type, CodeGenContext ctx, JavaClassSource classSource) {
        if (type.isEntityType()) {
            return "consumeObjectProperty";
        }

        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (ObjectNode.class.equals(_class)) {
                classSource.addImport(_class);
                return "consumeObjectProperty";
            } else if (JsonNode.class.equals(_class)) {
                classSource.addImport(_class);
                return "consumeAnyProperty";
            } else {
                return "consume" + _class.getSimpleName() + "Property";
            }
        }

        if (type.isListType()) {
            Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) type).getValueType();
            if (listValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(listValueType);
                if (ObjectNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "consumeObjectArrayProperty";
                } else if (JsonNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "consumeAnyArrayProperty";
                } else {
                    return "consume" + _class.getSimpleName() + "ArrayProperty";
                }
            }
        }

        if (type.isMapType()) {
            Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) type).getValueType();
            if (mapValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(mapValueType);
                if (ObjectNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "consumeObjectMapProperty";
                } else if (JsonNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "consumeAnyMapProperty";
                } else {
                    return "consume" + _class.getSimpleName() + "MapProperty";
                }
            }
        }

        return "consumeProperty";
    }

    /**
     * Determines the {@code JsonUtil.set*} method variant for writing a property of the given type.
     *
     * @return the method name, e.g. "setStringProperty", "setObjectArrayProperty"
     */
    public static String determineSetPropertyVariant(Type type, CodeGenContext ctx, JavaClassSource classSource) {
        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (ObjectNode.class.equals(_class)) {
                classSource.addImport(_class);
                return "setObjectProperty";
            } else if (JsonNode.class.equals(_class)) {
                classSource.addImport(_class);
                return "setAnyProperty";
            } else {
                return "set" + _class.getSimpleName() + "Property";
            }
        }

        if (type.isListType()) {
            Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) type).getValueType();
            if (listValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(listValueType);
                if (ObjectNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "setObjectArrayProperty";
                } else if (JsonNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "setAnyArrayProperty";
                } else {
                    return "set" + _class.getSimpleName() + "ArrayProperty";
                }
            }
        }

        if (type.isMapType()) {
            Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) type).getValueType();
            if (mapValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(mapValueType);
                if (ObjectNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "setObjectMapProperty";
                } else if (JsonNode.class.equals(_class)) {
                    classSource.addImport(_class);
                    return "setAnyMapProperty";
                } else {
                    return "set" + _class.getSimpleName() + "MapProperty";
                }
            }
        }

        return "setProperty";
    }
}
