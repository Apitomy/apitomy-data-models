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



    @Override
    public String getName() {
        return "clone" + StringUtils.capitalize(entityName);
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        throw new UnsupportedOperationException(
                "ClonerMethod is a naming-only helper and does not support writeTo(JavaSource)");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
