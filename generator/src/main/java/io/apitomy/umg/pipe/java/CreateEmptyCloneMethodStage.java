package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.pipe.java.method.EmptyCloneMethod;

/**
 * Creates the "emptyClone" method for all entity implementations.  This is required by the
 * Node interface (all model nodes must implement it).
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateEmptyCloneMethodStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createEmptyCloneMethod(entity);
        });
    }

    private void createEmptyCloneMethod(EntityModel entity) {
        JavaClassSource javaEntity = lookupJavaEntityImpl(entity);

        String nodeFQN = getNodeEntityInterfaceFQN();
        JavaInterfaceSource nodeInterfaceSource = getState().getJavaIndex().lookupInterface(nodeFQN);

        new EmptyCloneMethod(javaEntity.getName(), nodeInterfaceSource).writeTo(javaEntity);
    }

}
