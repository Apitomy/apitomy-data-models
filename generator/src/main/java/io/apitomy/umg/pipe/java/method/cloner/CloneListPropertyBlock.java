package io.apitomy.umg.pipe.java.method.cloner;

import java.util.ArrayList;
import java.util.List;

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
 * Generates code to clone a list property (primitive list or entity list).
 */
public class CloneListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource clonerClassSource;
    private final CodeGenContext ctx;

    public CloneListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource clonerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.clonerClassSource = clonerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();

        body.addContext("getterMethodName", ctx.getterMethodName(property));

        if (listValueType.isPrimitiveType()) {
            clonerClassSource.addImport(List.class);
            clonerClassSource.addImport(ArrayList.class);
            body.addContext("setterMethodName", ctx.setterMethodName(property));
            body.addContext("valueType", PrimitiveTypeHelper.determineValueType(listValueType, ctx, clonerClassSource));

            body.append("{");
            body.append("    List<${valueType}> srcList = source.${getterMethodName}();");
            body.append("    if (srcList != null) {");
            body.append("        target.${setterMethodName}(new ArrayList<>(srcList));");
            body.append("    }");
            body.append("}");
        } else if (listValueType.isEntityType()) {
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), entityModel, ctx, "LIST");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = ctx.resolveCommonJavaEntity(resolved.entityModel());
            clonerClassSource.addImport(resolved.javaInterface());
            clonerClassSource.addImport(commonEntityTypeJavaModel);
            clonerClassSource.addImport(List.class);

            body.addContext("entityJavaType", resolved.javaInterface().getName());
            body.addContext("commonEntityType", commonEntityTypeJavaModel.getName());
            body.addContext("createMethodName", ctx.createMethodName(resolved.entityModel()));
            body.addContext("cloneMethodName", CloneEntityPropertyBlock.cloneMethodName(resolved.entityModel()));
            body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));

            body.append("{");
            body.append("    List<? extends ${commonEntityType}> srcList = source.${getterMethodName}();");
            body.append("    if (srcList != null && !srcList.isEmpty()) {");
            body.append("        srcList.forEach(srcItem -> {");
            body.append("            ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();");
            body.append("            this.${cloneMethodName}((${entityJavaType}) srcItem, tgtItem);");
            body.append("            target.${addMethodName}(tgtItem);");
            body.append("        });");
            body.append("    }");
            body.append("}");
        } else {
            ctx.warn("LIST Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + entityModel.fullyQualifiedName());
        }
    }

    

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
