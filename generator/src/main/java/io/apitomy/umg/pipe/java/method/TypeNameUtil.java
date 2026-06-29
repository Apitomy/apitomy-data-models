package io.apitomy.umg.pipe.java.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;

public final class TypeNameUtil {

    private TypeNameUtil() {
    }

    public static String getTypeName(Type type) {
        if (type.isEntityType()) {
            return type.getName();
        } else if (type.isPrimitiveType()) {
            return StringUtils.capitalize(type.getName());
        } else if (type.isPrimitiveUnionVariantType()) {
            return StringUtils.capitalize(type.getName());
        } else if (type.isUnionType()) {
            List<Type> nestedTypes = new ArrayList<>(((UnionType) type).getTypes());
            return getUnionTypeName(nestedTypes);
        } else if (type.isListType()) {
            return getTypeName(((ListType) type).getValueType()) + "List";
        } else if (type.isMapType()) {
            return getTypeName(((MapType) type).getValueType()) + "Map";
        } else {
            throw new RuntimeException("Unsupported type in union: " + type);
        }
    }

    private static String getUnionTypeName(List<Type> unionNestedTypes) {
        return unionNestedTypes.stream().map(pt -> getTypeName(pt)).reduce((t, u) -> t + u).orElseThrow() + "Union";
    }
}
