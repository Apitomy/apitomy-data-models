package io.apitomy.umg.pipe.java.method.cloner;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.SetterMethod;

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

        body.addContext(Map.of(
                "getterMethodName", GetterMethod.methodName(property),
                "setterMethodName", SetterMethod.methodName(property),
                "createMethodName", FactoryMethod.methodName(resolved.entityModel().getName()),
                "cloneMethodName", cloneMethodName(resolved.entityModel()),
                "entityType", propertyTypeJavaEntity.getName()
        ));

        body.appendBlock("""
                {
                    if (source.${getterMethodName}() != null) {
                        target.${setterMethodName}(target.${createMethodName}());
                        this.${cloneMethodName}((${entityType}) source.${getterMethodName}(), (${entityType}) target.${getterMethodName}());
                    }
                }
                """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }

    static String cloneMethodName(EntityModel entityModel) {
        return ClonerMethod.methodName(entityModel.getName());
    }
}
