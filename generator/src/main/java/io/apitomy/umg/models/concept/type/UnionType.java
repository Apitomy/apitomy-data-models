package io.apitomy.umg.models.concept.type;

import io.apitomy.umg.beans.UnionRule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a union of multiple types (e.g., {@code Foo|Bar|boolean}).
 * <p>
 * Union types can be:
 * <ul>
 *   <li>Named — created from a type alias in the spec YAML</li>
 *   <li>Anonymous — created inline from a property's type string</li>
 * </ul>
 * Named unions are stored in the type index and can be referenced by name.
 */
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class UnionType extends AbstractType {

    @lombok.Builder.Default
    private List<Type> types = new ArrayList<>();

    @lombok.Builder.Default
    private List<UnionRule> unionRules = new ArrayList<>();

    @Override
    public UnionType copy() {
        return UnionType.builder()
                .namespace(namespace)
                .name(name)
                .rawType(rawType)
                .types(new ArrayList<>(types))
                .unionRules(new ArrayList<>(unionRules))
                .parent(parent)
                .leaf(leaf)
                .root(root)
                .build();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        if (visitor.shouldVisit(this)) {
            types.forEach(t -> t.accept(visitor));
        }
        visitor.visit(this);
    }

    @Override
    public boolean isUnionType() { return true; }

    public UnionRule getRuleFor(String rawUnionSubtype) {
        if (unionRules != null) {
            return unionRules.stream()
                    .filter(rule -> rule.getUnionType().equals(rawUnionSubtype))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
