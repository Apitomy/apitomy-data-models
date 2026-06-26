package io.apitomy.umg.pipe.java.method.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to write a primitive-typed property to JSON.
 */
public class WritePrimitivePropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WritePrimitivePropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("setPropertyMethodName", determineSetPropertyVariant(property.getResolvedType()));
        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));

        body.append("JsonUtil.${setPropertyMethodName}(json, \"${propertyName}\", node.${getterMethodName}());");
    }

    /**
     * Determines the JsonUtil set method variant to use for the given type.
     */
    String determineSetPropertyVariant(Type type) {
        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (ObjectNode.class.equals(_class)) {
                writerClassSource.addImport(_class);
                return "setObjectProperty";
            } else if (JsonNode.class.equals(_class)) {
                writerClassSource.addImport(_class);
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
                    writerClassSource.addImport(_class);
                    return "setObjectArrayProperty";
                } else if (JsonNode.class.equals(_class)) {
                    writerClassSource.addImport(_class);
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
                    writerClassSource.addImport(_class);
                    return "setObjectMapProperty";
                } else if (JsonNode.class.equals(_class)) {
                    writerClassSource.addImport(_class);
                    return "setAnyMapProperty";
                } else {
                    return "set" + _class.getSimpleName() + "MapProperty";
                }
            }
        }

        PropertyModel property = propertyWithOrigin.getProperty();
        ctx.warn("Unable to determine value type for: " + property);
        return "setProperty";
    }

    /**
     * Determines the Java data type of the given property.
     */
    String determineValueType(Type type) {
        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (_class != null) {
                writerClassSource.addImport(_class);
                return _class.getSimpleName();
            }
        }

        if (type.isListType()) {
            Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) type).getValueType();
            if (listValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(listValueType);
                if (_class != null) {
                    writerClassSource.addImport(_class);
                    return "List<" + _class.getSimpleName() + ">";
                }
            }
        }

        if (type.isMapType()) {
            Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) type).getValueType();
            if (mapValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(mapValueType);
                if (_class != null) {
                    writerClassSource.addImport(_class);
                    return "Map<String, " + _class.getSimpleName() + ">";
                }
            }
        }

        PropertyModel property = propertyWithOrigin.getProperty();
        ctx.warn("Unable to determine value type for: " + property);
        return "Object";
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
