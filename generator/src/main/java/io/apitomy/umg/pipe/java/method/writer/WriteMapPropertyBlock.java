package io.apitomy.umg.pipe.java.method.writer;

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
 * Generates code to write a map property to JSON (primitive map or entity map).
 */
public class WriteMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
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

        Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();
        if (mapValueType.isPrimitiveType()) {
            WritePrimitivePropertyBlock helper = new WritePrimitivePropertyBlock(propertyWithOrigin, writerClassSource, ctx);
            body.addContext("setPropertyMethodName", helper.determineSetPropertyVariant(property.getResolvedType()));
            writerClassSource.addImport(List.class);

            body.append("JsonUtil.${setPropertyMethodName}(json, \"${propertyName}\", node.${getterMethodName}());");
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            String fqEntityName = entityModel.getNamespace().fullName() + "." + entityTypeName;
            EntityModel entityTypeModel = ctx.getConceptIndex().lookupEntity(fqEntityName);
            if (entityTypeModel == null) {
                ctx.warn("MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                ctx.warn("       property type is entity but not found in index: " + property.getResolvedType());
                return;
            }
            JavaInterfaceSource entityTypeJavaModel = ctx.getJavaIndex().lookupInterface(ctx.getJavaEntityInterfaceFQN(entityTypeModel));
            if (entityTypeJavaModel == null) {
                ctx.warn("MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                ctx.warn("       property type is entity but not found in JAVA index: " + property.getResolvedType());
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = ctx.resolveCommonJavaEntity(entityTypeModel);

            writerClassSource.addImport(Map.class);
            writerClassSource.addImport(entityTypeJavaModel);
            writerClassSource.addImport(commonEntityTypeJavaModel);

            body.addContext("mapValueJavaType", entityTypeJavaModel.getName());
            body.addContext("writeMethodName", "write" + entityTypeName);
            body.addContext("mapValueCommonJavaType", commonEntityTypeJavaModel.getName());

            body.append("{");
            body.append("    Map<String, ? extends ${mapValueCommonJavaType}> models = node.${getterMethodName}();");
            body.append("    if (models != null && !models.isEmpty()) {");
            body.append("        ObjectNode object = JsonUtil.objectNode();");
            body.append("        models.keySet().forEach(jsonName -> {");
            body.append("            ObjectNode jsonValue = JsonUtil.objectNode();");
            body.append("            this.${writeMethodName}((${mapValueJavaType}) models.get(jsonName), jsonValue);");
            body.append("            JsonUtil.setObjectProperty(object, jsonName, jsonValue);");
            body.append("        });");
            body.append("        JsonUtil.setObjectProperty(json, \"${propertyName}\", object);");
            body.append("    }");
            body.append("}");
        } else {
            ctx.warn("MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
