package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.apitomy.umg.models.concept.PropertyModelWithOrigin;

public abstract class AbstractUnionTypeJavaStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        Set<PropertyModelWithOrigin> unionProperties = new HashSet<>();
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entity);
            unionProperties.addAll(allProperties.stream().filter(property ->
                isUnion(property.getProperty()) || isUnionList(property.getProperty()) || isUnionMap(property.getProperty())
            ).collect(Collectors.toSet()));
        });

        unionProperties.forEach(property -> {
            doProcess(property);
        });
    }

    protected abstract void doProcess(PropertyModelWithOrigin property);

}
