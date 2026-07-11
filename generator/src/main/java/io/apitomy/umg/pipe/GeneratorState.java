package io.apitomy.umg.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.apitomy.umg.UnifiedModelGeneratorConfig;
import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.concept.SpecificationIndex;
import io.apitomy.umg.index.java.JavaIndex;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.models.spec.SpecificationModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratorState {

    private UnifiedModelGeneratorConfig config;

    private Collection<SpecificationModel> specifications;

    private ConceptIndex conceptIndex = new ConceptIndex();

    private SpecificationIndex specIndex = new SpecificationIndex();

    private JavaIndex javaIndex = new JavaIndex();

    /**
     * Returns all traits with the same name as the given parent trait.  Does a search of
     * the namespace tree to find such traits.
     *
     * @param parentTrait
     */
    public Collection<TraitModel> findChildTraitsFor(TraitModel parentTrait) {
        String traitName = parentTrait.getName();
        NamespaceModel parentNamespace = parentTrait.getNamespace();
        List<TraitModel> childTraits = new ArrayList<>();
        for (NamespaceModel namespaceModel : parentNamespace.getChildren().values()) {
            TraitModel childTrait = findChildTraitIn(namespaceModel, traitName);
            if (childTrait != null) {
                childTraits.add(childTrait);
            }
        }
        return childTraits;
    }

    /**
     * Returns all entities with the same name as the given parent entity.  Does a search of
     * the namespace tree to find such entities.
     *
     * @param parentEntity
     */
    public Collection<EntityModel> findChildEntitiesFor(EntityModel parentEntity) {
        String entityName = parentEntity.getName();
        NamespaceModel parentNamespace = parentEntity.getNamespace();
        List<EntityModel> childEntities = new ArrayList<>();
        for (NamespaceModel namespaceModel : parentNamespace.getChildren().values()) {
            EntityModel childEntity = findChildEntityIn(namespaceModel, entityName);
            if (childEntity != null) {
                childEntities.add(childEntity);
            }
        }
        return childEntities;
    }

    /**
     * Finds a child trait in the given namespace with the given name.
     *
     * @param namespaceModel
     * @param traitName
     */
    public TraitModel findChildTraitIn(NamespaceModel namespaceModel, String traitName) {
        if (namespaceModel.containsTrait(traitName)) {
            return namespaceModel.getTraits().get(traitName);
        }
        for (NamespaceModel childNamespace : namespaceModel.getChildren().values()) {
            TraitModel traitModel = findChildTraitIn(childNamespace, traitName);
            if (traitModel != null) {
                return traitModel;
            }
        }
        return null;
    }

    /**
     * Finds a child entity in the given namespace with the given name.
     *
     * @param namespaceModel
     * @param entityName
     */
    public EntityModel findChildEntityIn(NamespaceModel namespaceModel, String entityName) {
        if (namespaceModel.containsEntity(entityName)) {
            return namespaceModel.getEntities().get(entityName);
        }
        for (NamespaceModel childNamespace : namespaceModel.getChildren().values()) {
            EntityModel entityModel = findChildEntityIn(childNamespace, entityName);
            if (entityModel != null) {
                return entityModel;
            }
        }
        return null;
    }

}
