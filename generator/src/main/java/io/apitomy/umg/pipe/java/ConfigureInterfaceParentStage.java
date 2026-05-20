package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.TraitModel;

/**
 * Configures the parents of all interfaces (entities and traits).  Uses the concept
 * hierarchy to determine the parents.
 *
 * @author eric.wittmann@gmail.com
 */
public class ConfigureInterfaceParentStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").forEach(entity -> {
            configureEntityInterfaceParent(entity);
        });
        getState().getConceptIndex().findTraits("").forEach(trait -> {
            configureTraitInterfaceParent(trait);
        });
    }

    private void configureEntityInterfaceParent(EntityModel entity) {
        EntityModel parentEntity = (EntityModel) entity.getParent();
        JavaInterfaceSource parentJavaEntity = null;
        if (parentEntity != null) {
            parentJavaEntity = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(parentEntity));
        } else {
            parentJavaEntity = getState().getJavaIndex().lookupInterface(getNodeEntityInterfaceFQN());
        }

        JavaInterfaceSource javaEntity = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(entity));
        javaEntity.addInterface(parentJavaEntity);
    }

    private void configureTraitInterfaceParent(TraitModel trait) {
        TraitModel parentTrait = trait.getParent();
        if (parentTrait != null) {
            JavaInterfaceSource parentJavaTrait = getState().getJavaIndex().lookupInterface(getJavaTraitInterfaceFQN(parentTrait));
            JavaInterfaceSource javaTrait = getState().getJavaIndex().lookupInterface(getJavaTraitInterfaceFQN(trait));
            javaTrait.addInterface(parentJavaTrait);
        }
    }
}