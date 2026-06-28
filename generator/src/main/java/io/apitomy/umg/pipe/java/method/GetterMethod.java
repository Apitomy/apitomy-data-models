package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;

/**
 * Generates a getter method body: {@code return ${fieldName};}
 */
public class GetterMethod implements CanAddImports {

    private final PropertyModel property;
    private final CodeGenContext ctx;

    public GetterMethod(PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.ctx = ctx;
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
