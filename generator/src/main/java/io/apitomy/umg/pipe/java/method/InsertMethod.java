package io.apitomy.umg.pipe.java.method;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
 * Generates an "insert" method: signature, parameters, @Override (for impl), and body
 * that initializes collection if null and inserts at index using DataModelUtil.
 */
public class InsertMethod implements Method {

    private final PropertyModel property;
    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;
    private final ParentAttachmentBlock parentBlock;

    public InsertMethod(PropertyModel property, PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
        this.property = property;
        this.propertyWithOrigin = propertyWithOrigin;
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



    @Override
    public String getName() {
        return "insert" + StringUtils.capitalize(ctx.singularize(property.getName()));
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        Type resolvedType = property.getResolvedType();
        Type resolvedValueType = resolvedType.isCollectionType()
                ? ((CollectionType) resolvedType).getValueType()
                : null;
        if (resolvedValueType == null) {
            ctx.warn("Type not supported for 'insert' method: " + getName() + " with type: " + resolvedType);
            return;
        }

        var jt = ctx.getJavaTypeFactory().createJavaType(
                resolvedValueType,
                propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(target);

        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setPublic()
                .setName(getName())
                .setReturnTypeVoid();

        if (resolvedType.isMapType()) {
            method.addParameter("String", "name");
        }
        method.addParameter(jt.toJavaTypeString(), "value");
        method.addParameter("int", "atIndex");

        if (target instanceof JavaClassSource) {
            method.addAnnotation(Override.class);
            addImportsTo(target);

            boolean isEntityValue = resolvedValueType.isEntityType();
            boolean isUnionValue = resolvedValueType.isUnionType();
            boolean isPrimitiveValue = resolvedValueType.isPrimitiveType();

            String fieldName = ctx.getFieldName(property);
            String propertyName = property.getName();

            BodyBuilder body = new BodyBuilder();
            body.addContext("fieldName", fieldName);
            body.addContext("propertyName", propertyName);

            if (isEntityValue || isPrimitiveValue || isUnionValue) {
                body.ifElse(resolvedType.isMapType(), () -> {
                    JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
                    target.addImport(dataModelUtilSource);
                    target.addImport(LinkedHashMap.class);
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
                    target.addImport(dataModelUtilSource);
                    target.addImport(ArrayList.class);
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
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (parentBlock != null) {
            parentBlock.addImportsTo(source);
        }
    }

}
