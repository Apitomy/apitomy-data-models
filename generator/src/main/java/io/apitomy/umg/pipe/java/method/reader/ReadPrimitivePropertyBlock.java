package io.apitomy.umg.pipe.java.method.reader;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;

/**
 * Generates code to read a primitive-typed property from JSON.
 */
public class ReadPrimitivePropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadPrimitivePropertyBlock(PropertyModelWithOrigin propertyWithOrigin, JavaClassSource readerClassSource,
            CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("valueType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), ctx, readerClassSource));
        body.addContext("consumeProperty", PrimitiveTypeHelper.determineConsumePropertyVariant(property.getResolvedType(), ctx, readerClassSource));
        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));

        body.append("{");
        body.append("    ${valueType} value = JsonUtil.${consumeProperty}(json, \"${propertyName}\");");
        body.append("    node.${setterMethodName}(value);");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
