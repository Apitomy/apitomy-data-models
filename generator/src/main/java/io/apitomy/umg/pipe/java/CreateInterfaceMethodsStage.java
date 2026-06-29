package io.apitomy.umg.pipe.java;

import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.java.method.MappedNodeMethods;

/**
 * Adds methods to all entity interfaces. This works by finding all the properties for the entity and then
 * deciding what methods should exist on the entity interface based on the name and type of the property.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateInterfaceMethodsStage extends AbstractCreateMethodsStage {

    @Override
    protected void doProcess() {
        initContext();
        getState().getConceptIndex().findEntities("").forEach(entity -> {
            createEntityInterfaceMethods(entity);
        });
        getState().getConceptIndex().findTraits("").forEach(trait -> {
            createTraitInterfaceMethods(trait);
        });
    }

    private void createEntityInterfaceMethods(EntityModel entity) {
        Set<String> createdProperties = new HashSet<>();

        JavaInterfaceSource javaEntity = lookupJavaEntity(entity);
        entity.getProperties().values().forEach(property -> {
            createPropertyMethods(javaEntity,
                    PropertyModelWithOrigin.builder().property(property).origin(entity).build());
            createdProperties.add(property.getName());
        });
        // If this is a leaf entity, we need to redeclare all Trait based properties on the
        // entity's core interface.  This will be redundant (those same methods will be
        // declared on the Trait interfaces), but will make it more convenient to access
        // properties that make up the entity.  In other words, you will always be able to
        // access a property of a leaf Entity even if the property comes from one of its
        // Traits, without casting as the Trait.
        if (entity.isLeaf()) {
            getState().getConceptIndex().getEntityPropertiesFromTraits(entity).forEach(property -> {
                if (!createdProperties.contains(property.getProperty().getName())) {
                    createPropertyMethods(javaEntity, property);
                    createdProperties.add(property.getProperty().getName());
                }
            });
        }
    }

    private void createTraitInterfaceMethods(TraitModel trait) {
        trait.getProperties().values().forEach(property -> {
            createPropertyMethods(lookupJavaTrait(trait),
                    PropertyModelWithOrigin.builder().property(property).origin(trait).build());
        });
    }

    /**
     * When an entity has a "*" property, that means the entity is a wrapper around a map of values of a
     * particular type. In this case, the entity interface needs to extend the "MappedNode" interface.
     *
     */
    @Override
    protected void createMappedNodeMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        new MappedNodeMethods(propertyWithOrigin.getProperty(), propertyWithOrigin, getCtx()).writeTo(javaEntity);
    }
}
