package io.apitomy.umg.pipe.java.method.reader;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to read a union-typed property from JSON using the type-based reader method.
 */
public class ReadUnionPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadUnionPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type resolved = property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var jt = ctx.getJavaTypeFactory().createJavaType(resolved, nsModel);
        String readMethodName = "read" + jt.getSimpleName();

        readerClassSource.addImport(JsonNode.class);
        jt.addImportsTo(readerClassSource);

        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));
        body.addContext("readMethodName", readMethodName);

        body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

        body.append("{");
        body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
        body.append("    if (JsonUtil.isJsonNode(${varName})) {");
        body.append("        node.${setterMethodName}(this.${readMethodName}(${varName}, null));");
        body.append("        json.remove(\"${propertyName}\");");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
