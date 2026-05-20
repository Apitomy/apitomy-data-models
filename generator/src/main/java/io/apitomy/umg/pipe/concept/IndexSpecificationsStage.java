package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.pipe.AbstractStage;

public class IndexSpecificationsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getSpecifications().forEach(spec -> getState().getSpecIndex().index(spec));
    }

}
