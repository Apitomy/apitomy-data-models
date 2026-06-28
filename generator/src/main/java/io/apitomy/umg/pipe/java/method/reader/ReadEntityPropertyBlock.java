package io.apitomy.umg.pipe.java.method.reader;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read an entity-typed property from JSON.
 */
public class ReadEntityPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadEntityPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        var resolved = EntityResolver.resolveEntityInterface(prop.getPropertyWithOrigin(), prop.getOwningEntity(), prop.getCtx(), "");
        if (resolved == null) {
            return;
        }
        readerClassSource.addImport(resolved.javaInterface());
        readerClassSource.addImport(JsonNode.class);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "setterMethodName", prop.getSetterName(),
                "createMethodName", FactoryMethod.methodName(resolved.entityModel().getName()),
                "getterMethodName", prop.getGetterName(),
                "readMethodName", ReaderMethod.methodName(resolved.entityModel().getName()),
                "propertyEntityType", resolved.javaInterface().getName(),
                "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
        ));

        body.appendBlock("""
{
    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
    if (JsonUtil.isObject(${varName})) {
        node.${setterMethodName}(node.${createMethodName}());
        ${readMethodName}(JsonUtil.toObject(${varName}), (${propertyEntityType}) node.${getterMethodName}());
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
