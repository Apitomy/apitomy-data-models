package io.apitomy.umg.pipe.java.method.reader;

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

        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));
        body.addContext("createMethodName", ctx.createMethodName(resolved.entityModel()));
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("readMethodName", ctx.readMethodName(resolved.entityModel()));
        body.addContext("propertyEntityType", resolved.javaInterface().getName());
        body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

        body.append("{");
        body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
        body.append("    if (JsonUtil.isObject(${varName})) {");
        body.append("        node.${setterMethodName}(node.${createMethodName}());");
        body.append("        ${readMethodName}((ObjectNode) ${varName}, (${propertyEntityType}) node.${getterMethodName}());");
        body.append("        json.remove(\"${propertyName}\");");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
