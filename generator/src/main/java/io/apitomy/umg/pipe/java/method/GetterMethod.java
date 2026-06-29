package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;


/**
 * Generates a getter method: signature, return type, @Override (for impl), and
 * body {@code return ${fieldName};}
 */
public class GetterMethod implements Method {

    private final PropertyModel property;
    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;

    public GetterMethod(PropertyModel property, PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
        this.property = property;
        this.propertyWithOrigin = propertyWithOrigin;
        this.ctx = ctx;
    }

    /**
     * Naming-only constructor — use when only getName() is needed.
     */
    public GetterMethod(PropertyModel property) {
        this.property = property;
        this.propertyWithOrigin = null;
        this.ctx = null;
    }



    @Override
    public String getName() {
        String name = property.getName();
        if (name.startsWith("/")) {
            name = property.getCollection();
        }
        boolean isBool = property.getResolvedType().isPrimitiveType() && property.getResolvedType().getName().equals("boolean");
        return (isBool ? "is" : "get") + StringUtils.capitalize(name);
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setName(getName())
                .setPublic();

        var jt = ctx.getJavaTypeFactory().createJavaType(
                property.getResolvedType(),
                propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(target);
        method.setReturnType(jt.toJavaTypeString());

        if (target instanceof JavaClassSource) {
            method.addAnnotation(Override.class);
            String fieldName = ctx.getFieldName(property);
            BodyBuilder body = new BodyBuilder();
            body.addContext("fieldName", fieldName);
            body.append("return ${fieldName};");
            method.setBody(body.toString());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
