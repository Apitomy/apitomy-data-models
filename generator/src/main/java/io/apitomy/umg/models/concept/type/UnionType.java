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

    private String aliasName;

    @Override
    public UnionType copy() {
        return UnionType.builder()
                .namespace(namespace)
                .name(name)
                .rawType(rawType)
                .types(new ArrayList<>(types))
                .unionRules(new ArrayList<>(unionRules))
                .aliasName(aliasName)
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

    /**
     * Resolves the union type that a property's declared union rules apply to, unwrapping a single
     * list or map wrapper.
     * <p>
     * A property declared as {@code A|B} resolves directly to a union, but {@code [A|B]} and
     * {@code {A|B}} resolve to a list or map whose value type is the union. Union rules are declared
     * on the property in all three cases, so callers that attach or read rules must unwrap first.
     *
     * @param type the property's resolved type
     * @return the effective union type, or {@code null} if the type is not a union or a collection of one
     */
    public static UnionType effectiveUnionType(Type type) {
        if (type instanceof UnionType unionType) {
            return unionType;
        }
        if (type instanceof ListType listType && listType.getValueType() instanceof UnionType unionType) {
            return unionType;
        }
        if (type instanceof MapType mapType && mapType.getValueType() instanceof UnionType unionType) {
            return unionType;
        }
        return null;
    }

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
