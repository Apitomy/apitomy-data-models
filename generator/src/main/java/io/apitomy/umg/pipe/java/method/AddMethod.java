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
 * Generates an "add" method: signature, parameters, @Override (for impl), and body
 * that initializes collection if null and adds the value.
 * Handles both list and map variants.
 */
public class AddMethod implements Method {

    private final PropertyModel property;
    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;
    private final ParentAttachmentBlock parentBlock;

    public AddMethod(PropertyModel property, PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
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

    @Override
    public void writeTo(JavaSource<?> target) {
        Type resolvedType = property.getResolvedType();
        Type resolvedValueType = resolvedType.isCollectionType()
                ? ((CollectionType) resolvedType).getValueType()
                : null;
        if (resolvedValueType == null) {
            ctx.warn("Type not supported for 'add' method: " + getName() + " with type: " + resolvedType);
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
                    target.addImport(LinkedHashMap.class);
                    return """
                        if (this.${fieldName} == null) {
                            this.${fieldName} = new LinkedHashMap<>();
                        }
                        this.${fieldName}.put(name, value);
                        """;
                }, () -> {
                    target.addImport(ArrayList.class);
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
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (parentBlock != null) {
            parentBlock.addImportsTo(source);
        }
    }

}
