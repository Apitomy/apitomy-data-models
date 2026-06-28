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
        body.append("if (value != null) {");
        body.append("    ((NodeImpl) value)._setParent(this);");
        body.append("    ((NodeImpl) value)._setParentPropertyName(" + quotedName + ");");
        body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType." + parentPropertyType + ");");
        if (kind == ParentPropertyKind.MAP) {
            body.append("    ((NodeImpl) value)._setMapPropertyName(name);");
        }
        body.append("}");
    }

    /**
     * Union attachment for setter (standard) - has full isEntity/isEntityList/isEntityMap/else dispatch.
     */
    private void appendUnionStandardAttachment(BodyBuilder body) {
        String quotedName = "\"" + propertyName + "\"";
        body.append("if (value != null) {");
        body.append("    if (value.isEntity()) {");
        body.append("        ((NodeImpl) value)._setParent(this);");
        body.append("        ((NodeImpl) value)._setParentPropertyName(" + quotedName + ");");
        body.append("        ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);");
        body.append("    } else if (value.isEntityList()) {");
        body.append("        ((UnionValueImpl<?>) value)._setParent(this);");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyName(" + quotedName + ");");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);");
        body.append("        List<?> entityList = (List<?>) ((UnionValue<?>) value).getValue();");
        body.append("        for (Object entity : entityList) {");
        body.append("            if (entity != null) {");
        body.append("                ((NodeImpl) entity)._setParent(this);");
        body.append("                ((NodeImpl) entity)._setParentPropertyName(" + quotedName + ");");
        body.append("                ((NodeImpl) entity)._setParentPropertyType(ParentPropertyType.array);");
        body.append("            }");
        body.append("        }");
        body.append("    } else if (value.isEntityMap()) {");
        body.append("        ((UnionValueImpl<?>) value)._setParent(this);");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyName(" + quotedName + ");");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);");
        body.append("        Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) value).getValue();");
        body.append("        Collection<String> keys = entityMap.keySet();");
        body.append("        for (String key : keys) {");
        body.append("            NodeImpl entity = (NodeImpl) entityMap.get(key);");
        body.append("            if (entity != null) {");
        body.append("                entity._setParent(this);");
        body.append("                entity._setParentPropertyName(" + quotedName + ");");
        body.append("                entity._setParentPropertyType(ParentPropertyType.map);");
        body.append("                entity._setMapPropertyName(key);");
        body.append("            }");
        body.append("        }");
        body.append("    } else {");
        body.append("        ((UnionValueImpl<?>) value)._setParent(this);");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyName(" + quotedName + ");");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType.standard);");
        body.append("    }");
        body.append("}");
    }

    /**
     * Union attachment for array/map collection methods - simpler isEntity/else dispatch.
     */
    private void appendUnionCollectionAttachment(BodyBuilder body) {
        String parentPropertyType = kindToString();
        String quotedName = "\"" + propertyName + "\"";
        body.append("if (value != null) {");
        body.append("    if (value.isEntity()) {");
        body.append("        ((NodeImpl) value)._setParent(this);");
        body.append("        ((NodeImpl) value)._setParentPropertyName(" + quotedName + ");");
        body.append("        ((NodeImpl) value)._setParentPropertyType(ParentPropertyType." + parentPropertyType + ");");
        if (kind == ParentPropertyKind.MAP) {
            body.append("        ((NodeImpl) value)._setMapPropertyName(name);");
        }
        body.append("    } else {");
        body.append("        ((UnionValueImpl<?>) value)._setParent(this);");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyName(" + quotedName + ");");
        body.append("        ((UnionValueImpl<?>) value)._setParentPropertyType(ParentPropertyType." + parentPropertyType + ");");
        if (kind == ParentPropertyKind.MAP) {
            body.append("        ((UnionValueImpl<?>) value)._setMapPropertyName(name);");
        }
        body.append("    }");
        body.append("}");
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
