package io.apitomy.umg.pipe.java.method.cloner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeUtil;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Generates code to clone a map property (primitive map or entity map).
 */
public class CloneMapPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    public CloneMapPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
        this.prop = prop;
        this.clonerClassSource = clonerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type mapValueType = ((MapType) property.getResolvedType()).getValueType();

        if (mapValueType.isPrimitiveType()) {
            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(LinkedHashMap.class);
            body.addContext(Map.of(
                    "getterMethodName", prop.getGetterName(),
                    "setterMethodName", prop.getSetterName(),
                    "valueType", PrimitiveTypeUtil.determineValueType(mapValueType, prop.getCtx(), clonerClassSource)
            ));

            body.appendBlock("""
                    {
                        Map<String, ${valueType}> srcMap = source.${getterMethodName}();
                        if (srcMap != null && !srcMap.isEmpty()) {
                            target.${setterMethodName}(new LinkedHashMap<>(srcMap));
                        }
                    }
                    """);
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, prop.getOwningEntity(), prop.getCtx(), "MAP");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = prop.getCtx().resolveCommonJavaEntity(resolved.entityModel());

            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(resolved.javaInterface());
            clonerClassSource.addImport(commonEntityTypeJavaModel);

            body.addContext(Map.of(
                    "getterMethodName", prop.getGetterName(),
                    "entityJavaType", resolved.javaInterface().getName(),
                    "commonEntityType", commonEntityTypeJavaModel.getName(),
                    "createMethodName", new FactoryMethod(entityTypeName).getName(),
                    "cloneMethodName", new ClonerMethod(entityTypeName).getName(),
                    "addMethodName", new AddMethod(prop.getCtx().singularize(property.getName())).getName()
            ));

            body.appendBlock("""
                    {
                        Map<String, ? extends ${commonEntityType}> srcMap = source.${getterMethodName}();
                        if (srcMap != null && !srcMap.isEmpty()) {
                            srcMap.keySet().forEach(name -> {
                                ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();
                                this.${cloneMethodName}((${entityJavaType}) srcMap.get(name), tgtItem);
                                target.${addMethodName}(name, tgtItem);
                            });
                        }
                    }
                    """);
        } else {
            prop.getCtx().warn("MAP Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + prop.getOwningEntity().fullyQualifiedName());
        }
    }



    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
