package io.apitomy.umg.pipe.java.method;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates an "add" method body: initialize collection if null, add value, then
 * attach parent for entity/union types. Handles both list and map variants.
 */
public class AddMethod implements Method {

    private final PropertyModel property;
    private final JavaSource<?> javaEntity;
    private final CodeGenContext ctx;
    private final ParentAttachmentBlock parentBlock;

    public AddMethod(JavaSource<?> javaEntity, PropertyModel property, CodeGenContext ctx) {
        this.property = property;
        this.javaEntity = javaEntity;
        this.ctx = ctx;

        Type resolvedType = property.getResolvedType();
        Type resolvedValueType = resolvedType.isCollectionType()
                ? ((CollectionType) resolvedType).getValueType()
                : null;

        if (resolvedValueType != null && (resolvedValueType.isEntityType() || resolvedValueType.isUnionType())) {
            ParentAttachmentBlock.ParentPropertyKind kind = resolvedType.isMapType()
                    ? ParentAttachmentBlock.ParentPropertyKind.MAP
                    : ParentAttachmentBlock.ParentPropertyKind.ARRAY;
            this.parentBlock = new ParentAttachmentBlock(resolvedValueType, property.getName(), kind, ctx);
        } else {
            this.parentBlock = null;
        }
    }

    /**
     * Returns the add method name for the given (singularized) name.
     */
    public static String methodName(String singularName) {
        return "add" + StringUtils.capitalize(singularName);
    }

    @Override
    public String getName() {
        return methodName(ctx.singularize(property.getName()));
    }

    public void writeTo(MethodSource<?> method) {
        String fieldName = ctx.getFieldName(property);
        String propertyName = property.getName();

        Type resolvedType = property.getResolvedType();
        Type resolvedValueType = resolvedType.isCollectionType()
                ? ((CollectionType) resolvedType).getValueType()
                : null;
        boolean isEntityValue = resolvedValueType != null && resolvedValueType.isEntityType();
        boolean isUnionValue = resolvedValueType != null && resolvedValueType.isUnionType();
        boolean isPrimitiveValue = resolvedValueType != null && resolvedValueType.isPrimitiveType();

        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.addContext("propertyName", propertyName);

        if (isEntityValue || isPrimitiveValue || isUnionValue) {
            body.ifElse(resolvedType.isMapType(), () -> {
                javaEntity.addImport(LinkedHashMap.class);
                return """
if (this.${fieldName} == null) {
    this.${fieldName} = new LinkedHashMap<>();
}
this.${fieldName}.put(name, value);
""";
            }, () -> {
                javaEntity.addImport(ArrayList.class);
                return """
if (this.${fieldName} == null) {
    this.${fieldName} = new ArrayList<>();
}
this.${fieldName}.add(value);
""";
            });

            if (parentBlock != null) {
                parentBlock.appendTo(body);
            }
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
