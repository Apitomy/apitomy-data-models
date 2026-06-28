package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * Naming class for writer methods: {@code write${EntityName}}.
 */
public class WriterMethod implements Method {

    private final String entityName;

    public WriterMethod(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Returns the writer method name for the given entity name.
     */
    public static String methodName(String entityName) {
        return "write" + StringUtils.capitalize(entityName);
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
