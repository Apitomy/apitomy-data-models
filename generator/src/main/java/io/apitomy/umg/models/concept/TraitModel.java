package io.apitomy.umg.models.concept;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TraitModel extends EntityOrTraitModel {

    private TraitModel parent;
    private boolean transparent;

}
