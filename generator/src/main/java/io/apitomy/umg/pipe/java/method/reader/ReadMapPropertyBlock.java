package io.apitomy.umg.pipe.java.method.reader;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;

/**
 * Generates code to read a map property from JSON (primitive map or entity map).
 */
public class ReadMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));

        Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();
        if (mapValueType.isPrimitiveType()) {
            body.addContext("consumeMethodName", PrimitiveTypeHelper.determineConsumePropertyVariant(property.getResolvedType(), ctx, readerClassSource));
            body.addContext("propertyValueJavaType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), ctx, readerClassSource));
            readerClassSource.addImport(Map.class);

            body.append("{");
            body.append("    ${propertyValueJavaType} value = JsonUtil.${consumeMethodName}(json, \"${propertyName}\");");
            body.append("    node.${setterMethodName}(value);");
            body.append("}");
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, entityModel, ctx, "MAP");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());

            body.addContext("mapValueJavaType", resolved.javaInterface().getName());
            body.addContext("createMethodName", "create" + entityTypeName);
            body.addContext("readMethodName", "read" + entityTypeName);
            body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));

            body.append("{");
            body.append("    ObjectNode object = JsonUtil.consumeObjectProperty(json, \"${propertyName}\");");
            body.append("    JsonUtil.keys(object).forEach(name -> {");
            body.append("        ObjectNode mapValue = JsonUtil.consumeObjectProperty(object, name);");
            body.append("        if (mapValue != null) {");
            body.append("            ${mapValueJavaType} model = (${mapValueJavaType}) node.${createMethodName}();");
            body.append("            node.${addMethodName}(name, model);");
            body.append("            this.${readMethodName}(mapValue, model);");
            body.append("        }");
            body.append("    });");
            body.append("}");
        } else {
            ctx.warn("MAP Entity property '" + property.getName() + "' not read (unsupported) for entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
