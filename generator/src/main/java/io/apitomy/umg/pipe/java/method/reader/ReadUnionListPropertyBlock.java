package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to read a list of union values from JSON using the type-based reader method.
 */
public class ReadUnionListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadUnionListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        io.apitomy.umg.models.concept.type.ListType listType =
                (io.apitomy.umg.models.concept.type.ListType) property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var valueJt = ctx.getJavaTypeFactory().createJavaType(listType.getValueType(), nsModel);
        String readMethodName = "read" + valueJt.getSimpleName();

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(ArrayList.class);
        valueJt.addImportsTo(readerClassSource);

        body.addContext("propertyName", property.getName());
        body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));
        body.addContext("readMethodName", readMethodName);
        body.addContext("unionJavaType", valueJt.toJavaTypeString());

        body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

        body.append("{");
        body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
        body.append("    if (JsonUtil.isArray(${varName})) {");
        body.append("        List<JsonNode> _nodes = JsonUtil.toList(${varName});");
        body.append("        List<${unionJavaType}> _items = new ArrayList<>();");
        body.append("        boolean _valid = true;");
        body.append("        for (int _i = 0; _i < _nodes.size(); _i++) {");
        body.append("            ${unionJavaType} _result = this.${readMethodName}(_nodes.get(_i), null);");
        body.append("            if (_result == null) { _valid = false; break; }");
        body.append("            _items.add(_result);");
        body.append("        }");
        body.append("        if (_valid) {");
        body.append("            for (int _i = 0; _i < _items.size(); _i++) {");
        body.append("                node.${addMethodName}(_items.get(_i));");
        body.append("            }");
        body.append("            JsonUtil.removeProperty(json, \"${propertyName}\");");
        body.append("        }");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
