package io.apitomy.umg.pipe.java.method.cloner;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Generates code to clone an entity-typed property: creates the target entity,
 * then recursively clones from source to target.
 */
public class CloneEntityPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    // resolved during appendTo
    private JavaInterfaceSource propertyTypeJavaEntity;

    public CloneEntityPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
        this.prop = prop;
        this.clonerClassSource = clonerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        var resolved = EntityResolver.resolveEntityInterface(prop.getPropertyWithOrigin(), prop.getOwningEntity(), prop.getCtx(), "");
        if (resolved == null) {
            return;
        }
        propertyTypeJavaEntity = resolved.javaInterface();
        clonerClassSource.addImport(propertyTypeJavaEntity);

        body.addContext(Map.of(
                "getterMethodName", prop.getGetterName(),
                "setterMethodName", prop.getSetterName(),
                "createMethodName", new FactoryMethod(resolved.entityModel().getName()).getName(),
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
        return new ClonerMethod(entityModel.getName()).getName();
    }
}
