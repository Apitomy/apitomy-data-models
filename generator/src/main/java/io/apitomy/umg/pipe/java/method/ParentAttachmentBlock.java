package io.apitomy.umg.pipe.java.method;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates parent tracking code for entity/union types. The generated code varies based
 * on the type and the parent-property-type (standard, array, map).
 */
public class ParentAttachmentBlock extends CodeBlock {

    public enum ParentPropertyKind {
        STANDARD("standard"), ARRAY("array"), MAP("map");

        private final String value;
        ParentPropertyKind(String value) { this.value = value; }
        public String value() { return value; }
    }

    private final Type valueType;
    private final String propertyName;
    private final ParentPropertyKind kind;
    private final CodeGenContext ctx;

    /**
     * @param valueType the resolved type of the value being attached
     * @param propertyName the property name for _setParentPropertyName, or null for mapped-node methods
     * @param kind determines the ParentPropertyType enum value (standard/array/map)
     * @param ctx stage context for FQN lookups
     */
    public ParentAttachmentBlock(Type valueType, String propertyName, ParentPropertyKind kind,
            CodeGenContext ctx) {
        this.valueType = valueType;
        this.propertyName = propertyName;
        this.kind = kind;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        if (valueType.isEntityType()) {
            appendEntityAttachment(body);
        } else if (valueType.isUnionType()) {
            if (kind == ParentPropertyKind.STANDARD) {
                appendUnionStandardAttachment(body);
            } else {
                appendUnionCollectionAttachment(body);
            }
        }
        // PrimitiveType: no parent tracking needed
    }

    private void appendEntityAttachment(BodyBuilder body) {
        String parentPropertyType = kindToString();
        String quotedName = propertyName != null ? "\"" + propertyName + "\"" : "null";
        body.addContext(Map.of(
                "quotedName", quotedName,
                "parentPropertyType", parentPropertyType
        ));
        body.ifElse(kind == ParentPropertyKind.MAP,
                () -> """
                        if (value != null) {
                            DataModelUtil.setParentMap(value, this, ${quotedName}, ParentPropertyType.${parentPropertyType}, name);
                        }
                        """,
                () -> """
                        if (value != null) {
                            DataModelUtil.setParent(value, this, ${quotedName}, ParentPropertyType.${parentPropertyType});
                        }
                        """);
    }

    /**
     * Union attachment for setter (standard) - has full isEntity/isEntityList/isEntityMap/else dispatch.
     */
    private void appendUnionStandardAttachment(BodyBuilder body) {
        body.addContext("propertyName", propertyName);
        body.appendBlock("""
                if (value != null) {
                    if (value.isEntity()) {
                        DataModelUtil.setParent(value, this, "${propertyName}", ParentPropertyType.standard);
                    } else if (value.isEntityList()) {
                        DataModelUtil.setParent(value, this, "${propertyName}", ParentPropertyType.standard);
                        List<?> entityList = (List<?>) ((UnionValue<?>) value).getValue();
                        for (Object entity : entityList) {
                            if (entity != null) {
                                DataModelUtil.setParent(entity, this, "${propertyName}", ParentPropertyType.array);
                            }
                        }
                    } else if (value.isEntityMap()) {
                        DataModelUtil.setParent(value, this, "${propertyName}", ParentPropertyType.standard);
                        Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) value).getValue();
                        Collection<String> keys = entityMap.keySet();
                        for (String key : keys) {
                            Object entity = entityMap.get(key);
                            if (entity != null) {
                                DataModelUtil.setParentMap(entity, this, "${propertyName}", ParentPropertyType.map, key);
                            }
                        }
                    } else {
                        DataModelUtil.setParent(value, this, "${propertyName}", ParentPropertyType.standard);
                    }
                }
                """);
    }

    /**
     * Union attachment for array/map collection methods - simpler isEntity/else dispatch.
     */
    private void appendUnionCollectionAttachment(BodyBuilder body) {
        String parentPropertyType = kindToString();
        body.addContext(Map.of(
                "propertyName", propertyName,
                "parentPropertyType", parentPropertyType
        ));
        body.ifElse(kind == ParentPropertyKind.MAP,
                () -> """
                        if (value != null) {
                            DataModelUtil.setParentMap(value, this, "${propertyName}", ParentPropertyType.${parentPropertyType}, name);
                        }
                        """,
                () -> """
                        if (value != null) {
                            DataModelUtil.setParent(value, this, "${propertyName}", ParentPropertyType.${parentPropertyType});
                        }
                        """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (valueType.isEntityType()) {
            JavaEnumSource parentPropertyTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getParentPropertyTypeEnumFQN());
            source.addImport(parentPropertyTypeSource);
            JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
            source.addImport(dataModelUtilSource);
        } else if (valueType.isUnionType()) {
            JavaEnumSource parentPropertyTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getParentPropertyTypeEnumFQN());
            source.addImport(parentPropertyTypeSource);
            JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
            source.addImport(dataModelUtilSource);
            JavaInterfaceSource unionValueSource = ctx.getJavaIndex().lookupInterface(ctx.getUnionValueInterfaceFQN());
            source.addImport(unionValueSource);

            if (kind == ParentPropertyKind.STANDARD) {
                source.addImport(Collection.class);
                source.addImport(List.class);
                source.addImport(Map.class);
            }
        }
    }

    private String kindToString() {
        return kind.value();
    }

}
