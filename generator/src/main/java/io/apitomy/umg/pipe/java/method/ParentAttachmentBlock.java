package io.apitomy.umg.pipe.java.method;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.type.Type;

/**
 * Generates parent tracking code for entity/union types via DataModelUtil.setParent.
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

    public ParentAttachmentBlock(Type valueType, String propertyName, ParentPropertyKind kind,
            CodeGenContext ctx) {
        this.valueType = valueType;
        this.propertyName = propertyName;
        this.kind = kind;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        if (!valueType.isEntityType() && !valueType.isUnionType()) {
            return;
        }

        String quotedName = propertyName != null ? "\"" + propertyName + "\"" : "null";
        body.addContext(Map.of(
                "quotedName", quotedName,
                "parentPropertyType", kind.value()
        ));

        body.ifElse(kind == ParentPropertyKind.MAP,
                () -> "DataModelUtil.setParentMap(value, this, ${quotedName}, ParentPropertyType.${parentPropertyType}, name);",
                () -> "DataModelUtil.setParent(value, this, ${quotedName}, ParentPropertyType.${parentPropertyType});");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        if (valueType.isEntityType() || valueType.isUnionType()) {
            JavaEnumSource parentPropertyTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getParentPropertyTypeEnumFQN());
            source.addImport(parentPropertyTypeSource);
            JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
            source.addImport(dataModelUtilSource);
        }
    }
}
