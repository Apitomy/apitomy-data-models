package io.apitomy.umg.base.util;

import io.apitomy.umg.base.Node;
import io.apitomy.umg.base.NodeImpl;
import io.apitomy.umg.base.ParentPropertyType;
import io.apitomy.umg.base.union.Union;
import io.apitomy.umg.base.union.UnionValue;
import io.apitomy.umg.base.union.UnionValueImpl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataModelUtil {

    public static <V> Map<String, V> insertMapEntry(Map<String, V> map, String key, V value, int atIndex) {
        if (map.containsKey(key)) {
            return map;
        }
        if (!(map instanceof LinkedHashMap) || atIndex >= map.size()){
            map.put(key, value);
            return map;
        }

        final LinkedHashMap<String, V> newMap = new LinkedHashMap<>();
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

    /**
     * Sets parent tracking metadata on a child node or union value.
     * For unions containing entity lists or maps, also sets parent on the nested entities.
     */
    public static void setParent(Object child, Node parent, String propertyName, ParentPropertyType propertyType) {
        setParent(child, parent, propertyName, propertyType, null);
    }

    public static void setParentMap(Object child, Node parent, String propertyName, ParentPropertyType propertyType, String mapPropertyName) {
        setParent(child, parent, propertyName, propertyType, mapPropertyName);
    }

    private static void setParent(Object child, Node parent, String propertyName, ParentPropertyType propertyType, String mapPropertyName) {
        if (child == null) return;

        if (child instanceof NodeImpl) {
            ((NodeImpl) child)._setParent(parent);
            ((NodeImpl) child)._setParentPropertyName(propertyName);
            ((NodeImpl) child)._setParentPropertyType(propertyType);
            if (mapPropertyName != null) {
                ((NodeImpl) child)._setMapPropertyName(mapPropertyName);
            }
        } else if (child instanceof UnionValueImpl) {
            ((UnionValueImpl<?>) child)._setParent(parent);
            ((UnionValueImpl<?>) child)._setParentPropertyName(propertyName);
            ((UnionValueImpl<?>) child)._setParentPropertyType(propertyType);
            if (mapPropertyName != null) {
                ((UnionValueImpl<?>) child)._setMapPropertyName(mapPropertyName);
            }
        }

        // Set parent relationship on nested Any values within union entity lists/maps
        if (child instanceof Union) {
            Union union = (Union) child;
            if (union.isEntityList()) {
                List<?> entityList = (List<?>) ((UnionValue<?>) union).getValue();
                for (int i = 0; i < entityList.size(); i++) {
                    Object entity = entityList.get(i);
                    if (entity != null) {
                        setParent(entity, parent, propertyName, ParentPropertyType.array, null);
                    }
                }
            } else if (union.isEntityMap()) {
                Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) union).getValue();
                Collection<String> keys = entityMap.keySet();
                for (String key : keys) {
                    Object entity = entityMap.get(key);
                    if (entity != null) {
                        setParent(entity, parent, propertyName, ParentPropertyType.map, key);
                    }
                }
            }
        }
    }

}
