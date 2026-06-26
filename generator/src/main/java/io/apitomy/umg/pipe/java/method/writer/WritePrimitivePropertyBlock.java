package io.apitomy.umg.pipe.java.method.writer;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;

/**
 * Generates code to write a primitive-typed property to JSON.
 */
public class WritePrimitivePropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WritePrimitivePropertyBlock(PropertyModelWithOrigin propertyWithOrigin, JavaClassSource writerClassSource,
            CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("setPropertyMethodName", PrimitiveTypeHelper.determineSetPropertyVariant(property.getResolvedType(), ctx, writerClassSource));
        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", ctx.getterMethodName(property));

        body.append("JsonUtil.${setPropertyMethodName}(json, \"${propertyName}\", node.${getterMethodName}());");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
