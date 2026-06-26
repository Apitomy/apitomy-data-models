package io.apitomy.umg.pipe.java.method.writer;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to write a map of union values to JSON using the type-based writer method.
 */
public class WriteUnionMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteUnionMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        io.apitomy.umg.models.concept.type.MapType mapType =
                (io.apitomy.umg.models.concept.type.MapType) property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var valueJt = ctx.getJavaTypeFactory().createJavaType(mapType.getValueType(), nsModel);
        String writeMethodName = "write" + valueJt.getSimpleName();

        valueJt.addImportsTo(writerClassSource);
        writerClassSource.addImport(JsonNode.class);
        writerClassSource.addImport(ObjectNode.class);
        writerClassSource.addImport(Map.class);

        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("writeMethodName", writeMethodName);
        body.addContext("unionJavaType", valueJt.toJavaTypeString());

        writerClassSource.addImport(Collection.class);

        body.append("{");
        body.append("    Map<String, ${unionJavaType}> items = node.${getterMethodName}();");
        body.append("    if (items != null && !items.isEmpty()) {");
        body.append("        ObjectNode mapJson = JsonUtil.objectNode();");
        body.append("        Collection<String> keys = items.keySet();");
        body.append("        keys.forEach(key -> {");
        body.append("            JsonNode value = this.${writeMethodName}(items.get(key));");
        body.append("            if (value != null) JsonUtil.setProperty(mapJson, key, value);");
        body.append("        });");
        body.append("        JsonUtil.setProperty(json, \"${propertyName}\", mapJson);");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
