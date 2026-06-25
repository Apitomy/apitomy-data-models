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
    private final ImplMethodContext ctx;

    public ClearMethod(PropertyModel property, ImplMethodContext ctx) {
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

        if (needsDetach) {
            String valuesExpr = resolvedType.isMapType()
                    ? "this." + fieldName + ".values()" : "this." + fieldName;
            body.addContext("valuesExpr", valuesExpr);
            body.append("if (this.${fieldName} != null) {");
            body.append("    ${valuesExpr}.forEach(item -> {");
            body.append("        if (item != null) item.detach();");
            body.append("    });");
            body.append("    this.${fieldName}.clear();");
            body.append("}");
        } else {
            body.append("if (this.${fieldName} != null) {");
            body.append("    this.${fieldName}.clear();");
            body.append("}");
        }

        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
