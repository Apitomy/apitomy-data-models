package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a "clear" method body: clear collection plus detach all items if entity/union.
 */
public class ClearMethod implements CanAddImports {

    private final PropertyModel property;
    private final CodeGenContext ctx;

    public ClearMethod(PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.ctx = ctx;
    }

    public void writeTo(MethodSource<?> method) {
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

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
