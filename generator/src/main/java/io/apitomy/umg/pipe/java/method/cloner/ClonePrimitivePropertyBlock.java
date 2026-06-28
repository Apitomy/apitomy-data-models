package io.apitomy.umg.pipe.java.method.cloner;

import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.SetterMethod;

/**
 * Generates code to clone a primitive property from source to target:
 * {@code target.setX(source.getX());}
 */
public class ClonePrimitivePropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;

    public ClonePrimitivePropertyBlock(PropertyCodeGen prop) {
        this.prop = prop;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        body.addContext("getterMethodName", GetterMethod.methodName(property));
        body.addContext("setterMethodName", SetterMethod.methodName(property));
        body.append("target.${setterMethodName}(source.${getterMethodName}());");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed
    }
}
