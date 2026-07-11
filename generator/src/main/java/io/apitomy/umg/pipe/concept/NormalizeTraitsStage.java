package io.apitomy.umg.pipe.concept;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * This stage is responsible for generating a hierarchy of traits from the
 * collection of traits defined in the various specification configs being
 * processed.  It tries to identify commonalities between all spec versions
 * so that traits can be shared across them.
 */
public class NormalizeTraitsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        // Process every trait model we've created thus far
        Queue<TraitModel> modelsToProcess = new ConcurrentLinkedQueue<>();
        modelsToProcess.addAll(getState().getConceptIndex().findTraits(""));
        Set<String> modelsProcessed = new HashSet<>();

        // Keep working until we've processed every model (including any new models we
        // might create during processing).
        while (!modelsToProcess.isEmpty()) {
            TraitModel traitModel = modelsToProcess.remove();
            if (modelsProcessed.contains(traitModel.fullyQualifiedName())) {
                continue;
            }

            // Check if we need to create a parent trait for this model in any parent scope
            NamespaceModel ancestorNamespaceModel = traitModel.getNamespace().getParent();
            while (ancestorNamespaceModel != null) {
                if (needsParentTrait(ancestorNamespaceModel, traitModel.getName())) {
                    TraitModel ancestorTrait = TraitModel.builder()
                            .name(traitModel.getName())
                            .parent(traitModel.getParent())
                            .namespace(ancestorNamespaceModel)
                            .build();
                    ancestorNamespaceModel.getTraits().put(ancestorTrait.getName(), ancestorTrait);
                    modelsToProcess.add(ancestorTrait);
                    getState().getConceptIndex().index(ancestorTrait);

                    Collection<TraitModel> childTraits = getState().findChildTraitsFor(ancestorTrait);
                    // Make the new parent trait the actual parent of each child trait
                    childTraits.forEach(childTrait -> {
                        childTrait.setParent(ancestorTrait);
                        // Skip processing this model if its turn comes up in the queue.
                        modelsProcessed.add(childTrait.fullyQualifiedName());
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
     * A trait needs a parent trait if there are multiple child subtrees containing
     * a trait with the same name.  Additionally, a parent trait is created at any
     * namespace that has a registered prefix (spec-level or major-version-level namespace)
     * even if only one child subtree contains the trait.  This ensures that spec-level
     * interfaces are always generated even when a spec has only one major version.
     *
     * @param namespaceModel
     * @param traitName
     */
    private boolean needsParentTrait(NamespaceModel namespaceModel, String traitName) {
        int count = 0;
        for (NamespaceModel childNamespace : namespaceModel.getChildren().values()) {
            if (containsTraitRecursive(childNamespace, traitName)) {
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

    private boolean containsTraitRecursive(NamespaceModel namespace, String traitName) {
        if (namespace.containsTrait(traitName)) {
            return true;
        }
        for (NamespaceModel child : namespace.getChildren().values()) {
            if (containsTraitRecursive(child, traitName)) {
                return true;
            }
        }
        return false;
    }

}
