package io.apitomy.umg.pipe.java.method.writer;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.WriterMethod;

/**
 * Generates code to write a union-typed property to JSON using the type-based writer method.
 */
public class WriteUnionPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource writerClassSource;

    public WriteUnionPropertyBlock(PropertyCodeGen prop, JavaClassSource writerClassSource) {
        this.prop = prop;
        this.writerClassSource = writerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type resolved = property.getResolvedType();
        var nsModel = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        var jt = prop.getCtx().getJavaTypeFactory().createJavaType(resolved, nsModel);
        String writeMethodName = WriterMethod.methodName(jt.getSimpleName());

        jt.addImportsTo(writerClassSource);
        writerClassSource.addImport(JsonNode.class);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "getterMethodName", prop.getGetterName(),
                "writeMethodName", writeMethodName
        ));

        body.appendBlock("""
                {
                    JsonNode value = this.${writeMethodName}(node.${getterMethodName}());
                    if (value != null) JsonUtil.setProperty(json, "${propertyName}", value);
                }
                """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
