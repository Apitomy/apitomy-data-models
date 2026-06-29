package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.java.type.JavaType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;

/**
 * Generates union "as" methods: {@code as${ComponentName}}.
 * <p>
 * When constructed with just a component name or variant type, this is a
 * naming-only helper (calling {@link #writeTo} will throw). Supply return type
 * info and active/entity flags via the full constructor to enable code generation.
 */
public class UnionAsMethod implements Method {

    private final String componentName;
    private final String returnType;
    private final Boolean active;
    private final boolean entityVariant;
    private final JavaType javaType;

    public UnionAsMethod(Type variantType) {
        this.componentName = JavaTypeFactory.getUnionComponentName(variantType);
        this.returnType = null;
        this.active = null;
        this.entityVariant = false;
        this.javaType = null;
    }

    public UnionAsMethod(String componentName) {
        this.componentName = componentName;
        this.returnType = null;
        this.active = null;
        this.entityVariant = false;
        this.javaType = null;
    }

    public UnionAsMethod(String componentName, String returnType, boolean active, boolean entityVariant,
                         JavaType javaType) {
        this.componentName = componentName;
        this.returnType = returnType;
        this.active = active;
        this.entityVariant = entityVariant;
        this.javaType = javaType;
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
    public void writeTo(JavaSource<?> target) {
        if (active == null) {
            throw new UnsupportedOperationException(
                    "UnionAsMethod requires the active/returnType flags to support writeTo(JavaSource)");
        }

        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setName(getName()).setReturnType(returnType).setPublic();
        method.addAnnotation(Override.class);
        javaType.addImportsTo(target);

        if (active) {
            if (entityVariant) {
                method.setBody("return this;");
            } else {
                method.setBody("return getValue();");
            }
        } else {
            method.setBody("throw new ClassCastException();");
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (javaType != null) {
            javaType.addImportsTo(source);
        }
    }

}
