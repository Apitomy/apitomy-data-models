package io.apitomy.umg.pipe.java.method.cloner;

import io.apitomy.umg.pipe.java.AbstractJavaStage;

import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionVariantComparator;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.UnionAsMethod;
import io.apitomy.umg.pipe.java.method.UnionIsMethod;

/**
 * Generates code to clone a list of union values.
 */
public class CloneUnionListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource clonerClassSource;
    private final CodeGenContext ctx;

    public CloneUnionListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource clonerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.clonerClassSource = clonerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        NamespaceModel nsContext = propertyWithOrigin.getOrigin().getNamespace();
        io.apitomy.umg.models.concept.type.UnionType effectiveUnionType = (io.apitomy.umg.models.concept.type.UnionType) ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();

        clonerClassSource.addImport(List.class);

        var unionJavaType = ctx.getJavaTypeFactory().createJavaType(effectiveUnionType, nsContext);
        unionJavaType.addImportsTo(clonerClassSource);
        body.addContext(Map.of(
                "unionJavaType", unionJavaType.getSimpleName(),
                "getterMethodName", GetterMethod.methodName(property),
                "addMethodName", AddMethod.methodName(ctx.singularize(property.getName()))
        ));

        body.append("{");
        body.append("    List<${unionJavaType}> srcList = source.${getterMethodName}();");
        body.append("    if (srcList != null && !srcList.isEmpty()) {");
        body.append("        srcList.forEach(srcUnion -> {");

        effectiveUnionType.getTypes().stream()
                .sorted(UnionVariantComparator.INSTANCE)
                .forEach(nestedType -> {
            String typeName = AbstractJavaStage.getTypeName(nestedType);
            String isMethodName = UnionIsMethod.methodName(typeName);
            String asMethodName = UnionAsMethod.methodName(typeName);

            body.addContext("isMethodName", isMethodName);
            body.addContext("asMethodName", asMethodName);

            body.append("            if (srcUnion.${isMethodName}()) {");

            if (nestedType.isPrimitiveType() || nestedType.isPrimitiveUnionVariantType()) {
                String unionValueInterfaceFQN = ctx.getUnionTypeFQN(typeName + "UnionValue");
                String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                JavaInterfaceSource unionValueInterface = ctx.getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueInterface != null && unionValueClass != null) {
                    clonerClassSource.addImport(unionValueInterface);
                    clonerClassSource.addImport(unionValueClass);
                    body.addContext("unionValueClassName", unionValueClass.getName());
                    body.append("                target.${addMethodName}(new ${unionValueClassName}(srcUnion.${asMethodName}()));");
                }
            } else if (nestedType.isEntityType()) {
                NamespaceModel nestedTypeEntityNS = entityModel.getNamespace();
                String nestedTypeEntityName = nestedTypeEntityNS.fullName() + "." + nestedType.getName();
                EntityModel nestedTypeEntity = ctx.getConceptIndex().lookupEntity(nestedTypeEntityName);
                if (nestedTypeEntity != null) {
                    JavaInterfaceSource entityJavaSource = ctx.resolveJavaEntityType(nestedTypeEntityNS, nestedType);
                    JavaClassSource entityImplSource = ctx.lookupJavaEntityImpl(ctx.getJavaEntityClassFQN(nestedTypeEntity));
                    if (entityJavaSource != null && entityImplSource != null) {
                        clonerClassSource.addImport(entityJavaSource);
                        clonerClassSource.addImport(entityImplSource);
                        body.addContext("entityType", entityJavaSource.getName());
                        body.addContext("entityImplType", entityImplSource.getName());
                        body.addContext("cloneMethodName", CloneEntityPropertyBlock.cloneMethodName(nestedTypeEntity));
                        body.append("                ${entityType} tgtItem = new ${entityImplType}();");
                        body.append("                this.${cloneMethodName}((${entityType}) srcUnion.${asMethodName}(), tgtItem);");
                        body.append("                target.${addMethodName}(tgtItem);");
                    }
                }
            } else {
                ctx.warn("UNION LIST property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
            }

            body.append("            }");
        });

        body.append("        });");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
