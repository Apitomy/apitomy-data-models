package io.apitomy.umg.pipe.concept;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.pipe.AbstractStage;

public class NormalizeEntitiesStage extends AbstractStage {

    @Override
    protected void doProcess() {
        // Process every entity model we've created thus far
        Queue<EntityModel> modelsToProcess = new ConcurrentLinkedQueue<>();
        modelsToProcess.addAll(getState().getConceptIndex().findEntities(""));
        Set<String> modelsProcessed = new HashSet<>();

        // Keep working until we've processed every model (including any new models we
        // might create during processing).
        while (!modelsToProcess.isEmpty()) {
            EntityModel traitModel = modelsToProcess.remove();
            if (modelsProcessed.contains(traitModel.fullyQualifiedName())) {
                continue;
            }

            // Check if we need to create a parent entity for this model in any parent scope
            NamespaceModel ancestorNamespaceModel = traitModel.getNamespace().getParent();
            while (ancestorNamespaceModel != null) {
                if (needsParentEntity(ancestorNamespaceModel, traitModel.getName())) {
                    EntityModel ancestorEntity = EntityModel.builder()
                            .name(traitModel.getName())
                            .parent(traitModel.getParent())
                            .namespace(ancestorNamespaceModel)
                            .build();
                    ancestorNamespaceModel.getEntities().put(ancestorEntity.getName(), ancestorEntity);
                    modelsToProcess.add(ancestorEntity);
                    getState().getConceptIndex().index(ancestorEntity);

                    Collection<EntityModel> childEntities = getState().findChildEntitiesFor(ancestorEntity);
                    // Make the new parent entity the actual parent of each child entity
                    childEntities.forEach(childEntity -> {
                        childEntity.setParent(ancestorEntity);
                        // Skip processing this model if its turn comes up in the queue.
                        modelsProcessed.add(childEntity.fullyQualifiedName());
                    });
                    // break out of loop - no need to search further up the hierarchy
                    ancestorNamespaceModel = null;
                } else {
                    ancestorNamespaceModel = ancestorNamespaceModel.getParent();
                }
            }
        }
    }

    /**
     * An entity needs a parent entity if there are multiple child subtrees containing
     * an entity with the same name.  Additionally, a parent entity is created at any
     * namespace that has a registered prefix (spec-level or major-version-level namespace)
     * even if only one child subtree contains the entity.  This ensures that spec-level
     * interfaces are always generated (e.g. OpenRpcDocument) even when a spec has only
     * one major version.
     *
     * @param namespaceModel
     * @param entityName
     */
    private boolean needsParentEntity(NamespaceModel namespaceModel, String entityName) {
        int count = 0;
        for (NamespaceModel childNamespace : namespaceModel.getChildren().values()) {
            if (containsEntityRecursive(childNamespace, entityName)) {
                count++;
            }
        }
        if (count > 1) {
            return true;
        }
        if (count == 1 && hasRegisteredPrefix(namespaceModel)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the given namespace has a registered prefix in the specification index,
     * indicating it is a spec-level or major-version-level namespace.
     */
    private boolean hasRegisteredPrefix(NamespaceModel namespaceModel) {
        return getState().getSpecIndex().getNsToPrefix().containsKey(namespaceModel.fullName());
    }

    private boolean containsEntityRecursive(NamespaceModel namespace, String entityName) {
        if (namespace.containsEntity(entityName)) {
            return true;
        }
        for (NamespaceModel child : namespace.getChildren().values()) {
            if (containsEntityRecursive(child, entityName)) {
                return true;
            }
        }
        return false;
    }

}
