package io.apitomy.umg.pipe.java.method.reader;

import java.util.List;

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
        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));

        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
        if (listValueType.isPrimitiveType()) {
            body.addContext("consumeMethodName", PrimitiveTypeHelper.determineConsumePropertyVariant(property.getResolvedType(), ctx, readerClassSource));
            body.addContext("propertyValueJavaType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), ctx, readerClassSource));
            readerClassSource.addImport(List.class);

            body.append("{");
            body.append("    ${propertyValueJavaType} value = JsonUtil.${consumeMethodName}(json, \"${propertyName}\");");
            body.append("    node.${setterMethodName}(value);");
            body.append("}");
        } else if (listValueType.isEntityType()) {
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), entityModel, ctx, "LIST");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());
            readerClassSource.addImport(List.class);

            body.addContext("listValueJavaType", resolved.javaInterface().getName());
            body.addContext("createMethodName", ctx.createMethodName(resolved.entityModel()));
            body.addContext("readMethodName", ctx.readMethodName(resolved.entityModel()));
            body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));

            body.append("{");
            body.append("    List<ObjectNode> objects = JsonUtil.consumeObjectArrayProperty(json, \"${propertyName}\");");
            body.append("    if (objects != null) {");
            body.append("        objects.forEach(object -> {");
            body.append("            ${listValueJavaType} model = (${listValueJavaType}) node.${createMethodName}();");
            body.append("            node.${addMethodName}(model);");
            body.append("            this.${readMethodName}(object, model);");
            body.append("        });");
            body.append("    }");
            body.append("}");
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
