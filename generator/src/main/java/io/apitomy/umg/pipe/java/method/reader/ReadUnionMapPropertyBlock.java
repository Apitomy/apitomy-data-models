package io.apitomy.umg.pipe.java.method.reader;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read a map of union values from JSON using the type-based reader method.
 */
public class ReadUnionMapPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadUnionMapPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        io.apitomy.umg.models.concept.type.MapType mapType =
                (io.apitomy.umg.models.concept.type.MapType) property.getResolvedType();
        var nsModel = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        var valueJt = prop.getCtx().getJavaTypeFactory().createJavaType(mapType.getValueType(), nsModel);
        String readMethodName = new ReaderMethod(valueJt.getSimpleName()).getName();

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(ObjectNode.class);
        readerClassSource.addImport(List.class);
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
                    if (JsonUtil.isObject(${varName})) {
                        ObjectNode _obj = JsonUtil.toObject(${varName});
                        List<String> _keys = JsonUtil.keys(_obj);
                        for (int _i = 0; _i < _keys.size(); _i++) {
                            String _key = _keys.get(_i);
                            JsonNode _val = JsonUtil.getProperty(_obj, _key);
                            if (JsonUtil.isJsonNode(_val)) {
                                ${unionJavaType} model = this.${readMethodName}(_val, null);
                                if (model != null) node.${addMethodName}(_key, model);
                            }
                        }
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
