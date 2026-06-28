package io.apitomy.umg.pipe.java.method.writer;

import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.WriterMethod;

/**
 * Generates code to write an entity-typed property to JSON.
 */
public class WriteEntityPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource writerClassSource;

    public WriteEntityPropertyBlock(PropertyCodeGen prop, JavaClassSource writerClassSource) {
        this.prop = prop;
        this.writerClassSource = writerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        var resolved = EntityResolver.resolveEntityInterface(prop.getPropertyWithOrigin(), prop.getOwningEntity(), prop.getCtx(), "");
        if (resolved == null) {
            return;
        }
        writerClassSource.addImport(resolved.javaInterface());

        body.addContext(Map.of(
                "propertyName", property.getName(),
                "getterMethodName", prop.getGetterName(),
                "writeMethodName", writeMethodName(resolved.entityModel()),
                "propertyTypeJavaEntity", resolved.javaInterface().getName()
        ));

        body.appendBlock("""
{
    if (node.${getterMethodName}() != null) {
        ObjectNode object = JsonUtil.objectNode();
        this.${writeMethodName}((${propertyTypeJavaEntity}) node.${getterMethodName}(), object);
        JsonUtil.setProperty(json, "${propertyName}", object);
    }
}
""");
    }

    static String writeMethodName(EntityModel entityModel) {
        return WriterMethod.methodName(entityModel.getName());
    }

    static String writeMethodName(String entityName) {
        return WriterMethod.methodName(entityName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to writerClassSource during appendTo
    }
}
