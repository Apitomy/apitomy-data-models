package io.apitomy.umg.models.concept.type;

/**
 * Represents a resolved type in the generator's concept model.
 * <p>
 * Types form a tree: a {@link UnionType} contains multiple variant {@code Type}s,
 * a {@link ListType} or {@link MapType} wraps a value {@code Type}.
 * <p>
 * Unlike the raw parsed type representation,
 * {@code Type} instances are resolved against the concept index — entity types reference
 * actual {@link io.apitomy.umg.models.concept.EntityModel} objects.
 *
 * @see EntityType
 * @see UnionType
 * @see PrimitiveType
 * @see ListType
 * @see MapType
 */
public interface Type {

    String getNamespace();

    String getName();

    /**
     * The raw type string this type was parsed from (e.g., "string", "[Widget]", "Foo|Bar").
     */
    RawType getRawType();

    boolean isRoot();

    void setRoot(boolean root);

    /**
     * Parent type in the normalization hierarchy — a type with the same name
     * in a parent namespace. Used for cross-version type lifting.
     */
    Type getParent();

    void setParent(Type parent);

    boolean isLeaf();

    void setLeaf(boolean leaf);

    void accept(TypeVisitor visitor);

    Type copy();

    default boolean isEntityType() { return false; }

    default boolean isPrimitiveType() { return false; }

    default boolean isUnionType() { return false; }

    default boolean isListType() { return false; }

    default boolean isMapType() { return false; }

    default boolean isCollectionType() { return false; }

    default boolean isPrimitiveUnionVariantType() { return false; }

    default boolean isPrimitiveListType() { return false; }

    default boolean isPrimitiveMapType() { return false; }

    default boolean isEntityListType() { return false; }

    default boolean isEntityMapType() { return false; }
}
