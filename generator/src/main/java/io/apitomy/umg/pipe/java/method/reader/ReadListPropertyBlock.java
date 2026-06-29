package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeUtil;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read a list property from JSON (primitive list or entity list).
 */
public class ReadListPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadListPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type listValueType = ((ListType) property.getResolvedType()).getValueType();
        if (listValueType.isPrimitiveType()) {
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(List.class);
            readerClassSource.addImport(ArrayList.class);

            String expectedType = PrimitiveTypeUtil.determineExpectedTypeString(listValueType, prop.getCtx());
            String toConversionMethod = PrimitiveTypeUtil.determineToConversionMethod(listValueType, prop.getCtx(), readerClassSource);
            String elementValueType = PrimitiveTypeUtil.determineValueType(listValueType, prop.getCtx(), readerClassSource);
            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "setterMethodName", prop.getSetterName(),
                    "expectedType", expectedType,
                    "toConversionMethod", toConversionMethod,
                    "elementValueType", elementValueType,
                    "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
            ));

            body.appendBlock("""
{
    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
    if (JsonUtil.isArray(${varName}) && JsonUtil.allMatch(${varName}, "${expectedType}")) {
        List<${elementValueType}> items = new ArrayList<>();
        List<JsonNode> _nodes = JsonUtil.toList(${varName});
        for (int _i = 0; _i < _nodes.size(); _i++) {
            items.add(JsonUtil.${toConversionMethod}(_nodes.get(_i)));
        }
        node.${setterMethodName}(items);
        JsonUtil.removeProperty(json, "${propertyName}");
    }
}
""");
        } else if (listValueType.isEntityType()) {
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), prop.getOwningEntity(), prop.getCtx(), "LIST");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(List.class);

            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "setterMethodName", prop.getSetterName(),
                    "listValueJavaType", resolved.javaInterface().getName(),
                    "createMethodName", new FactoryMethod(resolved.entityModel().getName()).getName(),
                    "readMethodName", new ReaderMethod(resolved.entityModel().getName()).getName(),
                    "addMethodName", new AddMethod(prop.getCtx().singularize(property.getName())).getName(),
                    "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
            ));

            body.appendBlock("""
{
    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
    if (JsonUtil.isArray(${varName}) && JsonUtil.allMatch(${varName}, "object")) {
        List<JsonNode> _nodes = JsonUtil.toList(${varName});
        for (int _i = 0; _i < _nodes.size(); _i++) {
            ObjectNode object = JsonUtil.toObject(_nodes.get(_i));
            ${listValueJavaType} model = (${listValueJavaType}) node.${createMethodName}();
            node.${addMethodName}(model);
            this.${readMethodName}(object, model);
        }
        JsonUtil.removeProperty(json, "${propertyName}");
    }
}
""");
        } else {
            prop.getCtx().warn("LIST Entity property '" + property.getName() + "' not read (unsupported) for entity: " + prop.getOwningEntity().fullyQualifiedName());
            prop.getCtx().warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
