package io.apitomy.umg.pipe.java.method.writer;

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
 * Generates code to write a union-typed property to JSON using the type-based writer method.
 */
public class WriteUnionPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteUnionPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type resolved = property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var jt = ctx.getJavaTypeFactory().createJavaType(resolved, nsModel);
        String writeMethodName = "write" + jt.getSimpleName();

        jt.addImportsTo(writerClassSource);
        writerClassSource.addImport(JsonNode.class);

        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("writeMethodName", writeMethodName);

        body.append("{");
        body.append("    JsonNode value = this.${writeMethodName}(node.${getterMethodName}());");
        body.append("    if (value != null) JsonUtil.setProperty(json, \"${propertyName}\", value);");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
