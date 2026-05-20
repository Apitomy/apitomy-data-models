package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

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
        createAcceptMethod(entity, javaEntity);
    }

    /**
     * Creates the "accept" method, needed by the Visitable interface that all nodes must
     * implement.
     * @param entity
     * @param javaEntity
     */
    private void createAcceptMethod(EntityModel entity, JavaClassSource javaEntity) {
        VisitorModel visitorModel = getState().getConceptIndex().lookupVisitor(entity.getNamespace().fullName());
        JavaInterfaceSource javaVisitor = lookupJavaVisitor(visitorModel);
        String visitorInterfaceFQN = getRootVisitorInterfaceFQN();
        JavaInterfaceSource javaRootVisitorInterface = getState().getJavaIndex().lookupInterface(visitorInterfaceFQN);

        String methodName = "accept";

        javaEntity.addImport(javaVisitor);
        javaEntity.addImport(javaRootVisitorInterface);
        MethodSource<JavaClassSource> method = javaEntity.addMethod().setPublic().setName(methodName).setReturnTypeVoid();
        method.addParameter(javaRootVisitorInterface.getName(), "visitor");
        method.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.addContext("visitorClass", javaVisitor.getName());
        body.addContext("entityType", entity.getName());
        body.append("${visitorClass} viz = (${visitorClass}) visitor;");
        body.append("viz.visit${entityType}(this);");
        method.setBody(body.toString());
    }

}
