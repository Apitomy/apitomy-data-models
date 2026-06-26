package io.apitomy.umg.pipe.java.method.writer;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to write an entity-typed property to JSON.
 */
public class WriteEntityPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteEntityPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        String propertyTypeEntityName = entityModel.getNamespace().fullName() + "." + property.getResolvedType().getName();
        EntityModel propertyTypeEntity = ctx.getConceptIndex().lookupEntity(propertyTypeEntityName);
        if (propertyTypeEntity == null) {
            ctx.warn("Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
            return;
        }
        JavaInterfaceSource propertyTypeJavaEntity = ctx.resolveJavaEntityType(entityModel.getNamespace(), property);
        writerClassSource.addImport(propertyTypeJavaEntity);

        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("writeMethodName", writeMethodName(propertyTypeEntity));
        body.addContext("propertyTypeJavaEntity", propertyTypeJavaEntity.getName());

        body.append("{");
        body.append("    if (node.${getterMethodName}() != null) {");
        body.append("        ObjectNode object = JsonUtil.objectNode();");
        body.append("        this.${writeMethodName}((${propertyTypeJavaEntity}) node.${getterMethodName}(), object);");
        body.append("        JsonUtil.setObjectProperty(json, \"${propertyName}\", object);");
        body.append("    }");
        body.append("}");
    }

    static String writeMethodName(EntityModel entityModel) {
        return "write" + StringUtils.capitalize(entityModel.getName());
    }

    static String writeMethodName(String entityName) {
        return "write" + StringUtils.capitalize(entityName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
