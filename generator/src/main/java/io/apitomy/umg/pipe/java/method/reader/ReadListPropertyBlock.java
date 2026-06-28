package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.ReaderMethod;
import io.apitomy.umg.pipe.java.method.SetterMethod;

/**
 * Generates code to read a list property from JSON (primitive list or entity list).
 */
public class ReadListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
        if (listValueType.isPrimitiveType()) {
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(List.class);
            readerClassSource.addImport(ArrayList.class);

            String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(listValueType, ctx);
            String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(listValueType, ctx, readerClassSource);
            String elementValueType = PrimitiveTypeHelper.determineValueType(listValueType, ctx, readerClassSource);
            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "setterMethodName", SetterMethod.methodName(property),
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
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), entityModel, ctx, "LIST");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(List.class);

            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "setterMethodName", SetterMethod.methodName(property),
                    "listValueJavaType", resolved.javaInterface().getName(),
                    "createMethodName", FactoryMethod.methodName(resolved.entityModel().getName()),
                    "readMethodName", ReaderMethod.methodName(resolved.entityModel().getName()),
                    "addMethodName", AddMethod.methodName(ctx.singularize(property.getName())),
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
            ctx.warn("LIST Entity property '" + property.getName() + "' not read (unsupported) for entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
