package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.java.type.JavaTypeFactory;

/**
 * Naming class for union "as" methods: {@code as${ComponentName}}.
 */
public class UnionAsMethod implements Method {

    private final String componentName;

    public UnionAsMethod(Type variantType) {
        this.componentName = JavaTypeFactory.getUnionComponentName(variantType);
    }

    public UnionAsMethod(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Returns the union "as" method name for the given component name.
     */
    public static String methodName(String componentName) {
        return "as" + componentName;
    }

    /**
     * Returns the union "as" method name for the given variant type.
     */
    public static String methodName(Type variantType) {
        return "as" + JavaTypeFactory.getUnionComponentName(variantType);
    }

    @Override
    public String getName() {
        return methodName(componentName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
