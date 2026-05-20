package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.models.concept.PropertyModelWithOriginComparator;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Create a property comparator (used when ordering properties of an entity) for each leaf entity.
 * @author eric.wittmann@gmail.com
 */
public class CreatePropertyComparatorStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            PropertyModelWithOriginComparator comparator = new PropertyModelWithOriginComparator(entity.getPropertyOrder());
            getState().getConceptIndex().index(entity, comparator);
        });
    }

}
