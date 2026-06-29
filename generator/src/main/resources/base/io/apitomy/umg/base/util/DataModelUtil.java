package io.apitomy.umg.base.util;

import io.apitomy.umg.base.Node;
import io.apitomy.umg.base.NodeImpl;
import io.apitomy.umg.base.ParentPropertyType;
import io.apitomy.umg.base.union.UnionValueImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataModelUtil {

    public static <V> Map<String, V> insertMapEntry(Map<String, V> map, String key, V value, int atIndex) {
        // If the new key is present, then return without modification.
        if (map.containsKey(key)) {
            return map;
        }
        // If the map isn't an LHM then ordering can't be maintained anyway
        // If the atIndex is null then we're trying to undo a command that was persisted prior to this functionality being added
        // If the atIndex is >= the map size it has to go at the end anyway
        if (!(map instanceof LinkedHashMap) || atIndex >= map.size()){
            map.put(key, value);
            return map;
        }

        final LinkedHashMap<String, V> newMap = new LinkedHashMap<>();
        // In order to maintain ordering of the elements when replacing a key in a LinkedHashMap,
        // create a new instance and populate it in insertion order
        int index = 0;
        for (Map.Entry<String, V> entry : map.entrySet()) {
            if (index++ == atIndex) {
                newMap.put(key, value);
            }
            newMap.put(entry.getKey(), entry.getValue());
        }
        return newMap;
    }

    public static <V> List<V> insertListEntry(List<V> list, V value, int atIndex) {
        if (atIndex >= list.size()) {
            list.add(value);
        } else if (atIndex < 0) {
            list.add(0, value);
        } else {
            list.add(atIndex, value);
        }
        return list;
    }

    public static void setParent(Object child, Node parent, String propertyName, ParentPropertyType propertyType) {
        if (child instanceof NodeImpl) {
            ((NodeImpl) child)._setParent(parent);
            ((NodeImpl) child)._setParentPropertyName(propertyName);
            ((NodeImpl) child)._setParentPropertyType(propertyType);
        } else if (child instanceof UnionValueImpl) {
            ((UnionValueImpl<?>) child)._setParent(parent);
            ((UnionValueImpl<?>) child)._setParentPropertyName(propertyName);
            ((UnionValueImpl<?>) child)._setParentPropertyType(propertyType);
        }
    }

    public static void setParentMap(Object child, Node parent, String propertyName, ParentPropertyType propertyType, String mapPropertyName) {
        setParent(child, parent, propertyName, propertyType);
        if (child instanceof NodeImpl) {
            ((NodeImpl) child)._setMapPropertyName(mapPropertyName);
        } else if (child instanceof UnionValueImpl) {
            ((UnionValueImpl<?>) child)._setMapPropertyName(mapPropertyName);
        }
    }

}
