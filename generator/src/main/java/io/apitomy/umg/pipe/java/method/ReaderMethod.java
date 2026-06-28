package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * Naming class for reader methods: {@code read${EntityName}}.
 */
public class ReaderMethod implements Method {

    private final String entityName;

    public ReaderMethod(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Returns the reader method name for the given entity name.
     */
    public static String methodName(String entityName) {
        return "read" + StringUtils.capitalize(entityName);
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
