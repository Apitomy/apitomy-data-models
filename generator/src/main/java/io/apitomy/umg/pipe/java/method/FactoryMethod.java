package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;

/**
 * Generates a factory method body: create new entity impl instance and set parent.
 */
public class FactoryMethod implements CanAddImports {

    private final JavaSource<?> javaEntity;
    private final String entityName;
    private final CodeGenContext ctx;

    private JavaClassSource entityImpl;

    public FactoryMethod(JavaSource<?> javaEntity, String entityName, CodeGenContext ctx) {
        this.javaEntity = javaEntity;
        this.entityName = entityName;
        this.ctx = ctx;
    }

    public void writeTo(MethodSource<?> method) {
        String entityFQN = javaEntity.getPackage() + "." + entityName;
        EntityModel entityModel = ctx.getConceptIndex().lookupEntity(entityFQN);
        String implFQN = ctx.getJavaEntityClassFQN(entityModel);

        entityImpl = ctx.lookupJavaEntityImpl(implFQN);
        if (entityImpl == null) {
            ctx.error("Could not resolve entity type (impl): " + implFQN);
            return;
        }
        javaEntity.addImport(entityImpl);

        BodyBuilder body = new BodyBuilder();
        body.addContext("implClass", entityImpl.getName());
        body.appendBlock("""
                ${implClass} node = new ${implClass}();
                node._setParent(this);
                return node;
                """);
        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added during writeTo since we need to resolve the impl first
    }

}
