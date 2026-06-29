package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read a list of union values from JSON using the type-based reader method.
 */
public class ReadUnionListPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadUnionListPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        io.apitomy.umg.models.concept.type.ListType listType =
                (io.apitomy.umg.models.concept.type.ListType) property.getResolvedType();
        var nsModel = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        var valueJt = prop.getCtx().getJavaTypeFactory().createJavaType(listType.getValueType(), nsModel);
        String readMethodName = new ReaderMethod(valueJt.getSimpleName()).getName();

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(ArrayList.class);
        valueJt.addImportsTo(readerClassSource);

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getName())).getName(),
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
