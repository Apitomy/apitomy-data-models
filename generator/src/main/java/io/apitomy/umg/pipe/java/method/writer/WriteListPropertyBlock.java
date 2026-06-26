package io.apitomy.umg.pipe.java.method.writer;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
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
 * Generates code to write a list property to JSON (primitive list or entity list).
 */
public class WriteListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));

        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
        if (listValueType.isPrimitiveType()) {
            body.append("JsonUtil.setProperty(json, \"${propertyName}\", JsonUtil.toArrayNode(node.${getterMethodName}()));");
        } else if (listValueType.isEntityType()) {
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), entityModel, ctx, "LIST");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = ctx.resolveCommonJavaEntity(resolved.entityModel());

            writerClassSource.addImport(resolved.javaInterface());
            writerClassSource.addImport(commonEntityTypeJavaModel);
            writerClassSource.addImport(List.class);
            writerClassSource.addImport(ArrayNode.class);

            body.addContext("propertyName", property.getName());
            body.addContext("listValueJavaType", resolved.javaInterface().getName());
            body.addContext("writeMethodName", WriteEntityPropertyBlock.writeMethodName(resolved.entityModel()));
            body.addContext("listValueCommonJavaType", commonEntityTypeJavaModel.getName());

            body.append("{");
            body.append("    List<? extends ${listValueCommonJavaType}> models = node.${getterMethodName}();");
            body.append("    if (models != null && !models.isEmpty()) {");
            body.append("        ArrayNode array = JsonUtil.arrayNode();");
            body.append("        models.forEach(model -> {");
            body.append("            ObjectNode object = JsonUtil.objectNode();");
            body.append("            this.${writeMethodName}((${listValueJavaType}) model, object);");
            body.append("            JsonUtil.addToArray(array, object);");
            body.append("        });");
            body.append("        JsonUtil.setProperty(json, \"${propertyName}\", array);");
            body.append("    }");
            body.append("}");
        } else {
            ctx.warn("LIST Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
