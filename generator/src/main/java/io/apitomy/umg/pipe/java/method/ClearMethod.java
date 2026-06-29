package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a "clear" method: signature, @Override (for impl), and body that
 * clears a collection plus detaches all items if entity/union.
 */
public class ClearMethod implements Method {

    private final PropertyModel property;
    private final CodeGenContext ctx;

    public ClearMethod(PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.ctx = ctx;
    }



    @Override
    public String getName() {
        return "clear" + StringUtils.capitalize(property.getName());
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setPublic()
                .setName(getName())
                .setReturnTypeVoid();

        if (target instanceof JavaClassSource) {
            method.addAnnotation(Override.class);

            String fieldName = ctx.getFieldName(property);
            Type resolvedType = property.getResolvedType();
            Type resolvedValueType = resolvedType.isCollectionType()
                    ? ((CollectionType) resolvedType).getValueType()
                    : null;
            boolean needsDetach = resolvedValueType != null
                    && (resolvedValueType.isEntityType() || resolvedValueType.isUnionType());

            BodyBuilder body = new BodyBuilder();
            body.addContext("fieldName", fieldName);

            body.ifElse(needsDetach, () -> {
                String valuesExpr = resolvedType.isMapType()
                        ? "this." + fieldName + ".values()" : "this." + fieldName;
                body.addContext("valuesExpr", valuesExpr);
                return """
if (this.${fieldName} != null) {
    ${valuesExpr}.forEach(item -> {
        if (item != null) item.detach();
    });
    this.${fieldName}.clear();
}
""";
            }, () -> """
if (this.${fieldName} != null) {
    this.${fieldName}.clear();
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
