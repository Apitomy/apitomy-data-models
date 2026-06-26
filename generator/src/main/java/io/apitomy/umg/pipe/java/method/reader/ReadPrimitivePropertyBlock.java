package io.apitomy.umg.pipe.java.method.reader;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to read a primitive-typed property from JSON.
 */
public class ReadPrimitivePropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadPrimitivePropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("valueType", determineValueType(property.getResolvedType()));
        body.addContext("consumeProperty", determineConsumePropertyVariant(property.getResolvedType()));
        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));

        body.append("{");
        body.append("    ${valueType} value = JsonUtil.${consumeProperty}(json, \"${propertyName}\");");
        body.append("    node.${setterMethodName}(value);");
        body.append("}");
    }

    /**
     * Determines the Java data type of the given property.
     */
    String determineValueType(Type type) {
        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (_class != null) {
                readerClassSource.addImport(_class);
                return _class.getSimpleName();
            }
        }

        if (type.isListType()) {
            Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) type).getValueType();
            if (listValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(listValueType);
                if (_class != null) {
                    readerClassSource.addImport(_class);
                    return "List<" + _class.getSimpleName() + ">";
                }
            }
        }

        if (type.isMapType()) {
            Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) type).getValueType();
            if (mapValueType.isPrimitiveType()) {
                Class<?> _class = ctx.primitiveTypeToClass(mapValueType);
                if (_class != null) {
                    readerClassSource.addImport(_class);
                    return "Map<String, " + _class.getSimpleName() + ">";
                }
            }
        }

        PropertyModel property = propertyWithOrigin.getProperty();
        ctx.warn("Unable to determine value type for: " + property);
        return "Object";
    }

    /**
     * Determines the JsonUtil consume method variant to use for the given type.
     */
    String determineConsumePropertyVariant(Type type) {
        if (type.isEntityType()) {
            return "consumeObjectProperty";
        }

        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (ObjectNode.class.equals(_class)) {
                readerClassSource.addImport(_class);
                return "consumeObjectProperty";
            } else if (JsonNode.class.equals(_class)) {
                readerClassSource.addImport(_class);
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
                    readerClassSource.addImport(_class);
                    return "consumeObjectArrayProperty";
                } else if (JsonNode.class.equals(_class)) {
                    readerClassSource.addImport(_class);
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
                    readerClassSource.addImport(_class);
                    return "consumeObjectMapProperty";
                } else if (JsonNode.class.equals(_class)) {
                    readerClassSource.addImport(_class);
                    return "consumeAnyMapProperty";
                } else {
                    return "consume" + _class.getSimpleName() + "MapProperty";
                }
            }
        }

        PropertyModel property = propertyWithOrigin.getProperty();
        ctx.warn("Unable to determine value type for: " + property);
        return "consumeProperty";
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
