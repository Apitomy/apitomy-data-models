package io.apitomy.umg.pipe.java.method.reader;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;

/**
 * Generates code to read an entity-typed property from JSON.
 */
public class ReadEntityPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadEntityPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        var resolved = EntityResolver.resolveEntityInterface(propertyWithOrigin, entityModel, ctx, "");
        if (resolved == null) {
            return;
        }
        readerClassSource.addImport(resolved.javaInterface());
        readerClassSource.addImport(JsonNode.class);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "setterMethodName", ctx.setterMethodName(property),
                "createMethodName", ctx.createMethodName(resolved.entityModel()),
                "getterMethodName", ctx.getterMethodName(property),
                "readMethodName", ctx.readMethodName(resolved.entityModel()),
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
