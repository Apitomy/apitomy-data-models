package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.TraitModel;

/**
 * Creates the java interfaces for all entities.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateTraitInterfacesStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findTraits("").forEach(trait -> {
            createTraitInterface(trait);
        });
    }

    private void createTraitInterface(TraitModel trait) {
        String _package = getJavaTraitInterfacePackage(trait);
        String name = getJavaTraitInterfaceName(trait);

        JavaInterfaceSource entityInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(name)
                .setPublic();

        getState().getJavaIndex().index(entityInterface);
    }
}