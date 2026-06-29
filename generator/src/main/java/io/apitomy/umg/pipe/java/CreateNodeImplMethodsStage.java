package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.java.method.AcceptMethod;
import io.apitomy.umg.pipe.java.method.EmptyCloneMethod;

/**
 * Creates the "accept" and "emptyClone" methods on all leaf entity implementation classes.
 * These are required by the Visitable and Node interfaces respectively.
 */
public class CreateNodeImplMethodsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream()
                .filter(EntityModel::isLeaf)
                .forEach(this::createMethods);
    }

    private void createMethods(EntityModel entity) {
        JavaClassSource javaEntity = lookupJavaEntityImpl(entity);

        VisitorModel visitorModel = getState().getConceptIndex()
                .lookupVisitor(entity.getNamespace().fullName());
        JavaInterfaceSource javaVisitor = lookupJavaVisitor(visitorModel);
        JavaInterfaceSource javaRootVisitor = getState().getJavaIndex()
                .lookupInterface(getRootVisitorInterfaceFQN());
        new AcceptMethod(entity.getName(), javaVisitor, javaRootVisitor).writeTo(javaEntity);

        JavaInterfaceSource nodeInterface = getState().getJavaIndex()
                .lookupInterface(getNodeEntityInterfaceFQN());
        new EmptyCloneMethod(javaEntity.getName(), nodeInterface).writeTo(javaEntity);
    }
}
