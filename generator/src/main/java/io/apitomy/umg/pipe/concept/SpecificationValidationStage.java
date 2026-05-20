package io.apitomy.umg.pipe.concept;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.apitomy.umg.beans.Entity;
import io.apitomy.umg.beans.Property;
import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.beans.Trait;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Validate the input specifications.
 * @author eric.wittmann@gmail.com
 */
public class SpecificationValidationStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecifications().forEach(spec -> {
            require(spec.getName(), "Specification missing required property 'name'.");
            require(spec.getNamespace(), "Specification %s missing required property '%s'.", spec.getName(), "namespace");
            require(spec.getPrefix(), "Specification %s missing required property '%s'.", spec.getName(), "prefix");
            require(spec.getVersions(), "Specification %s missing required property '%s'.", spec.getName(), "versions");
        });
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            require(specVer.getName(), "Specification version missing required property 'name'.");
            require(specVer.getNamespace(), "Specification '%s' missing required property 'namespace'.", specVer.getName());
            require(specVer.getPrefix(), "Specification '%s' missing required property 'prefix'.", specVer.getName());
            require(specVer.getVersion(), "Specification '%s' missing required property 'version'.", specVer.getName());

            Set<Trait> traits = specVer.getTraits();
            traits.forEach(trait -> {
                if (trait.getProperties() == null || trait.getProperties().isEmpty()) {
                    fail("Trait '%s' in specification '%s' must have at least one property.", trait.getName(), specVer.getName());
                } else {
                    // TODO validate properties
                }
            });
            Set<Entity> entities = specVer.getEntities();
            entities.forEach(entity -> {
                validateEntityProperties(specVer, entity);
            });
            long numRootEntities = entities.stream().filter(entity -> entity.getRoot() != null && entity.getRoot()).count();
            if (numRootEntities != 1) {
                fail("Specification version '%s' must have one and only one root entity.", specVer.getName());
            }
        });
    }

    private void validateEntityProperties(SpecificationVersion specVer, Entity entity) {
        if (entity.getProperties() == null || entity.getProperties().isEmpty()) {
            fail("Entity '%s' in specification '%s' must have at least one property.", entity.getName(), specVer.getName());
        }
        if (entity.getPropertyOrder() == null || entity.getPropertyOrder().size() == 0) {
            fail("Entity '%s' in specification '%s' is missing its property ordering.", entity.getName(), specVer.getName());
        }

        // Check for any properties that are shaded by a Trait but with a different type.  Traits
        // can shade Entity properties by the types must be exactly the same.
        {
            List<Property> entityProperties = entity.getProperties();
            for (Property entityProperty : entityProperties) {
                List<String> traitNames = entity.getTraits();
                for (String traitName : traitNames ) {
                    Trait trait = specVer.getTraits().stream().filter(t -> t.getName().equals(traitName)).findFirst().get();
                    List<Property> traitProperties = trait.getProperties();
                    for (Property traitProperty : traitProperties) {
                        if (traitProperty.getName().equals(entityProperty.getName())) {
                            if (!entityProperty.getType().equals(traitProperty.getType())) {
                                fail("Entity '%s' in specification '%s' contains a property '%s' that is illegally shaded by trait '%s' (types do not match).",
                                        entity.getName(), specVer.getName(), entityProperty.getName(), traitName);
                            }
                        }
                    }
                }
            }
        }

        // Validate the "propertyOrder" field.
        {
            List<String> propertyOrder = entity.getPropertyOrder();
            List<Property> allProperties = getAllEntityProperties(specVer, entity);
            for (Property property : allProperties) {
                if (!propertyOrder.contains(property.getName())) {
                    fail("Property '%s' from entity '%s' in specification '%s' is missing from propertyOrdering.",
                            property.getName(), entity.getName(), specVer.getName());
                }
            }
            Set<String> allPropertyNames = allProperties.stream().map(p -> p.getName()).collect(Collectors.toUnmodifiableSet());
            for (String pname : propertyOrder) {
                if (!allPropertyNames.contains(pname)) {
                    fail("Extra (unnecessary) property '%s' included in [propertyOrder] of entity '%s' in specification '%s'.",
                            pname, entity.getName(), specVer.getName());
                }
            }
        }
    }

    private List<Property> getAllEntityProperties(SpecificationVersion specVer, Entity entity) {
        List<Property> allProperties = new LinkedList<>();
        allProperties.addAll(entity.getProperties());
        List<String> traits = entity.getTraits();
        for (String traitName : traits) {
            String traitFQN = specVer.getNamespace() + "." + traitName;
            Trait trait = getState().getSpecIndex().getTraitIndex().get(traitFQN);
            allProperties.addAll(trait.getProperties());
        }
        return allProperties;
    }

    private void require(Object propertyValue, String message, Object ... args) {
        if (propertyValue == null) {
            fail(message, args);
        }
    }

    private void fail(String message, Object ... args) {
        String formattedMsg = String.format(message, args);
        throw new RuntimeException(formattedMsg);
    }

}
