package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.UnionType;

public abstract class AbstractUnionTypeJavaStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        Set<PropertyModelWithOrigin> unionProperties = new LinkedHashSet<>();
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entity);
            unionProperties.addAll(allProperties.stream().filter(property ->
                isUnion(property.getProperty()) || isUnionList(property.getProperty()) || isUnionMap(property.getProperty())
                || hasResolvedUnionType(property)
            ).collect(Collectors.toCollection(LinkedHashSet::new)));
        });

        unionProperties.forEach(property -> {
            doProcess(property);
        });
    }

    protected abstract void doProcess(PropertyModelWithOrigin property);

    private boolean hasResolvedUnionType(PropertyModelWithOrigin pwo) {
        var resolved = pwo.getProperty().getResolvedType();
        if (resolved instanceof UnionType) return true;
        if (resolved instanceof CollectionType ct) {
            return ct.getValueType() instanceof UnionType;
        }
        return false;
    }

}
