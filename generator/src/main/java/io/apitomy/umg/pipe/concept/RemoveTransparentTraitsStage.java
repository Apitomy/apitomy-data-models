package io.apitomy.umg.pipe.concept;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Removes (aka inlines) any transparent traits.  This essentially copies all of the properties from
 * the trait to the entity and removes the trait from the entity.
 */
public class RemoveTransparentTraitsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        Set<TraitModel> traitsToRemove = new HashSet<>();
        getState().getConceptIndex().findEntities("").forEach(entity -> {
            entity.getTraits().stream().filter(t -> t.isTransparent()).collect(Collectors.toSet()).forEach(trait -> {
                // Copy all properties from the trait to the entity.
                entity.getProperties().putAll(trait.getProperties());
                // Remove trait from entity
                entity.getTraits().remove(trait);

                traitsToRemove.add(trait);
            });
        });

        // All transparent traits are now inlined. Remove them from the index
        // as they are no longer needed.
        traitsToRemove.forEach(trait -> {
            trait.getNamespace().getTraits().remove(trait.getName());
            getState().getConceptIndex().remove(trait);
        });
    }

}
