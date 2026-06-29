package io.apitomy.umg.pipe.java.method.writer;

import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.WriterMethod;

/**
 * Generates code to write a star (*) property to JSON.
 * Handles entity, primitive, primitive-list, and primitive-map subcases.
 */
public class WriteStarPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource writerClassSource;

    public WriteStarPropertyBlock(PropertyCodeGen prop, JavaClassSource writerClassSource) {
        this.prop = prop;
        this.writerClassSource = writerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type resolvedType = property.getResolvedType();

        if (resolvedType.isEntityType()) {
            appendEntity(body, property);
        } else if (resolvedType.isPrimitiveType()) {
            appendPrimitive(body, property);
        } else if (resolvedType.isPrimitiveListType()) {
            appendPrimitiveList(body, property);
        } else if (resolvedType.isPrimitiveMapType()) {
            appendPrimitiveMap(body, property);
        } else {
            prop.getCtx().warn("STAR Entity property '" + property.getName()
                    + "' not written (unhandled) for entity: " + prop.getOwningEntity().fullyQualifiedName());
            prop.getCtx().warn("       property type: " + resolvedType);
        }
    }

    private void appendEntity(BodyBuilder body, PropertyModel property) {
        var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                prop.getOwningEntity(), prop.getCtx(), "STAR");
        if (resolved == null) {
            return;
        }

        writerClassSource.addImport(List.class);
        writerClassSource.addImport(resolved.javaInterface());

        body.addContext("writeMethodName", new WriterMethod(resolved.entityModel().getName()).getName());
        body.addContext("entityJavaType", resolved.javaInterface().getName());

        body.appendBlock("""
{
    List<String> propertyNames = node.getItemNames();
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String propertyName = propertyNames.get(_i);
        ObjectNode object = JsonUtil.objectNode();
        this.${writeMethodName}((${entityJavaType}) node.getItem(propertyName), object);
        JsonUtil.setProperty(json, propertyName, object);
    }
}
""");
    }

    private void appendPrimitive(BodyBuilder body, PropertyModel property) {
        writerClassSource.addImport(List.class);

        body.addContext("valueType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), prop.getCtx(), writerClassSource));

        body.appendBlock("""
{
    List<String> propertyNames = node.getItemNames();
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String propertyName = propertyNames.get(_i);
        ${valueType} value = node.getItem(propertyName);
        JsonUtil.setProperty(json, propertyName, JsonUtil.toJsonNode(value));
    }
}
""");
    }

    private void appendPrimitiveList(BodyBuilder body, PropertyModel property) {
        writerClassSource.addImport(List.class);

        body.addContext("valueType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), prop.getCtx(), writerClassSource));

        body.appendBlock("""
{
    List<String> propertyNames = node.getItemNames();
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String propertyName = propertyNames.get(_i);
        ${valueType} value = node.getItem(propertyName);
        JsonUtil.setProperty(json, propertyName, JsonUtil.toArrayNode(value));
    }
}
""");
    }

    private void appendPrimitiveMap(BodyBuilder body, PropertyModel property) {
        writerClassSource.addImport(List.class);
        writerClassSource.addImport(Map.class);

        body.addContext("valueType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), prop.getCtx(), writerClassSource));

        body.appendBlock("""
{
    List<String> propertyNames = node.getItemNames();
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String propertyName = propertyNames.get(_i);
        ${valueType} value = node.getItem(propertyName);
        JsonUtil.setProperty(json, propertyName, JsonUtil.toObject(value));
    }
}
""");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
