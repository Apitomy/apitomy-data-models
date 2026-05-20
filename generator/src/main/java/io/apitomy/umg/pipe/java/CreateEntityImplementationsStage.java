package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates the java implementation classes for all leaf entities.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateEntityImplementationsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createEntityImpl(entity);
        });
    }

    private void createEntityImpl(EntityModel entity) {
        String _package = getJavaEntityClassPackage(entity);
        String name = getJavaEntityClassName(entity);

        JavaClassSource entityClass = Roaster.create(JavaClassSource.class)
                .setPackage(_package)
                .setName(name)
                .setPublic();

        JavaInterfaceSource entityInterface = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(entity));
        entityClass.addImport(entityInterface);
        entityClass.addInterface(entityInterface);

        if (entity.isRoot()) {
            // Root entities must extends RootNodeImpl
            JavaClassSource rootNodeImpl = getState().getJavaIndex().lookupClass(getRootNodeEntityClassFQN());
            entityClass.addImport(rootNodeImpl);
            entityClass.extendSuperType(rootNodeImpl);

            // Root entities need a default constructor that passes in the right model type
            JavaEnumSource modelTypeEnum = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
            entityClass.addImport(modelTypeEnum);

            MethodSource<JavaClassSource> entityConstructor = entityClass.addMethod().setPublic().setConstructor(true);
            String prefix = getPrefix(entity.getNamespace());
            String modelType = prefixToModelType(prefix);
            BodyBuilder body = new BodyBuilder();
            body.addContext("modelType", modelType);
            body.append("super(ModelType.${modelType});");
            entityConstructor.setBody(body.toString());
        } else {
            // All impl classes extend NodeImpl
            JavaClassSource nodeImpl = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
            entityClass.addImport(nodeImpl);
            entityClass.extendSuperType(nodeImpl);
        }

        getState().getJavaIndex().index(entityClass);
    }
}
