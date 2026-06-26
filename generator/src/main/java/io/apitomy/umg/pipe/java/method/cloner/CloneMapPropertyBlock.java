package io.apitomy.umg.pipe.java.method.cloner;

import java.util.LinkedHashMap;
import java.util.Map;

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
 * Generates code to clone a map property (primitive map or entity map).
 */
public class CloneMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource clonerClassSource;
    private final CodeGenContext ctx;

    public CloneMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource clonerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.clonerClassSource = clonerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();

        body.addContext("getterMethodName", ctx.getterMethodName(property));

        if (mapValueType.isPrimitiveType()) {
            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(LinkedHashMap.class);
            body.addContext("setterMethodName", ctx.setterMethodName(property));
            body.addContext("valueType", PrimitiveTypeHelper.determineValueType(mapValueType, ctx, clonerClassSource));

            body.append("{");
            body.append("    Map<String, ${valueType}> srcMap = source.${getterMethodName}();");
            body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
            body.append("        target.${setterMethodName}(new LinkedHashMap<>(srcMap));");
            body.append("    }");
            body.append("}");
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, entityModel, ctx, "MAP");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = ctx.resolveCommonJavaEntity(resolved.entityModel());

            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(resolved.javaInterface());
            clonerClassSource.addImport(commonEntityTypeJavaModel);

            body.addContext("entityJavaType", resolved.javaInterface().getName());
            body.addContext("commonEntityType", commonEntityTypeJavaModel.getName());
            body.addContext("createMethodName", "create" + entityTypeName);
            body.addContext("cloneMethodName", "clone" + entityTypeName);
            body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));

            body.append("{");
            body.append("    Map<String, ? extends ${commonEntityType}> srcMap = source.${getterMethodName}();");
            body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
            body.append("        srcMap.keySet().forEach(name -> {");
            body.append("            ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();");
            body.append("            this.${cloneMethodName}((${entityJavaType}) srcMap.get(name), tgtItem);");
            body.append("            target.${addMethodName}(name, tgtItem);");
            body.append("        });");
            body.append("    }");
            body.append("}");
        } else {
            ctx.warn("MAP Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + entityModel.fullyQualifiedName());
        }
    }

    

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
