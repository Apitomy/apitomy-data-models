package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a getter method body: {@code return ${fieldName};}
 */
public class GetterMethod implements Method {

    private final PropertyModel property;
    private final CodeGenContext ctx;

    public GetterMethod(PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.ctx = ctx;
    }

    /**
     * Returns the getter method name for the given property name and type.
     */
    public static String methodName(String propertyName, Type type) {
        boolean isBool = type.isPrimitiveType() && type.getName().equals("boolean");
        return (isBool ? "is" : "get") + StringUtils.capitalize(propertyName);
    }

    /**
     * Returns the getter method name for the given property.
     */
    public static String methodName(PropertyModel property) {
        String name = property.getName();
        if (name.startsWith("/")) {
            name = property.getCollection();
        }
        return methodName(name, property.getResolvedType());
    }

    @Override
    public String getName() {
        return methodName(property);
    }

    public void writeTo(MethodSource<?> method) {
        String fieldName = ctx.getFieldName(property);
        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.append("return ${fieldName};");
        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
