package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.AbstractStage;

/**
 * Create models for all of the entities described in all the specifications.
 */
public class CreateEntityModelsStage extends AbstractStage {
    @Override
    protected void doProcess() {
        info("-- Creating Entity Models --");
        getState().getSpecIndex().getAllSpecifications().forEach(specificationModel -> {
            specificationModel.getVersions().forEach(specVersion -> {

                specVersion.getEntities().forEach(entity -> {
                    NamespaceModel nsModel = getState().getConceptIndex().lookupNamespace(specVersion.getNamespace());
                    if (nsModel == null) {
                        throw new RuntimeException("Namespace '" + specVersion.getNamespace() + "' for entity '" + entity.getName() + "' not found.");
                    }
                    EntityModel entityModel = EntityModel.builder()
                            .namespace(nsModel)
                            .name(entity.getName())
                            .root(entity.getRoot() != null ? entity.getRoot() : false)
                            .specModel(specificationModel)
                            .specVersion(specVersion)
                            .propertyOrder(entity.getPropertyOrder())
                            .leaf(true)
                            .build();
                    info("Created entity model: %s", entityModel.fullyQualifiedName());

                    // Add traits to the model
                    entity.getTraits().forEach(trait -> {
                        String fqTraitName = specVersion.getNamespace() + "." + trait;
                        TraitModel traitModel = getState().getConceptIndex().lookupTrait(fqTraitName);
                        if (traitModel == null) {
                            throw new RuntimeException("Trait '" + fqTraitName + "' referenced by entity '" + entityModel.fullyQualifiedName() + "' not found.");
                        }
                        entityModel.getTraits().add(traitModel);
                    });

                    // Add entity to namespace
                    nsModel.getEntities().put(entityModel.getName(), entityModel);
                    // Index the model
                    getState().getConceptIndex().index(entityModel);
                });

            });
        });
    }
}
