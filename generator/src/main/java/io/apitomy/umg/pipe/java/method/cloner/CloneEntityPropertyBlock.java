package io.apitomy.umg.pipe.java.method.cloner;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;

/**
 * Generates code to clone an entity-typed property: creates the target entity,
 * then recursively clones from source to target.
 */
public class CloneEntityPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource clonerClassSource;
    private final CodeGenContext ctx;

    // resolved during appendTo
    private JavaInterfaceSource propertyTypeJavaEntity;

    public CloneEntityPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource clonerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.clonerClassSource = clonerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        var resolved = EntityResolver.resolveEntityInterface(propertyWithOrigin, entityModel, ctx, "");
        if (resolved == null) {
            return;
        }
        propertyTypeJavaEntity = resolved.javaInterface();
        clonerClassSource.addImport(propertyTypeJavaEntity);

        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("setterMethodName", ctx.setterMethodName(property));
        body.addContext("createMethodName", ctx.createMethodName(resolved.entityModel()));
        body.addContext("cloneMethodName", cloneMethodName(resolved.entityModel()));
        body.addContext("entityType", propertyTypeJavaEntity.getName());

        body.append("{");
        body.append("    if (source.${getterMethodName}() != null) {");
        body.append("        target.${setterMethodName}(target.${createMethodName}());");
        body.append("        this.${cloneMethodName}((${entityType}) source.${getterMethodName}(), (${entityType}) target.${getterMethodName}());");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }

    static String cloneMethodName(EntityModel entityModel) {
        return "clone" + entityModel.getName();
    }
}
