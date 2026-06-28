package io.apitomy.umg.pipe.java.method;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates an "insert" method body: initialize collection if null, insert at index
 * using DataModelUtil, then attach parent for entity/union types.
 */
public class InsertMethod implements CanAddImports {

    private final PropertyModel property;
    private final JavaSource<?> javaEntity;
    private final CodeGenContext ctx;
    private final ParentAttachmentBlock parentBlock;

    public InsertMethod(JavaSource<?> javaEntity, PropertyModel property, CodeGenContext ctx) {
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
            body.ifElse(resolvedType.isMapType(), () -> {
                JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
                javaEntity.addImport(dataModelUtilSource);
                javaEntity.addImport(LinkedHashMap.class);
                return """
if (this.${fieldName} == null) {
    this.${fieldName} = new LinkedHashMap<>();
    this.${fieldName}.put(name, value);
} else {
    this.${fieldName} = DataModelUtil.insertMapEntry(this.${fieldName}, name, value, atIndex);
}
""";
            }, () -> {
                JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
                javaEntity.addImport(dataModelUtilSource);
                javaEntity.addImport(ArrayList.class);
                return """
if (this.${fieldName} == null) {
    this.${fieldName} = new ArrayList<>();
    this.${fieldName}.add(value);
} else {
    this.${fieldName} = DataModelUtil.insertListEntry(this.${fieldName}, value, atIndex);
}
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
