package io.apitomy.umg.models.concept;

import java.util.List;

import io.apitomy.umg.beans.UnionRule;
import io.apitomy.umg.models.concept.type.Type;
import lombok.Builder;
import lombok.Data;

/**
 * Models a single property in an entity or trait.
 */
@Builder
@Data
public class PropertyModel {

    private String name;

    private String collection;

    private String discriminator;

    private String rawType;

    private List<UnionRule> unionRules;

    /**
     * @deprecated Use {@link #resolvedType} instead. Will be removed once all stages are migrated.
     */
    @Deprecated
    private PropertyType type;

    /**
     * The resolved type — references actual EntityModel objects, carries union rules,
     * and supports the visitor pattern. Set by CreatePropertyAndTypeModelsStage.
     */
    private Type resolvedType;

    private boolean shaded;

    public UnionRule getRuleFor(String rawUnionSubtype) {
        if (unionRules != null) {
            return unionRules.stream().filter(rule -> rule.getUnionType().equals(rawUnionSubtype)).findFirst().orElse(null);
        }
        return null;
    }
}
