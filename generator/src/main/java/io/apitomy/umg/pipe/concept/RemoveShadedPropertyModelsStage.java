package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.AbstractStage;

import java.util.Set;

/**
 * Removes any shaded property models from their entities.  A shaded property is one
 * that is defined on an Entity but *also* defined on a Trait of that Entity.  This
 * is redundant and only supported to make property ordering easier in the spec
 * definitions (this allows you to define a property in both a Trait and an Entity -
 * only the Trait property will be used when generating code, but the property in the
 * Entity will be used for ordering).
 */
public class RemoveShadedPropertyModelsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        info("-- Removing shaded Entity properties --");
        getState().getConceptIndex().getAllEntitiesWithCopy().forEach(entity -> {
            Set<String> propertyNames = Set.copyOf(entity.getProperties().keySet());
            for (String propertyName : propertyNames) {
                PropertyModel propertyModel = entity.getProperties().get(propertyName);
                if (propertyModel.isShaded()) {
                    entity.getProperties().remove(propertyName);
                }
            }
        });
    }

}
