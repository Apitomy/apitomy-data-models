package io.apitomy.umg.pipe.java.method.reader;

import java.util.List;

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
 * Generates code to read a map of union values from JSON using the type-based reader method.
 */
public class ReadUnionMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadUnionMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        io.apitomy.umg.models.concept.type.MapType mapType =
                (io.apitomy.umg.models.concept.type.MapType) property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var valueJt = ctx.getJavaTypeFactory().createJavaType(mapType.getValueType(), nsModel);
        String readMethodName = "read" + valueJt.getSimpleName();

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(ObjectNode.class);
        readerClassSource.addImport(List.class);
        valueJt.addImportsTo(readerClassSource);

        body.addContext("propertyName", property.getName());
        body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));
        body.addContext("readMethodName", readMethodName);
        body.addContext("unionJavaType", valueJt.toJavaTypeString());

        body.append("{");
        body.append("    ObjectNode mapObj = JsonUtil.consumeObjectProperty(json, \"${propertyName}\");");
        body.append("    if (mapObj != null) {");
        body.append("        JsonUtil.keys(mapObj).forEach(key -> {");
        body.append("            JsonNode value = JsonUtil.consumeAnyProperty(mapObj, key);");
        body.append("            if (value != null) {");
        body.append("                ${unionJavaType} model = this.${readMethodName}(value, null);");
        body.append("                if (model != null) node.${addMethodName}(key, model);");
        body.append("            }");
        body.append("        });");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
