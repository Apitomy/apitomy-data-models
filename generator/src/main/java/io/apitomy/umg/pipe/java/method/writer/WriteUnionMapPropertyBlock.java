package io.apitomy.umg.pipe.java.method.writer;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to write a map of union values to JSON using the type-based writer method.
 */
public class WriteUnionMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource writerClassSource;
    private final CodeGenContext ctx;

    public WriteUnionMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource writerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.writerClassSource = writerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        io.apitomy.umg.models.concept.type.MapType mapType =
                (io.apitomy.umg.models.concept.type.MapType) property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var valueJt = ctx.getJavaTypeFactory().createJavaType(mapType.getValueType(), nsModel);
        String writeMethodName = "write" + valueJt.getSimpleName();

        valueJt.addImportsTo(writerClassSource);
        writerClassSource.addImport(JsonNode.class);
        writerClassSource.addImport(ObjectNode.class);
        writerClassSource.addImport(Map.class);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "getterMethodName", ctx.getterMethodName(property),
                "writeMethodName", writeMethodName,
                "unionJavaType", valueJt.toJavaTypeString()
        ));

        writerClassSource.addImport(Collection.class);

        body.appendBlock("""
                {
                    Map<String, ${unionJavaType}> items = node.${getterMethodName}();
                    if (items != null && !items.isEmpty()) {
                        ObjectNode mapJson = JsonUtil.objectNode();
                        Collection<String> keys = items.keySet();
                        keys.forEach(key -> {
                            JsonNode value = this.${writeMethodName}(items.get(key));
                            if (value != null) JsonUtil.setProperty(mapJson, key, value);
                        });
                        JsonUtil.setProperty(json, "${propertyName}", mapJson);
                    }
                }
                """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
