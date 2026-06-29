package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a "remove" method: signature, parameter, @Override (for impl), and body
 * that removes from list or map, plus detaches if entity/union.
 */
public class RemoveMethod implements Method {

    private final PropertyModel property;
    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;

    public RemoveMethod(PropertyModel property, PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
        this.property = property;
        this.propertyWithOrigin = propertyWithOrigin;
        this.ctx = ctx;
    }



    @Override
    public String getName() {
        return "remove" + StringUtils.capitalize(ctx.singularize(property.getName()));
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setPublic()
                .setName(getName())
                .setReturnTypeVoid();

        if (property.getResolvedType().isListType()) {
            Type resolvedValueType = ((CollectionType) property.getResolvedType()).getValueType();
            var jt = ctx.getJavaTypeFactory().createJavaType(
                    resolvedValueType,
                    propertyWithOrigin.getOrigin().getNamespace());
            jt.addImportsTo(target);
            method.addParameter(jt.toJavaTypeString(), "value");
        } else {
            method.addParameter("String", "name");
        }

        if (target instanceof JavaClassSource) {
            method.addAnnotation(Override.class);

            String fieldName = ctx.getFieldName(property);
            BodyBuilder body = new BodyBuilder();
            body.addContext("fieldName", fieldName);

            Type resolvedType = property.getResolvedType();
            Type resolvedValueType = resolvedType.isCollectionType()
                    ? ((CollectionType) resolvedType).getValueType()
                    : null;
            boolean needsDetach = resolvedValueType != null
                    && (resolvedValueType.isEntityType() || resolvedValueType.isUnionType());

            body.ifElse(resolvedType.isListType(), () -> {
                if (needsDetach) {
                    return """
if (this.${fieldName} != null) {
    if (value != null && this.${fieldName}.remove(value)) {
        value.detach();
    }
}
""";
                } else {
                    return """
if (this.${fieldName} != null) {
    this.${fieldName}.remove(value);
}
""";
                }
            }, () -> """
if (this.${fieldName} != null) {
    this.${fieldName}.remove(name);
}
""");

            method.setBody(body.toString());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
