package io.apitomy.umg.pipe.java.method.cloner;

import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to clone a primitive property from source to target:
 * {@code target.setX(source.getX());}
 */
public class ClonePrimitivePropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;

    public ClonePrimitivePropertyBlock(PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("setterMethodName", ctx.setterMethodName(property));
        body.append("target.${setterMethodName}(source.${getterMethodName}());");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }
}
