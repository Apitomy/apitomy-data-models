package io.apitomy.umg.pipe.concept;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.AbstractStage;

public class NormalizeVisitorsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        // Normalize the visitors
        List<VisitorModel> branchVisitors = getState().getConceptIndex().findVisitors("").stream().filter(model -> model.hasChildren()).collect(Collectors.toList());
        int changesMade;
        do {
            changesMade = 0;
            for (VisitorModel branchVisitor : branchVisitors) {
                // Get all direct children of this parent entity.
                Collection<VisitorModel> childVisitors = branchVisitor.getChildren();

                // Get a collection of all entities for all the children
                Set<EntityModel> allEntities = new HashSet<>();
                childVisitors.forEach(child -> allEntities.addAll(child.getEntities()));

                // Filter the full list of entities - only keep the entities that exist in *all* children.
                List<EntityModel> entitiesToPullup = allEntities.stream()
                        .filter(entity -> existsInAllChildren(entity, childVisitors))
                        .collect(Collectors.toList());

                // Now pull up each of the entities in the above list
                entitiesToPullup.forEach(entity -> {
                    branchVisitor.addEntity(entity);
                    childVisitors.forEach(childVisitor -> childVisitor.removeEntity(entity.getName()));
                });

                // Did we find any entities to pull up?  If yes, increment "changes made".  We're going to keep
                // going through our visitor hierarchy until we've pulled up all the entities we can.
                changesMade += entitiesToPullup.size();
            }
        } while (changesMade > 0);

        // Ensure that there is a singular shared common visitor at the root namespace.  Depending
        // on the inputs, this might already exists.  But let's make sure.
        String rootNamespace = getState().getConfig().getRootNamespace();
        VisitorModel rootVisitor = getState().getConceptIndex().lookupVisitor(rootNamespace);
        if (rootVisitor == null) {
            createRootVisitor(rootNamespace);
        }

        // Now that the entities in the visitors are normalized, make sure each entity
        // has an appropriate namespace set.
        getState().getConceptIndex().findVisitors("").forEach(visitor -> {
            visitor.getEntities().forEach(entity -> {
                entity.setNamespace(visitor.getNamespace());
            });
        });
    }

    /**
     * Creates a visitor in the root namespace.  Ensures that all other unparented visitors
     * are parented by this new root visitor.
     * @param rootNamespace
     */
    private void createRootVisitor(String rootNamespace) {
        NamespaceModel rootNS = getState().getConceptIndex().lookupNamespace(rootNamespace);
        if (rootNS == null) {
            throw new RuntimeException("Root namespace not found: " + rootNamespace);
        }
        VisitorModel rootVisitor = VisitorModel.builder().namespace(rootNS).build();
        getState().getConceptIndex().findVisitors("").stream().filter(visitor -> visitor.getParent() == null).forEach(visitor -> {
            visitor.setParent(rootVisitor);
            rootVisitor.getChildren().add(visitor);
        });
        getState().getConceptIndex().index(rootVisitor);
    }

    /**
     * Returns true if the given entity exists in *all* of the given visitors.
     * @param entity
     * @param childVisitors
     */
    private boolean existsInAllChildren(EntityModel entity, Collection<VisitorModel> childVisitors) {
        return childVisitors.stream().map(visitor -> visitor.containsEntity(entity.getName())).reduce(true, (sub, element) -> sub && element);
    }

}
