package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a setter method body: {@code this.${fieldName} = value;} plus parent
 * tracking for entity/union types.
 */
public class SetterMethod implements Method {

    private final PropertyModel property;
    private final JavaSource<?> javaEntity;
    private final CodeGenContext ctx;
    private final ParentAttachmentBlock parentBlock;

    public SetterMethod(JavaSource<?> javaEntity, PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.javaEntity = javaEntity;
        this.ctx = ctx;

        Type resolvedType = property.getResolvedType();
        if (resolvedType.isEntityType() || resolvedType.isUnionType()) {
            this.parentBlock = new ParentAttachmentBlock(
                    resolvedType, property.getName(),
                    ParentAttachmentBlock.ParentPropertyKind.STANDARD, ctx);
        } else {
            this.parentBlock = null;
        }
    }

    /**
     * Returns the setter method name for the given property name.
     */
    public static String methodName(String propertyName) {
        return "set" + StringUtils.capitalize(propertyName);
    }

    /**
     * Returns the setter method name for the given property.
     */
    public static String methodName(PropertyModel property) {
        return methodName(property.getName());
    }

    @Override
    public String getName() {
        return methodName(property);
    }

    public void writeTo(MethodSource<?> method) {
        String fieldName = ctx.getFieldName(property);
        String propertyName = property.getName();

        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.addContext("propertyName", propertyName);
        body.append("this.${fieldName} = value;");

        if (parentBlock != null) {
            parentBlock.appendTo(body);
        }

        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (parentBlock != null) {
            parentBlock.addImportsTo(source);
        }
    }

}
