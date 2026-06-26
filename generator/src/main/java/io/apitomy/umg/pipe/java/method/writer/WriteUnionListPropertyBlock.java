package io.apitomy.umg.pipe.java.method.writer;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to write a list of union values to JSON using the type-based writer method.
 */
public class WriteUnionListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteUnionListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        io.apitomy.umg.models.concept.type.ListType listType =
                (io.apitomy.umg.models.concept.type.ListType) property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var valueJt = ctx.getJavaTypeFactory().createJavaType(listType.getValueType(), nsModel);
        String writeMethodName = "write" + valueJt.getSimpleName();

        valueJt.addImportsTo(writerClassSource);
        writerClassSource.addImport(JsonNode.class);
        writerClassSource.addImport(ArrayNode.class);
        writerClassSource.addImport(List.class);

        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("writeMethodName", writeMethodName);
        body.addContext("unionJavaType", valueJt.toJavaTypeString());

        body.append("{");
        body.append("    List<${unionJavaType}> items = node.${getterMethodName}();");
        body.append("    if (items != null && !items.isEmpty()) {");
        body.append("        ArrayNode array = JsonUtil.arrayNode();");
        body.append("        items.forEach(item -> {");
        body.append("            JsonNode value = this.${writeMethodName}(item);");
        body.append("            if (value != null) array.add(value);");
        body.append("        });");
        body.append("        JsonUtil.setProperty(json, \"${propertyName}\", array);");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
