package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;

/**
 * Creates the java interfaces for all entities.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateEntityInterfacesStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").forEach(entity -> {
            createEntityInterface(entity);
        });
    }

    private void createEntityInterface(EntityModel entity) {
        String _package = getJavaEntityInterfacePackage(entity);
        String name = getJavaEntityInterfaceName(entity);

        JavaInterfaceSource entityInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(name)
                .setPublic();

        getState().getJavaIndex().index(entityInterface);

        // If the entity is a root, it must extend the RootNode interface
        if (entity.isRoot()) {
            String rootNodeFQN = getRootNodeInterfaceFQN();
            JavaInterfaceSource rootNodeInterfaceSource = getState().getJavaIndex().lookupInterface(rootNodeFQN);
            entityInterface.addImport(rootNodeInterfaceSource);
            entityInterface.addInterface(rootNodeInterfaceSource);
        }
    }
}