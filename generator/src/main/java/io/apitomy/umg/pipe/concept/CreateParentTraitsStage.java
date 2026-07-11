package io.apitomy.umg.pipe.concept;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Creates "parent" traits for all entities with child properties.  Only creates the parent trait
 * if there are at least two entities that have that child as a property.  For example, the "Contact"
 * entity is always a child of the "Info" entity, so a "ContactParent" trait is not created as it
 * is not needed.
 * <p>
 * The purpose of the Parent trait is to have a common trait used to manage the parent-child
 * relationship of an entity during visitation.
 */
public class CreateParentTraitsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        List<PropertyModelWithOrigin> propertiesToRemove = new LinkedList<>();

        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            entity.getProperties().values().stream().filter(property -> needsParent(entity, property)).forEach(property -> {
                if (!property.getType().isList() && !property.getType().isMap()) {
                    String propertyTypeName = property.getType().getSimpleType();
                    String traitName = propertyTypeName + "Parent";
                    TraitModel parentTrait;
                    if (entity.getNamespace().containsTrait(traitName)) {
                        parentTrait = entity.getNamespace().getTraits().get(traitName);
                    } else {
                        parentTrait = TraitModel.builder().namespace(entity.getNamespace()).name(traitName).build();
                        PropertyModel traitProperty = PropertyModel.builder()
                                .name(property.getName())
                                .collection(property.getCollection())
                                .rawType(property.getRawType())
                                .type(property.getType()).build();
                        parentTrait.getProperties().put(property.getName(), traitProperty);
                        entity.getNamespace().getTraits().put(traitName, parentTrait);
                        getState().getConceptIndex().index(parentTrait);
                    }
                    entity.getTraits().add(parentTrait);

                    PropertyModelWithOrigin pmwo = PropertyModelWithOrigin.builder().property(property).origin(entity).build();
                    propertiesToRemove.add(pmwo);
                }
            });
        });

        propertiesToRemove.forEach(property -> {
            property.getOrigin().removeProperty(property.getProperty().getName());
        });
    }

    /**
     * Returns true if the given entity/property combination is not unique across the models.  If the
     * combination is unique, then no parent trait is needed.  If it is NOT unique (meaning there
     * are other entities with the same property) then a parent IS needed.
     * <br/>
     * In other words, we're trying to find multiple entities that share a child entity property.
     * So if "Foo" and "Bar" entities both have a property called "widget" of type "Widget", then
     * we want those entities to both have a "WidgetParent" trait.
     *
     * @param entity
     * @param property
     */
    private boolean needsParent(EntityModel entity, PropertyModel property) {
        if (isStarProperty(property)) {
            return false;
        }
        if (isRegexProperty(property)) {
            return false;
        }
        if (!isEntity(property)) {
            return false;
        }
        String propertyName = property.getName();
        String propertyTypeName = property.getType().getSimpleType();
        if (!propertyName.equals(StringUtils.capitalize(propertyTypeName))) {
            return false;
        }
        return getState().getConceptIndex().findEntities("")
                .stream()
                .filter(e -> !e.getName().equals(entity.getName()))
                .filter(e -> e.hasProperty(property.getName()))
                .filter(e -> e.getProperties().get(property.getName()).equals(property))
                .findFirst()
                .isPresent();
    }

}
