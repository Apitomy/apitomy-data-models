package io.apitomy.umg.pipe.java.method.cloner;

import java.util.ArrayList;
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
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.SetterMethod;

/**
 * Generates code to clone a list property (primitive list or entity list).
 */
public class CloneListPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    public CloneListPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
        this.prop = prop;
        this.clonerClassSource = clonerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();

        if (listValueType.isPrimitiveType()) {
            clonerClassSource.addImport(List.class);
            clonerClassSource.addImport(ArrayList.class);
            body.addContext(Map.of(
                    "getterMethodName", GetterMethod.methodName(property),
                    "setterMethodName", SetterMethod.methodName(property),
                    "valueType", PrimitiveTypeHelper.determineValueType(listValueType, prop.getCtx(), clonerClassSource)
            ));

            body.appendBlock("""
                    {
                        List<${valueType}> srcList = source.${getterMethodName}();
                        if (srcList != null) {
                            target.${setterMethodName}(new ArrayList<>(srcList));
                        }
                    }
                    """);
        } else if (listValueType.isEntityType()) {
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), prop.getOwningEntity(), prop.getCtx(), "LIST");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = prop.getCtx().resolveCommonJavaEntity(resolved.entityModel());
            clonerClassSource.addImport(resolved.javaInterface());
            clonerClassSource.addImport(commonEntityTypeJavaModel);
            clonerClassSource.addImport(List.class);

            body.addContext(Map.of(
                    "getterMethodName", GetterMethod.methodName(property),
                    "entityJavaType", resolved.javaInterface().getName(),
                    "commonEntityType", commonEntityTypeJavaModel.getName(),
                    "createMethodName", FactoryMethod.methodName(resolved.entityModel().getName()),
                    "cloneMethodName", ClonerMethod.methodName(resolved.entityModel().getName()),
                    "addMethodName", AddMethod.methodName(prop.getCtx().singularize(property.getName()))
            ));

            body.appendBlock("""
                    {
                        List<? extends ${commonEntityType}> srcList = source.${getterMethodName}();
                        if (srcList != null && !srcList.isEmpty()) {
                            srcList.forEach(srcItem -> {
                                ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();
                                this.${cloneMethodName}((${entityJavaType}) srcItem, tgtItem);
                                target.${addMethodName}(tgtItem);
                            });
                        }
                    }
                    """);
        } else {
            prop.getCtx().warn("LIST Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + prop.getOwningEntity().fullyQualifiedName());
        }
    }



    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
