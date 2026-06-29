package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.java.type.JavaTypeFactory;

/**
 * Generates union "is" methods: {@code is${ComponentName}}.
 * <p>
 * When constructed with just a component name or variant type, this is a
 * naming-only helper (calling {@link #writeTo} will throw). Supply the
 * {@code active} flag via the full constructor to enable code generation.
 */
public class UnionIsMethod implements Method {

    private final String componentName;
    private final Boolean active;

    public UnionIsMethod(Type variantType) {
        this.componentName = JavaTypeFactory.getUnionComponentName(variantType);
        this.active = null;
    }

    public UnionIsMethod(String componentName) {
        this.componentName = componentName;
        this.active = null;
    }

    public UnionIsMethod(String componentName, boolean active) {
        this.componentName = componentName;
        this.active = active;
    }



    @Override
    public String getName() {
        return "is" + componentName;
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        if (active == null) {
            throw new UnsupportedOperationException(
                    "UnionIsMethod requires the active flag to support writeTo(JavaSource)");
        }

        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setName(getName()).setReturnType(boolean.class).setPublic();
        method.addAnnotation(Override.class);
        method.setBody(active ? "return true;" : "return false;");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
