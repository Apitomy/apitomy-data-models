package io.apitomy.umg.models.concept;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import io.apitomy.umg.models.spec.SpecificationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EntityModel extends EntityOrTraitModel {
    private final SpecificationModel specModel;
    private final Collection<TraitModel> traits = new LinkedHashSet<>();
    private final boolean root;
    private final List<String> propertyOrder;
}
