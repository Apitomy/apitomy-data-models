package io.apitomy.umg.models.concept.type;

import java.util.Comparator;

/**
 * Orders union variant types by semantic importance for generated code dispatch:
 * <ol>
 *   <li>Entity types</li>
 *   <li>Entity/union collection types — lists then maps</li>
 *   <li>Primitive types</li>
 *   <li>Primitive collection types — lists then maps</li>
 * </ol>
 * Within each group, variants are sorted alphabetically by name.
 */
public class UnionVariantComparator implements Comparator<Type> {

    public static final UnionVariantComparator INSTANCE = new UnionVariantComparator();

    @Override
    public int compare(Type a, Type b) {
        int priorityA = priority(a);
        int priorityB = priority(b);
        if (priorityA != priorityB) {
            return Integer.compare(priorityA, priorityB);
        }
        return a.getName().compareToIgnoreCase(b.getName());
    }

    private static int priority(Type type) {
        if (type.isEntityType()) return 0;
        if (type.isListType() && isEntityOrUnionValue((CollectionType) type)) return 1;
        if (type.isMapType() && isEntityOrUnionValue((CollectionType) type)) return 2;
        if (type.isPrimitiveType() || type.isPrimitiveUnionVariantType()) return 3;
        if (type.isListType()) return 4;
        if (type.isMapType()) return 5;
        return 6;
    }

    private static boolean isEntityOrUnionValue(CollectionType ct) {
        return ct.getValueType().isEntityType() || ct.getValueType().isUnionType();
    }
}
