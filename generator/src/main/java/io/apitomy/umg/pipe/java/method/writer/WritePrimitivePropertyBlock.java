package io.apitomy.umg.pipe.java.method.writer;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeUtil;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Generates code to write a primitive-typed property to JSON.
 */
public class WritePrimitivePropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource writerClassSource;

    public WritePrimitivePropertyBlock(PropertyCodeGen prop, JavaClassSource writerClassSource) {
        this.prop = prop;
        this.writerClassSource = writerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", prop.getGetterName());

        body.ifElse(PrimitiveTypeUtil.isJsonNodeType(property.getResolvedType(), prop.getCtx()),
                // ObjectNode and JsonNode are already JsonNode subtypes, use setProperty directly
                () -> "JsonUtil.setProperty(json, \"${propertyName}\", node.${getterMethodName}());",
                () -> "JsonUtil.setProperty(json, \"${propertyName}\", JsonUtil.toJsonNode(node.${getterMethodName}()));");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
