package io.apitomy.datamodels.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.union.Union;
import io.apitomy.datamodels.visitors.DefinitionDetectionVisitor;

/**
 * A utility class containing methods used to interact with data model nodes.  Anything that
 * needs to be handled differently (with respect to data model nodes) in Java vs. TS/JS should be done
 * here.  There is a NodeUtil.ts file that contains the TS/JS specific implementation of this class.
 * @author eric.wittmann@gmail.com
 */
public class NodeUtil {

    public static Object getProperty(Object node, String propertyName) {
        if (isNode(node)) {
            return getNodeProperty((Node) node, propertyName);
        }
        if (node instanceof List) {
            int idx = Integer.parseInt(propertyName);
            List<?> list = (List<?>) node;
            return list.get(idx);
        }
        if (node instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) node;
            return map.get(propertyName);
        }

        return null;
    }

    public static void setProperty(Object node, String propertyName, Object newValue) {
        try {
            String setterName = "set" + StringUtils.capitalize(propertyName);
            Method method = Arrays.stream(node.getClass().getMethods()).filter(m -> m.getName().equals(setterName))
                    .findFirst().orElseThrow(() -> new RuntimeException("Could not find setter " + setterName));
            method.invoke(node, newValue);
        } catch (Exception e) {
            LoggerUtil.warn("Failed to set property '%s' on Node '%s': %s", propertyName,
                    node.getClass().getSimpleName(), e.getMessage());
        }

    }

    /**
     * Invokes a method on the given target by name, matching on parameter count.
     * Useful for calling methods on generated interfaces where the parameter types
     * vary by version but the method signature is otherwise identical.
     * @param target the object to invoke the method on
     * @param methodName the name of the method
     * @param args the arguments to pass
     * @return the return value of the method (or null for void methods)
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) {
        try {
            Method method = Arrays.stream(target.getClass().getMethods())
                    .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == args.length)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not find method " + methodName
                            + " with " + args.length + " parameter(s) on " + target.getClass().getSimpleName()));
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value for a given node property.
     * @param node
     * @param propertyName
     */
    public static Object getNodeProperty(Node node, String propertyName) {
        // Access the value of the property by invoking its Getter
        String getterName = "get" + StringUtils.capitalize(propertyName);
        try {
            Method method = node.getClass().getMethod(getterName);
            return method.invoke(node);
        } catch (NoSuchMethodException nsme) {
            // Might be a boolean getter...
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e1) {
            return null;
        }

        // If that didn't work, try it again with the boolean form of the getter.
        getterName = "is" + StringUtils.capitalize(propertyName);
        try {
            Method method = node.getClass().getMethod(getterName);
            return method.invoke(node);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e1) {
            return null;
        }
    }

    /**
     * Return true if the given object is a Node.
     * @param object
     */
    public static boolean isNode(Object object) {
        return object != null && object instanceof Node;
    }

    /**
     * Return true if the given node is a definition.  A node is a definition in both AsyncAPI
     * and OpenAPI if it is defined in a way that makes it reusable elsewhere in the document.
     * @param node
     */
    public static boolean isDefinition(Node node) {
        DefinitionDetectionVisitor detector = new DefinitionDetectionVisitor();
        if (node.parent() != null) {
            node.parent().accept(detector);
        }
        return detector.isDefinitionParentDetected();
    }

    /**
     * Return true if the given object is a Union type.
     * @param object
     */
    public static boolean isUnion(Object object) {
        return object != null && object instanceof Union;
    }

    /**
     * Returns true if the given object is a list.
     * @param object
     */
    public static boolean isList(Object object) {
        return object != null && object instanceof List;
    }

    /**
     * Returns true if the given object is a list.
     * @param object
     */
    public static boolean isMap(Object object) {
        return object != null && object instanceof Map;
    }

    /**
     * Gets an item out of a map.  Needed due to transpilation wierdness.
     * @param map
     * @param key
     */
    @SuppressWarnings("rawtypes")
    public static Object getMapItem(Map map, String key) {
        return map.get(key);
    }

    /**
     * Converts a string value to an integer (it's expected to be formatted as an int).
     * @param value
     */
    public static int toInteger(String value) {
        return Integer.parseInt(value);
    }

    /**
     * Returns true if the given value is null or undefined.
     * @param value
     */
    public static boolean isNullOrUndefined(Object value) {
        return value == null;
    }

    /**
     * Returns true if the given value is defined (ie if the value is NOT null or undefined).
     * @param value
     */
    public static boolean isDefined(Object value) {
        return !isNullOrUndefined(value);
    }

    /**
     * Creates a list from a bunch of argument items.
     * @param items
     */
    public static List<String> asList(String ...items) {
        return Arrays.asList(items);
    }

    /**
     * Turns a list into an array.
     * @param list
     */
    public static String[] asArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    /**
     * Makes a copy of a list.
     * @param list
     */
    public static List<String> copyList(List<String> list) {
        List<String> rval = new ArrayList<>();
        rval.addAll(list);
        return rval;
    }

    /**
     * Tests two objects for equality.
     * @param value1
     * @param value2
     */
    public static boolean equals(Object value1, Object value2) {
        if (value1 == value2) {
            return true;
        }
        return value1 != null && value1.equals(value2);
    }

    /**
     * Gets the keys from the given map.
     * @param map
     */
    public static Collection<String> getMapKeys(Map<String, ?> map) {
        if (map == null) {
            return Collections.emptyList();
        }
        return map.keySet();
    }

    /**
     * Gets the values from the given map.
     * @param map
     */
    public static <T> Collection<T> getMapValues(Map<String, T> map) {
        if (map == null) {
            return Collections.emptyList();
        }
        return map.values();
    }

}
