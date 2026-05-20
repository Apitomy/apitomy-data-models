package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.VisitorModel;

/**
 * Creates the visitor interfaces.  There is hierarchy of visitors that is similar to the
 * hierarchy of data models but slightly different.  The visitor hieararchy is determined
 * as part of the concept phase.  This class simply generates Java interfaces from that
 * visitor concept hierarchy.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateVisitorInterfacesStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findVisitors("").stream().filter(visitor -> visitor.getParent() == null).forEach(visitor -> {
            createVisitorInterfaces(visitor, null);
        });
    }

    /**
     * Creates visitor interfaces for all visitors in a hierarchy, starting with the
     * given root visitor model.
     *
     * @param visitor
     * @param parentVisitorInterface
     */
    private void createVisitorInterfaces(VisitorModel visitor, JavaInterfaceSource parentVisitorInterface) {
        debug("Creating interface for: " + visitor.toString());
        String visitorPackageName = getVisitorInterfacePackageName(visitor);
        String visitorInterfaceName = getVisitorInterfaceName(visitor);

        // Create the visitor interface
        JavaInterfaceSource visitorInterfaceSource = Roaster.create(JavaInterfaceSource.class)
                .setPackage(visitorPackageName)
                .setName(visitorInterfaceName)
                .setPublic();
        if (parentVisitorInterface != null) {
            visitorInterfaceSource.addImport(parentVisitorInterface);
            visitorInterfaceSource.addInterface(parentVisitorInterface.getName());
        }

        // Now create visitXyz methods for each entity in the visitor
        visitor.getEntities().forEach(entity -> {
            createVisitMethodFor(visitorInterfaceSource, entity);
        });

        getState().getJavaIndex().index(visitorInterfaceSource);

        // Now recursively process all the children of this visitor.
        visitor.getChildren().forEach(childVisitor -> {
            createVisitorInterfaces(childVisitor, visitorInterfaceSource);
        });
    }

    /**
     * Creates the readXyz() method for the given entity.
     *
     * @param visitorInterfaceSource
     * @param entity
     */
    private void createVisitMethodFor(JavaInterfaceSource visitorInterfaceSource, EntityModel entity) {
        String visitMethodName = "visit" + entity.getName();

        JavaInterfaceSource javaEntityModel = findRootJavaEntity(entity);
        if (javaEntityModel == null) {
            warn("Java entity not found for: " + entity.fullyQualifiedName());
            return;
        }

        visitorInterfaceSource.addImport(javaEntityModel);

        MethodSource<JavaInterfaceSource> methodSource = visitorInterfaceSource.addMethod()
                .setName(visitMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(javaEntityModel.getName(), "node");
    }

    private JavaInterfaceSource findRootJavaEntity(EntityModel entity) {
        while (entity.getParent() != null) {
            entity = (EntityModel) entity.getParent();
        }
        return getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(entity));
    }
}
