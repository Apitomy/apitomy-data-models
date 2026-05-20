package io.apitomy.umg.pipe.concept;

import java.util.stream.Collectors;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Creates a Visitor for each leaf namespace.  Adds all of the entities
 * from that leaf namespace.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateVisitorsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findNamespaces("").stream().filter(ns -> ns.getChildren().isEmpty()).collect(Collectors.toList()).forEach(ns -> {
            debug("Creating a visitor for namespace: %s", ns.fullName());
            VisitorModel visitorModel = VisitorModel.builder().namespace(ns).build();
            visitorModel.getEntities().addAll(ns.getEntities().values().stream().map(entity -> cloneEntity(entity)).collect(Collectors.toSet()));
            ns.setVisitor(visitorModel);
            getState().getConceptIndex().index(visitorModel);

            // Now create ancestor visitors
            NamespaceModel parentNS = ns.getParent();
            while (parentNS != null) {
                // If the parent namespace has at least 2 children, it needs a visitor
                if (parentNS.getChildren().size() > 1) {
                    VisitorModel parentVisitor = parentNS.getVisitor();
                    if (parentVisitor == null) {
                        debug("Creating a visitor for namespace: %s", parentNS.fullName());
                        parentVisitor = VisitorModel.builder().namespace(parentNS).build();
                        parentNS.setVisitor(parentVisitor);
                        getState().getConceptIndex().index(parentVisitor);
                    }
                    visitorModel.setParent(parentVisitor);
                    parentVisitor.getChildren().add(visitorModel);

                    visitorModel = parentVisitor;
                }

                parentNS = parentNS.getParent();
            }
        });
    }

    private EntityModel cloneEntity(EntityModel entity) {
        return EntityModel.builder().name(entity.getName()).build();
    }

}
