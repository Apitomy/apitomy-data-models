package io.apitomy.umg.models.concept;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyModelWithOriginComparator implements Comparator<PropertyModelWithOrigin> {

    private Map<String, Integer> propertyValues = new HashMap<>();

    public PropertyModelWithOriginComparator(List<String> propertyOrder) {
        for (int idx = 0; idx < propertyOrder.size(); idx++) {
            propertyValues.put(propertyOrder.get(idx), idx);
        }
    }

    @Override
    public int compare(PropertyModelWithOrigin property1, PropertyModelWithOrigin property2) {
        String name1 = property1.getProperty().getName();
        String name2 = property2.getProperty().getName();
        if (name1.equals(name2)) {
            return 0;
        }

        Integer orderValue1 = propertyValues.get(name1);
        Integer orderValue2 = propertyValues.get(name2);
        if (orderValue1 == null || orderValue2 == null) {
            return name1.compareToIgnoreCase(name2);
        }

        return orderValue1.compareTo(orderValue2);
    }

}
