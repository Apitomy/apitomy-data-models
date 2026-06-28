package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * Naming class for cloner methods: {@code clone${EntityName}}.
 */
public class ClonerMethod implements Method {

    private final String entityName;

    public ClonerMethod(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Returns the cloner method name for the given entity name.
     */
    public static String methodName(String entityName) {
        return "clone" + StringUtils.capitalize(entityName);
    }

    @Override
    public String getName() {
        return methodName(entityName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
