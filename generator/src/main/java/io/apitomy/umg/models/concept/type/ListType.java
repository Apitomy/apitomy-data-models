package io.apitomy.umg.models.concept.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a list type (e.g., {@code [Widget]}, {@code [string]}).
 */
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class ListType extends CollectionType {

    @Override
    public ListType copy() {
        return ListType.builder()
                .namespace(namespace)
                .name(name)
                .rawType(rawType)
                .valueType(valueType)
                .parent(parent)
                .leaf(leaf)
                .root(root)
                .build();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        valueType.accept(visitor);
        visitor.visit(this);
    }

    @Override
    public boolean isListType() { return true; }

    @Override
    public boolean isPrimitiveListType() { return valueType.isPrimitiveType(); }

    @Override
    public boolean isEntityListType() { return valueType.isEntityType(); }
}
