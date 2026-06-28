package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;

/**
 * Generates a factory method: signature, return type, @Override (for impl),
 * and body that creates a new entity impl instance and sets the parent.
 */
public class FactoryMethod implements Method {

    private final JavaSource<?> javaEntity;
    private final String entityName;
    private final CodeGenContext ctx;

    public FactoryMethod(JavaSource<?> javaEntity, String entityName, CodeGenContext ctx) {
        this.javaEntity = javaEntity;
        this.entityName = entityName;
        this.ctx = ctx;
    }

    /**
     * Returns the factory method name for the given entity name.
     */
    public static String methodName(String entityName) {
        return "create" + StringUtils.capitalize(entityName);
    }

    @Override
    public String getName() {
        return methodName(entityName);
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        String methodName = getName();

        // The name of the "create" method is based on the type, so it's possible to have
        // duplicates.  Let's not do that.
        if (ctx.hasNamedMethod(((MethodHolderSource<?>) target), methodName)) {
            return;
        }

        String _package = target.getPackage();
        JavaInterfaceSource entityType = ctx.resolveJavaEntityType(_package, entityName);
        if (entityType == null) {
            ctx.error("Could not resolve entity type: " + _package + "::" + entityName);
            return;
        }

        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setPublic()
                .setName(methodName)
                .setReturnType(entityType);

        if (target instanceof JavaClassSource) {
            method.addAnnotation(Override.class);

            String entityFQN = target.getPackage() + "." + entityName;
            EntityModel entityModel = ctx.getConceptIndex().lookupEntity(entityFQN);
            String implFQN = ctx.getJavaEntityClassFQN(entityModel);

            JavaClassSource entityImpl = ctx.lookupJavaEntityImpl(implFQN);
            if (entityImpl == null) {
                ctx.error("Could not resolve entity type (impl): " + implFQN);
                return;
            }
            target.addImport(entityImpl);

            BodyBuilder body = new BodyBuilder();
            body.addContext("implClass", entityImpl.getName());
            body.appendBlock("""
                    ${implClass} node = new ${implClass}();
                    node._setParent(this);
                    return node;
                    """);
            method.setBody(body.toString());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added during writeTo since we need to resolve the impl first
    }

}
