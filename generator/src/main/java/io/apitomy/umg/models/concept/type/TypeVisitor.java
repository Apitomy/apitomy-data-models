package io.apitomy.umg.models.concept.type;

/**
 * Visitor for the {@link Type} hierarchy.
 * All methods have default no-op implementations — override only the types you care about.
 */
public interface TypeVisitor {

    default void visit(PrimitiveType type) {}

    default boolean shouldVisit(UnionType type) { return true; }

    default void visit(UnionType type) {}

    default void visit(ListType type) {}

    default void visit(MapType type) {}

    default void visit(EntityType type) {}

    default void visit(PrimitiveUnionVariantType type) {}
}
