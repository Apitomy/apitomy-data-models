package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a setter method: signature, parameter, @Override (for impl), and body
 * with field assignment plus parent tracking for entity/union types.
 */
public class SetterMethod implements Method {

    private final PropertyModel property;
    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;
    private final ParentAttachmentBlock parentBlock;

    public SetterMethod(PropertyModel property, PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
        this.property = property;
        this.propertyWithOrigin = propertyWithOrigin;
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
     * Naming-only constructor — use when only getName() is needed.
     */
    public SetterMethod(PropertyModel property) {
        this.property = property;
        this.propertyWithOrigin = null;
        this.ctx = null;
        this.parentBlock = null;
    }



    @Override
    public String getName() {
        return "set" + StringUtils.capitalize(property.getName());
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setName(getName())
                .setReturnTypeVoid()
                .setPublic();

        var jt = ctx.getJavaTypeFactory().createJavaType(
                property.getResolvedType(),
                propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(target);
        method.addParameter(jt.toJavaTypeString(), "value");

        if (target instanceof JavaClassSource) {
            method.addAnnotation(Override.class);
            addImportsTo(target);

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
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (parentBlock != null) {
            parentBlock.addImportsTo(source);
        }
    }

}
