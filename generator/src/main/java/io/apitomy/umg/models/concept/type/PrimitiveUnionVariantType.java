package io.apitomy.umg.models.concept.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Wraps a {@link PrimitiveType} when it appears as a variant in a union.
 * This distinction exists because primitives in unions need different
 * code generation (wrapper classes like {@code BooleanUnionValueImpl}).
 */
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class PrimitiveUnionVariantType extends AbstractType {

    private PrimitiveType type;

    @Override
    public PrimitiveUnionVariantType copy() {
        return PrimitiveUnionVariantType.builder()
                .namespace(namespace)
                .name(name)
                .rawType(rawType)
                .type(type)
                .parent(parent)
                .leaf(leaf)
                .root(root)
                .build();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        type.accept(visitor);
        visitor.visit(this);
    }

    @Override
    public boolean isPrimitiveUnionVariantType() { return true; }
}
