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
        STANDARD, ARRAY, MAP
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
                            ((NodeImpl) value)._setParent(this);
                            ((NodeImpl) value)._setParentPropertyName(${quotedName});
                            ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.${parentPropertyType});
                            ((NodeImpl) value)._setMapPropertyName(name);
                        }
                        """,
                () -> """
                        if (value != null) {
                            ((NodeImpl) value)._setParent(this);
                            ((NodeImpl) value)._setParentPropertyName(${quotedName});
                            ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.${parentPropertyType});
                        }
                        """);
    }

    /**
     * Union attachment for setter (standard) - has full isEntity/isEntityList/isEntityMap/else dispatch.
     */
    private void appendUnionStandardAttachment(BodyBuilder body) {
        body.addContext("quotedName", "\"" + propertyName + "\"");
        body.appendBlock("""
                if (value != null) {
                    if (value.isEntity()) {
                        ((NodeImpl) value)._setParent(this);
                        ((NodeImpl) value)._setParentPropertyName(${quotedName});
                        ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
                    } else if (value.isEntityList()) {
                        ((UnionValueImpl<?>) value)._setParent(this);
                        ((UnionValueImpl<?>) value)._setParentPropertyName(${quotedName});
                        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);
                        List<?> entityList = (List<?>) ((UnionValue<?>) value).getValue();
                        for (Object entity : entityList) {
                            if (entity != null) {
                                ((NodeImpl) entity)._setParent(this);
                                ((NodeImpl) entity)._setParentPropertyName(${quotedName});
                                ((NodeImpl) entity)._setParentPropertyType(ParentPropertyType.array);
                            }
                        }
                    } else if (value.isEntityMap()) {
                        ((UnionValueImpl<?>) value)._setParent(this);
                        ((UnionValueImpl<?>) value)._setParentPropertyName(${quotedName});
                        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);
                        Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) value).getValue();
                        Collection<String> keys = entityMap.keySet();
                        for (String key : keys) {
                            NodeImpl entity = (NodeImpl) entityMap.get(key);
                            if (entity != null) {
                                entity._setParent(this);
                                entity._setParentPropertyName(${quotedName});
                                entity._setParentPropertyType(ParentPropertyType.map);
                                entity._setMapPropertyName(key);
                            }
                        }
                    } else {
                        ((UnionValueImpl<?>) value)._setParent(this);
                        ((UnionValueImpl<?>) value)._setParentPropertyName(${quotedName});
                        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);
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
                "quotedName", "\"" + propertyName + "\"",
                "parentPropertyType", parentPropertyType
        ));
        body.ifElse(kind == ParentPropertyKind.MAP,
                () -> """
                        if (value != null) {
                            if (value.isEntity()) {
                                ((NodeImpl) value)._setParent(this);
                                ((NodeImpl) value)._setParentPropertyName(${quotedName});
                                ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.${parentPropertyType});
                                ((NodeImpl) value)._setMapPropertyName(name);
                            } else {
                                ((UnionValueImpl<?>) value)._setParent(this);
                                ((UnionValueImpl<?>) value)._setParentPropertyName(${quotedName});
                                ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.${parentPropertyType});
                                ((UnionValueImpl<?>) value)._setMapPropertyName(name);
                            }
                        }
                        """,
                () -> """
                        if (value != null) {
                            if (value.isEntity()) {
                                ((NodeImpl) value)._setParent(this);
                                ((NodeImpl) value)._setParentPropertyName(${quotedName});
                                ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.${parentPropertyType});
                            } else {
                                ((UnionValueImpl<?>) value)._setParent(this);
                                ((UnionValueImpl<?>) value)._setParentPropertyName(${quotedName});
                                ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.${parentPropertyType});
                            }
                        }
                        """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (valueType.isEntityType()) {
            JavaEnumSource parentPropertyTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getParentPropertyTypeEnumFQN());
            source.addImport(parentPropertyTypeSource);
            JavaClassSource nodeImplSource = ctx.getJavaIndex().lookupClass(ctx.getNodeEntityClassFQN());
            source.addImport(nodeImplSource);
        } else if (valueType.isUnionType()) {
            JavaEnumSource parentPropertyTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getParentPropertyTypeEnumFQN());
            source.addImport(parentPropertyTypeSource);
            JavaClassSource nodeImplSource = ctx.getJavaIndex().lookupClass(ctx.getNodeEntityClassFQN());
            source.addImport(nodeImplSource);
            JavaInterfaceSource unionValueSource = ctx.getJavaIndex().lookupInterface(ctx.getUnionValueInterfaceFQN());
            source.addImport(unionValueSource);
            JavaClassSource unionValueImplSource = ctx.getJavaIndex().lookupClass(ctx.getUnionTypeFQN("UnionValueImpl"));
            source.addImport(unionValueImplSource);

            if (kind == ParentPropertyKind.STANDARD) {
                source.addImport(Collection.class);
                source.addImport(List.class);
                source.addImport(Map.class);
            }
        }
    }

    private String kindToString() {
        return switch (kind) {
            case STANDARD -> "standard";
            case ARRAY -> "array";
            case MAP -> "map";
        };
    }

}
