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

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }

}
