package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates a "remove" method body: remove from list or map, plus detach if entity/union.
 */
public class RemoveMethod implements CanAddImports {

    private final PropertyModel property;
    private final CodeGenContext ctx;

    public RemoveMethod(PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.ctx = ctx;
    }

    public void writeTo(MethodSource<?> method) {
        String fieldName = ctx.getFieldName(property);
        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);

        Type resolvedType = property.getResolvedType();
        Type resolvedValueType = resolvedType.isCollectionType()
                ? ((CollectionType) resolvedType).getValueType()
                : null;
        boolean needsDetach = resolvedValueType != null
                && (resolvedValueType.isEntityType() || resolvedValueType.isUnionType());

        if (resolvedType.isListType()) {
            body.append("if (this.${fieldName} != null) {");
            if (needsDetach) {
                body.append("    if (value != null && this.${fieldName}.remove(value)) {");
                body.append("        value.detach();");
                body.append("    }");
            } else {
                body.append("    this.${fieldName}.remove(value);");
            }
            body.append("}");
        } else {
            body.append("if (this.${fieldName} != null) {");
            body.append("    this.${fieldName}.remove(name);");
            body.append("}");
        }

        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
