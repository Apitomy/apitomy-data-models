package io.apitomy.umg.pipe.java.method.cloner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;

/**
 * Generates code to clone a map property (primitive map or entity map).
 */
public class CloneMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource clonerClassSource;
    private final CodeGenContext ctx;

    public CloneMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource clonerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.clonerClassSource = clonerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();

        if (mapValueType.isPrimitiveType()) {
            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(LinkedHashMap.class);
            body.addContext(Map.of(
                    "getterMethodName", ctx.getterMethodName(property),
                    "setterMethodName", ctx.setterMethodName(property),
                    "valueType", PrimitiveTypeHelper.determineValueType(mapValueType, ctx, clonerClassSource)
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
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, entityModel, ctx, "MAP");
            if (resolved == null) {
                return;
            }
            JavaInterfaceSource commonEntityTypeJavaModel = ctx.resolveCommonJavaEntity(resolved.entityModel());

            clonerClassSource.addImport(Map.class);
            clonerClassSource.addImport(resolved.javaInterface());
            clonerClassSource.addImport(commonEntityTypeJavaModel);

            body.addContext(Map.of(
                    "getterMethodName", ctx.getterMethodName(property),
                    "entityJavaType", resolved.javaInterface().getName(),
                    "commonEntityType", commonEntityTypeJavaModel.getName(),
                    "createMethodName", "create" + entityTypeName,
                    "cloneMethodName", "clone" + entityTypeName,
                    "addMethodName", ctx.addMethodName(ctx.singularize(property.getName()))
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
            ctx.warn("MAP Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + entityModel.fullyQualifiedName());
        }
    }

    

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
