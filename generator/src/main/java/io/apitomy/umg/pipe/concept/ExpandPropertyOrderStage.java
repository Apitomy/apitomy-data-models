package io.apitomy.umg.pipe.concept;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import io.apitomy.umg.beans.Entity;
import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.beans.Trait;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Resolves the "propertyOrder" for each entity.  The values for propertyOrder might be
 * property names (no need to resolve) or they might be of the form "$this" or "$Entity".
 * In those cases, we should expand/resolve the directive into the properties based on
 * the order the actual properties are defined in the source config file.
 */
public class ExpandPropertyOrderStage extends AbstractStage {
    @Override
    protected void doProcess() {
        info("-- Resolving/Expanding Entity Property Order --");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            specVer.getEntities().forEach(entity -> {
                List<String> order = entity.getPropertyOrder();
                ListIterator<String> orderIterator = order.listIterator();
                while (orderIterator.hasNext()) {
                    String propertyName = orderIterator.next();
                    if (propertyName.startsWith("$")) {
                        List<String> expandedPropertyNames = expand(specVer, entity, propertyName);
                        orderIterator.remove();
                        expandedPropertyNames.forEach(name -> {
                            orderIterator.add(name);
                        });
                    }
                }
            });
        });
    }

    private List<String> expand(SpecificationVersion specVer, Entity entity, String directive) {
        String name = directive.substring(1);
        if ("this".equals(name)) {
            String entityFQN = specVer.getNamespace() + "." + entity.getName();
            Entity e = getState().getSpecIndex().getEntityIndex().get(entityFQN);
            return e.getProperties().stream().map(p -> p.getName()).collect(Collectors.toUnmodifiableList());
        } else {
            if (!entity.getTraits().contains(name)) {
                throw new RuntimeException(
                        "Property ordering for '" + specVer.getNamespace() + "." + entity.getName() +
                        "' refers to trait '" + name + "' that does not belong to entity.");
            }
            String traitFQN = specVer.getNamespace() + "." + name;
            Trait t = getState().getSpecIndex().getTraitIndex().get(traitFQN);
            return t.getProperties().stream().map(p -> p.getName()).collect(Collectors.toUnmodifiableList());
        }
    }
}
