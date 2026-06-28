package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.java.type.JavaTypeFactory;

/**
 * Naming class for union "is" methods: {@code is${ComponentName}}.
 */
public class UnionIsMethod implements Method {

    private final String componentName;

    public UnionIsMethod(Type variantType) {
        this.componentName = JavaTypeFactory.getUnionComponentName(variantType);
    }

    public UnionIsMethod(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Returns the union "is" method name for the given component name.
     */
    public static String methodName(String componentName) {
        return "is" + componentName;
    }

    /**
     * Returns the union "is" method name for the given variant type.
     */
    public static String methodName(Type variantType) {
        return "is" + JavaTypeFactory.getUnionComponentName(variantType);
    }

    @Override
    public String getName() {
        return methodName(componentName);
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        throw new UnsupportedOperationException(
                "UnionIsMethod is a naming-only helper and does not support writeTo(JavaSource)");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
