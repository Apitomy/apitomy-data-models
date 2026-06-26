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

        body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

        body.append("{");
        body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
        body.append("    if (JsonUtil.isObject(${varName})) {");
        body.append("        ObjectNode _obj = (ObjectNode) ${varName};");
        body.append("        List<String> _keys = JsonUtil.keys(_obj);");
        body.append("        for (int _i = 0; _i < _keys.size(); _i++) {");
        body.append("            String _key = _keys.get(_i);");
        body.append("            JsonNode _val = JsonUtil.getProperty(_obj, _key);");
        body.append("            if (JsonUtil.isJsonNode(_val)) {");
        body.append("                ${unionJavaType} model = this.${readMethodName}(_val, null);");
        body.append("                if (model != null) node.${addMethodName}(_key, model);");
        body.append("            }");
        body.append("        }");
        body.append("        json.remove(\"${propertyName}\");");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
