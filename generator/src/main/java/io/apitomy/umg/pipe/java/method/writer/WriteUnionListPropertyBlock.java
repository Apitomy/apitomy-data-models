package io.apitomy.umg.pipe.java.method.writer;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.WriterMethod;

/**
 * Generates code to write a list of union values to JSON using the type-based writer method.
 */
public class WriteUnionListPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource writerClassSource;

    public WriteUnionListPropertyBlock(PropertyCodeGen prop, JavaClassSource writerClassSource) {
        this.prop = prop;
        this.writerClassSource = writerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        io.apitomy.umg.models.concept.type.ListType listType =
                (io.apitomy.umg.models.concept.type.ListType) property.getResolvedType();
        var nsModel = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        var valueJt = prop.getCtx().getJavaTypeFactory().createJavaType(listType.getValueType(), nsModel);
        String writeMethodName = WriterMethod.methodName(valueJt.getSimpleName());

        valueJt.addImportsTo(writerClassSource);
        writerClassSource.addImport(JsonNode.class);
        writerClassSource.addImport(ArrayNode.class);
        writerClassSource.addImport(List.class);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "getterMethodName", GetterMethod.methodName(property),
                "writeMethodName", writeMethodName,
                "unionJavaType", valueJt.toJavaTypeString()
        ));

        body.appendBlock("""
                {
                    List<${unionJavaType}> items = node.${getterMethodName}();
                    if (items != null && !items.isEmpty()) {
                        ArrayNode array = JsonUtil.arrayNode();
                        items.forEach(item -> {
                            JsonNode value = this.${writeMethodName}(item);
                            if (value != null) array.add(value);
                        });
                        JsonUtil.setProperty(json, "${propertyName}", array);
                    }
                }
                """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
