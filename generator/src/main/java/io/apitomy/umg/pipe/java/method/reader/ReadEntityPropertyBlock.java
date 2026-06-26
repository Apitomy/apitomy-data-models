package io.apitomy.umg.pipe.java.method.reader;

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
        String propertyTypeEntityName = entityModel.getNamespace().fullName() + "." + property.getResolvedType().getName();
        EntityModel propertyTypeEntity = ctx.getConceptIndex().lookupEntity(propertyTypeEntityName);
        if (propertyTypeEntity == null) {
            ctx.warn("Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
            return;
        }
        JavaInterfaceSource propertyTypeJavaEntity = ctx.resolveJavaEntityType(entityModel.getNamespace(), property);
        readerClassSource.addImport(propertyTypeJavaEntity);

        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));
        body.addContext("createMethodName", ctx.createMethodName(propertyTypeEntity));
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("readMethodName", ctx.readMethodName(propertyTypeEntity));
        body.addContext("propertyEntityType", propertyTypeJavaEntity.getName());

        body.append("{");
        body.append("    ObjectNode object = JsonUtil.consumeObjectProperty(json, \"${propertyName}\");");
        body.append("    if (object != null) {");
        body.append("        node.${setterMethodName}(node.${createMethodName}());");
        body.append("        ${readMethodName}(object, (${propertyEntityType}) node.${getterMethodName}());");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
