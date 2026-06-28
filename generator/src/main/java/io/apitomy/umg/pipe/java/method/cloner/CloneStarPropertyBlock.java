package io.apitomy.umg.pipe.java.method.cloner;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Generates code to clone a star (*) property.
 * Handles entity and primitive/primitiveList/primitiveMap subcases.
 */
public class CloneStarPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    public CloneStarPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
        this.prop = prop;
        this.clonerClassSource = clonerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type resolvedType = property.getResolvedType();

        if (resolvedType.isEntityType()) {
            appendEntity(body, property);
        } else if (resolvedType.isPrimitiveType() || resolvedType.isPrimitiveListType() || resolvedType.isPrimitiveMapType()) {
            appendPrimitive(body);
        } else {
            prop.getCtx().warn("STAR Entity property '" + property.getName()
                    + "' not cloned (unhandled) for entity: " + prop.getOwningEntity().fullyQualifiedName());
        }
    }

    private void appendEntity(BodyBuilder body, PropertyModel property) {
        var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                prop.getOwningEntity(), prop.getCtx(), "STAR");
        if (resolved == null) {
            return;
        }
        clonerClassSource.addImport(resolved.javaInterface());
        clonerClassSource.addImport(List.class);

        body.addContext("entityJavaType", resolved.javaInterface().getName());
        body.addContext("createMethodName", FactoryMethod.methodName(resolved.entityModel().getName()));
        body.addContext("cloneMethodName", ClonerMethod.methodName(resolved.entityModel().getName()));

        body.appendBlock("""
{
    List<String> itemNames = source.getItemNames();
    if (itemNames != null) {
        itemNames.forEach(name -> {
            ${entityJavaType} srcItem = (${entityJavaType}) source.getItem(name);
            if (srcItem != null) {
                ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();
                this.${cloneMethodName}(srcItem, tgtItem);
                target.addItem(name, tgtItem);
            }
        });
    }
}
""");
    }

    private void appendPrimitive(BodyBuilder body) {
        clonerClassSource.addImport(List.class);

        body.appendBlock("""
{
    List<String> itemNames = source.getItemNames();
    if (itemNames != null) {
        itemNames.forEach(name -> {
            target.addItem(name, source.getItem(name));
        });
    }
}
""");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
