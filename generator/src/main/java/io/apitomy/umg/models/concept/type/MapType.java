package io.apitomy.umg.models.concept.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a map type (e.g., {@code {Widget}}, {@code {string}}).
 * Keys are always strings.
 */
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class MapType extends CollectionType {

    @Override
    public MapType copy() {
        return MapType.builder()
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
    public boolean isMapType() { return true; }

    @Override
    public boolean isPrimitiveMapType() { return valueType.isPrimitiveType(); }

    @Override
    public boolean isEntityMapType() { return valueType.isEntityType(); }

    @Override
    public boolean isUnionMapType() { return valueType.isUnionType(); }
}
