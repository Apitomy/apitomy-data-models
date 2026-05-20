package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

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
        createEmptyCloneMethod(entity, javaEntity);
    }

    /**
     * Creates the "emptyClone" method, needed by the Node interface that all nodes must
     * implement.
     * @param entity
     * @param javaEntity
     */
    private void createEmptyCloneMethod(EntityModel entity, JavaClassSource javaEntity) {
        String nodeFQN = getNodeEntityInterfaceFQN();
        JavaInterfaceSource nodeInterfaceSource = getState().getJavaIndex().lookupInterface(nodeFQN);

        String methodName = "emptyClone";

        MethodSource<JavaClassSource> method = javaEntity.addMethod().setPublic().setName(methodName).setReturnType(nodeInterfaceSource);
        method.addAnnotation(Override.class);
        javaEntity.addImport(nodeInterfaceSource);

        BodyBuilder body = new BodyBuilder();
        body.addContext("implClassName", javaEntity.getName());
        body.append("return new ${implClassName}();");
        method.setBody(body.toString());
    }

}
