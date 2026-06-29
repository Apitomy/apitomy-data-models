package io.apitomy.umg.pipe.java.method.writer;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeUtil;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.WriterMethod;

/**
 * Generates code to write a map property to JSON (primitive map or entity map).
 */
public class WriteMapPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource writerClassSource;

    public WriteMapPropertyBlock(PropertyCodeGen prop, JavaClassSource writerClassSource) {
        this.prop = prop;
        this.writerClassSource = writerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        body.addContext("propertyName", property.getName());
        body.addContext("getterMethodName", prop.getGetterName());

        Type mapValueType = ((MapType) property.getResolvedType()).getValueType();
        if (mapValueType.isPrimitiveType()) {
            body.append("JsonUtil.setProperty(json, \"${propertyName}\", JsonUtil.toObjectNode(node.${getterMethodName}()));");
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, prop.getOwningEntity(), prop.getCtx(), "MAP");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = prop.getCtx().resolveCommonJavaEntity(resolved.entityModel());

            writerClassSource.addImport(Map.class);
            writerClassSource.addImport(resolved.javaInterface());
            writerClassSource.addImport(commonEntityTypeJavaModel);

            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "getterMethodName", prop.getGetterName(),
                    "mapValueJavaType", resolved.javaInterface().getName(),
                    "writeMethodName", new WriterMethod(entityTypeName).getName(),
                    "mapValueCommonJavaType", commonEntityTypeJavaModel.getName()
            ));

            body.appendBlock("""
                    {
                        Map<String, ? extends ${mapValueCommonJavaType}> models = node.${getterMethodName}();
                        if (models != null && !models.isEmpty()) {
                            ObjectNode object = JsonUtil.objectNode();
                            for (String jsonName : models.keySet()) {
                                ObjectNode jsonValue = JsonUtil.objectNode();
                                this.${writeMethodName}((${mapValueJavaType}) models.get(jsonName), jsonValue);
                                JsonUtil.setProperty(object, jsonName, jsonValue);
                            }
                            JsonUtil.setProperty(json, "${propertyName}", object);
                        }
                    }
                    """);
        } else {
            prop.getCtx().warn("MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + prop.getOwningEntity().fullyQualifiedName());
            prop.getCtx().warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
