package io.apitomy.umg.models.concept.type;

import io.apitomy.umg.models.concept.EntityModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a reference to an entity type.
 */
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EntityType extends AbstractType {

    @ToString.Exclude
    private EntityModel entity;

    public static EntityType fromEntity(EntityModel entity) {
        return EntityType.builder()
                .namespace(entity.getNamespace().fullName())
                .name(entity.getName())
                .rawType(RawType.parse(entity.getName()))
                .entity(entity)
                .build();
    }

    @Override
    public EntityType copy() {
        return EntityType.builder()
                .namespace(namespace)
                .name(name)
                .rawType(rawType)
                .entity(entity)
                .parent(parent)
                .leaf(leaf)
                .root(root)
                .build();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isEntityType() { return true; }
}
