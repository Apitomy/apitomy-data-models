package io.apitomy.umg.pipe.java.method;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates an "add" method body: initialize collection if null, add value, then
 * attach parent for entity/union types. Handles both list and map variants.
 */
public class AddMethod implements CanAddImports {

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
            if (resolvedType.isMapType()) {
                javaEntity.addImport(LinkedHashMap.class);

                body.append("if (this.${fieldName} == null) {");
                body.append("    this.${fieldName} = new LinkedHashMap<>();");
                body.append("}");
                body.append("this.${fieldName}.put(name, value);");
            } else {
                javaEntity.addImport(ArrayList.class);

                body.append("if (this.${fieldName} == null) {");
                body.append("    this.${fieldName} = new ArrayList<>();");
                body.append("}");
                body.append("this.${fieldName}.add(value);");
            }

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
