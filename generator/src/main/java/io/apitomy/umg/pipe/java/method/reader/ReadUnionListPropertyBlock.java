package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read a list of union values from JSON using the type-based reader method.
 */
public class ReadUnionListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadUnionListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        io.apitomy.umg.models.concept.type.ListType listType =
                (io.apitomy.umg.models.concept.type.ListType) property.getResolvedType();
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var valueJt = ctx.getJavaTypeFactory().createJavaType(listType.getValueType(), nsModel);
        String readMethodName = ReaderMethod.methodName(valueJt.getSimpleName());

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(ArrayList.class);
        valueJt.addImportsTo(readerClassSource);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "addMethodName", AddMethod.methodName(ctx.singularize(property.getName())),
                "readMethodName", readMethodName,
                "unionJavaType", valueJt.toJavaTypeString(),
                "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
        ));

        body.appendBlock("""
                {
                    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
                    if (JsonUtil.isArray(${varName})) {
                        List<JsonNode> _nodes = JsonUtil.toList(${varName});
                        List<${unionJavaType}> _items = new ArrayList<>();
                        boolean _valid = true;
                        for (int _i = 0; _i < _nodes.size(); _i++) {
                            ${unionJavaType} _result = this.${readMethodName}(_nodes.get(_i), null);
                            if (_result == null) { _valid = false; break; }
                            _items.add(_result);
                        }
                        if (_valid) {
                            for (int _i = 0; _i < _items.size(); _i++) {
                                node.${addMethodName}(_items.get(_i));
                            }
                            JsonUtil.removeProperty(json, "${propertyName}");
                        }
                    }
                }
                """);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
