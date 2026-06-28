package io.apitomy.umg.pipe.java.method.reader;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Generates code to read a primitive-typed property from JSON.
 */
public class ReadPrimitivePropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadPrimitivePropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        readerClassSource.addImport(JsonNode.class);

        String isCheckMethod = PrimitiveTypeHelper.determineIsCheckMethod(property.getResolvedType(), prop.getCtx(), readerClassSource);
        String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(property.getResolvedType(), prop.getCtx(), readerClassSource);
        body.addContext(Map.of(
                "propertyName", property.getName(),
                "setterMethodName", prop.getSetterName(),
                "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"),
                "isCheckMethod", isCheckMethod,
                "toConversionMethod", toConversionMethod
        ));

        body.appendBlock("""
{
    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
    if (JsonUtil.${isCheckMethod}(${varName})) {
        node.${setterMethodName}(JsonUtil.${toConversionMethod}(${varName}));
        JsonUtil.removeProperty(json, "${propertyName}");
    }
}
""");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
