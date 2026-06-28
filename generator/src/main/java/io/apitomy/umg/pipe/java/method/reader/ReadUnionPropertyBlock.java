package io.apitomy.umg.pipe.java.method.reader;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;
import io.apitomy.umg.pipe.java.method.SetterMethod;

/**
 * Generates code to read a union-typed property from JSON using the type-based reader method.
 */
public class ReadUnionPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadUnionPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type resolved = property.getResolvedType();
        var nsModel = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        var jt = prop.getCtx().getJavaTypeFactory().createJavaType(resolved, nsModel);
        String readMethodName = ReaderMethod.methodName(jt.getSimpleName());

        readerClassSource.addImport(JsonNode.class);
        jt.addImportsTo(readerClassSource);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "setterMethodName", SetterMethod.methodName(property),
                "readMethodName", readMethodName,
                "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
        ));

        body.appendBlock("""
                {
                    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
                    if (JsonUtil.isJsonNode(${varName})) {
                        node.${setterMethodName}(this.${readMethodName}(${varName}, null));
                        JsonUtil.removeProperty(json, "${propertyName}");
                    }
                }
                """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
