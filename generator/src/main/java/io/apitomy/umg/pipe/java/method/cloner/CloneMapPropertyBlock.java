package io.apitomy.umg.pipe.java.method.cloner;

import java.util.LinkedHashMap;
import java.util.List;
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
            body.addContext("valueType", determineValueType(mapValueType));

            body.append("{");
            body.append("    Map<String, ${valueType}> srcMap = source.${getterMethodName}();");
            body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
            body.append("        target.${setterMethodName}(new LinkedHashMap<>(srcMap));");
            body.append("    }");
            body.append("}");
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            String fqEntityName = entityModel.getNamespace().fullName() + "." + entityTypeName;
            EntityModel entityTypeModel = ctx.getConceptIndex().lookupEntity(fqEntityName);
            if (entityTypeModel == null) {
                ctx.warn("MAP Entity property '" + property.getName() + "' not cloned for entity: " + entityModel.fullyQualifiedName());
                return;
            }
            JavaInterfaceSource entityTypeJavaModel = ctx.getJavaIndex().lookupInterface(ctx.getJavaEntityInterfaceFQN(entityTypeModel));
            if (entityTypeJavaModel == null) {
                ctx.warn("MAP Entity property '" + property.getName() + "' not cloned (java) for entity: " + entityModel.fullyQualifiedName());
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = ctx.resolveCommonJavaEntity(entityTypeModel);

            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(entityTypeJavaModel);
            clonerClassSource.addImport(commonEntityTypeJavaModel);

            body.addContext("entityJavaType", entityTypeJavaModel.getName());
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

    private String determineValueType(Type type) {
        if (type.isPrimitiveType()) {
            Class<?> _class = ctx.primitiveTypeToClass(type);
            if (_class != null) {
                clonerClassSource.addImport(_class);
                return _class.getSimpleName();
            }
        }
        return "Object";
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
