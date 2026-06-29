package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.java.method.AcceptMethod;

/**
 * Creates the "accept" method for all entity implementations.  This is required by the
 * Visitable interface that all nodes must implement.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateAcceptMethodStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createAcceptMethod(entity);
        });
    }

    private void createAcceptMethod(EntityModel entity) {
        JavaClassSource javaEntity = lookupJavaEntityImpl(entity);

        VisitorModel visitorModel = getState().getConceptIndex().lookupVisitor(entity.getNamespace().fullName());
        JavaInterfaceSource javaVisitor = lookupJavaVisitor(visitorModel);
        String visitorInterfaceFQN = getRootVisitorInterfaceFQN();
        JavaInterfaceSource javaRootVisitorInterface = getState().getJavaIndex().lookupInterface(visitorInterfaceFQN);

        new AcceptMethod(entity.getName(), javaVisitor, javaRootVisitorInterface).writeTo(javaEntity);
    }

}
