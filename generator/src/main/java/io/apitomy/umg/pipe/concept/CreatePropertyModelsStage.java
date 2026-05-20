package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.beans.Property;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.AbstractStage;

import java.util.Collection;
import java.util.Map;

/**
 * Creates the models for all properties for both Traits and Entities.  This stage
 * assumes that the Trait and Entity models have already been created by previous
 * stages in the pipe.
 */
public class CreatePropertyModelsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        info("-- Creating Property Models --");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            // Create property models for traits
            specVersion.getTraits().forEach(trait -> {
                String fqTraitName = specVersion.getNamespace() + "." + trait.getName();
                TraitModel traitModel = getState().getConceptIndex().lookupTrait(fqTraitName);
                trait.getProperties().forEach(property -> {
                    PropertyModel propertyModel = PropertyModel.builder()
                            .name(property.getName())
                            .collection(property.getCollection())
                            .discriminator(property.getDiscriminator())
                            .unionRules(property.getUnionRules())
                            .rawType(property.getType())
                            .type(PropertyType.parse(property.getType()))
                            .build();
                    info("Created trait property model: %s/%s", traitModel.fullyQualifiedName(), propertyModel.getName());
                    traitModel.getProperties().put(property.getName(), propertyModel);
                });
            });

            // Create property models for entities
            specVersion.getEntities().forEach(entity -> {
                String fqEntityName = specVersion.getNamespace() + "." + entity.getName();
                EntityModel entityModel = getState().getConceptIndex().lookupEntity(fqEntityName);
                entity.getProperties().forEach(property -> {
                    PropertyModel propertyModel = PropertyModel.builder()
                            .name(property.getName())
                            .collection(property.getCollection())
                            .discriminator(property.getDiscriminator())
                            .unionRules(property.getUnionRules())
                            .rawType(property.getType())
                            .type(PropertyType.parse(property.getType()))
                            .shaded(isPropertyShaded(property, entityModel))
                            .build();
                    info("Created entity property model: %s/%s", entityModel.fullyQualifiedName(), propertyModel.getName());
                    entityModel.getProperties().put(property.getName(), propertyModel);
                });
            });
        });
    }

    /**
     * A property is considered shaded if there exists a Trait on the property's Entity
     * that contains the same property.  Why do this?  The reason is to make it more
     * convenient to create simple "Parent" interfaces (traits) for certain properties
     * that may be shared across multiple Entities.
     * <br />
     * For example, in the OpenAPI specification, the same "servers" property exists
     * at three levels:
     * <br />
     * <ol>
     *     <li>Document</li>
     *     <li>PathItem</li>
     *     <li>Operation</li>
     * </ol>
     * <br />
     * It is convenient to create a "ServerParent" trait that can be shared by all
     * three entities.  This can be done by simply creating such a Trait and then
     * removing the "servers" property from the three entity definitions.  But in
     * that case the property order is verbose to express and maintain.  Instead,
     * the property can be kept in the definitions of the entities AND ALSO broken
     * out into a shared Trait.
     */
    private boolean isPropertyShaded(Property property, EntityModel entityModel) {
        if (isPrimitiveType(property)) {
            return false;
        }
        Collection<TraitModel> traits = entityModel.getTraits();
        if (traits != null) {
            for (TraitModel trait : traits) {
                Map<String, PropertyModel> traitProperties = trait.getProperties();
                if (traitProperties.keySet().contains(property.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPrimitiveType(Property property) {
        String type = property.getType();
        PropertyType propertyType = PropertyType.parse(type);
        return propertyType != null && propertyType.isPrimitiveType();
    }
}
