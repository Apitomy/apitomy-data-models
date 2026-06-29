package io.apitomy.umg.pipe.java.method.cloner;

import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeUtil;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Generates code to clone a regex-patterned property.
 * Handles entity and primitive/primitiveList/primitiveMap subcases.
 */
public class CloneRegexPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    public CloneRegexPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
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
            appendPrimitive(body, property);
        } else {
            prop.getCtx().warn("REGEX Entity property '" + property.getName()
                    + "' not cloned (unhandled) for entity: " + prop.getOwningEntity().fullyQualifiedName());
        }
    }

    private void appendEntity(BodyBuilder body, PropertyModel property) {
        var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                prop.getOwningEntity(), prop.getCtx(), "REGEX");
        if (resolved == null) {
            return;
        }
        JavaInterfaceSource commonEntityTypeJavaModel = prop.getCtx().resolveCommonJavaEntity(resolved.entityModel());

        clonerClassSource.addImport(resolved.javaInterface());
        clonerClassSource.addImport(commonEntityTypeJavaModel);
        clonerClassSource.addImport(Map.class);

        body.addContext(Map.of(
                "entityJavaType", resolved.javaInterface().getName(),
                "commonEntityType", commonEntityTypeJavaModel.getName(),
                "getterMethodName", new GetterMethod(property).getName(),
                "createMethodName", new FactoryMethod(resolved.entityModel().getName()).getName(),
                "cloneMethodName", new ClonerMethod(resolved.entityModel().getName()).getName(),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getCollection())).getName()
        ));

        body.appendBlock("""
{
    Map<String, ? extends ${commonEntityType}> srcMap = source.${getterMethodName}();
    if (srcMap != null && !srcMap.isEmpty()) {
        for (String name : srcMap.keySet()) {
            ${entityJavaType} srcItem = (${entityJavaType}) srcMap.get(name);
            ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();
            this.${cloneMethodName}(srcItem, tgtItem);
            target.${addMethodName}(name, tgtItem);
        }
    }
}
""");
    }

    private void appendPrimitive(BodyBuilder body, PropertyModel property) {
        clonerClassSource.addImport(Map.class);
        clonerClassSource.addImport(List.class);

        body.addContext(Map.of(
                "getterMethodName", new GetterMethod(property).getName(),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getCollection())).getName(),
                "valueType", PrimitiveTypeUtil.determineValueType(property.getResolvedType(), prop.getCtx(), clonerClassSource)
        ));

        body.appendBlock("""
{
    Map<String, ${valueType}> srcMap = source.${getterMethodName}();
    if (srcMap != null && !srcMap.isEmpty()) {
        List<String> keys = new java.util.ArrayList<>(srcMap.keySet());
        for (int _idx = 0; _idx < keys.size(); _idx++) {
            String name = keys.get(_idx);
            target.${addMethodName}(name, srcMap.get(name));
        }
    }
}
""");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
