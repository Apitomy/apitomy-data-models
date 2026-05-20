package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.pipe.AbstractStage;
import io.apitomy.umg.pipe.Util;

public class CreateTraitModelsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        info("-- Creating Trait Models --");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            specVersion.getTraits().forEach(trait -> {
                NamespaceModel nsModel = getState().getConceptIndex().lookupNamespace(specVersion.getNamespace());
                TraitModel traitModel = TraitModel.builder()
                        .namespace(nsModel)
                        .name(trait.getName())
                        .specVersion(specVersion)
                        .transparent(Util.nullableBoolean(trait.getTransparent()))
                        .leaf(true)
                        .build();
                info("Created trait model: %s", traitModel.fullyQualifiedName());

                // Add trait to namespace
                nsModel.getTraits().put(traitModel.getName(), traitModel);
                // Index the model
                getState().getConceptIndex().index(traitModel);
            });
        });
    }

}
