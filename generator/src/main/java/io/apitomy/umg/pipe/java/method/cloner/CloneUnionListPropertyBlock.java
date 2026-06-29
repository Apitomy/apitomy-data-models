package io.apitomy.umg.pipe.java.method.cloner;

import io.apitomy.umg.pipe.java.method.TypeNameUtil;

import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.concept.type.UnionVariantComparator;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.UnionAsMethod;
import io.apitomy.umg.pipe.java.method.UnionIsMethod;

/**
 * Generates code to clone a list of union values.
 */
public class CloneUnionListPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    public CloneUnionListPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
        this.prop = prop;
        this.clonerClassSource = clonerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        NamespaceModel nsContext = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        UnionType effectiveUnionType = (UnionType) ((ListType) property.getResolvedType()).getValueType();

        clonerClassSource.addImport(List.class);

        var unionJavaType = prop.getCtx().getJavaTypeFactory().createJavaType(effectiveUnionType, nsContext);
        unionJavaType.addImportsTo(clonerClassSource);
        body.addContext(Map.of(
                "unionJavaType", unionJavaType.getSimpleName(),
                "getterMethodName", prop.getGetterName(),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getName())).getName()
        ));

        body.appendBlock("""
{
    List<${unionJavaType}> srcList = source.${getterMethodName}();
    if (srcList != null && !srcList.isEmpty()) {
        for (int _idx = 0; _idx < srcList.size(); _idx++) {
            ${unionJavaType} srcUnion = srcList.get(_idx);
""");

        effectiveUnionType.getTypes().stream()
                .sorted(UnionVariantComparator.INSTANCE)
                .forEach(nestedType -> {
            String typeName = TypeNameUtil.getTypeName(nestedType);
            String isMethodName = new UnionIsMethod(typeName).getName();
            String asMethodName = new UnionAsMethod(typeName).getName();

            body.addContext("isMethodName", isMethodName);
            body.addContext("asMethodName", asMethodName);

            body.append("            if (srcUnion.${isMethodName}()) {");

            if (nestedType.isPrimitiveType() || nestedType.isPrimitiveUnionVariantType()) {
                String unionValueInterfaceFQN = prop.getCtx().getUnionTypeFQN(typeName + "UnionValue");
                String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                JavaInterfaceSource unionValueInterface = prop.getCtx().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                JavaClassSource unionValueClass = prop.getCtx().getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueInterface != null && unionValueClass != null) {
                    clonerClassSource.addImport(unionValueInterface);
                    clonerClassSource.addImport(unionValueClass);
                    body.addContext("unionValueClassName", unionValueClass.getName());
                    body.append("                target.${addMethodName}(new ${unionValueClassName}(srcUnion.${asMethodName}()));");
                }
            } else if (nestedType.isEntityType()) {
                NamespaceModel nestedTypeEntityNS = prop.getOwningEntity().getNamespace();
                String nestedTypeEntityName = nestedTypeEntityNS.fullName() + "." + nestedType.getName();
                EntityModel nestedTypeEntity = prop.getCtx().getConceptIndex().lookupEntity(nestedTypeEntityName);
                if (nestedTypeEntity != null) {
                    JavaInterfaceSource entityJavaSource = prop.getCtx().resolveJavaEntityType(nestedTypeEntityNS, nestedType);
                    JavaClassSource entityImplSource = prop.getCtx().lookupJavaEntityImpl(prop.getCtx().getJavaEntityClassFQN(nestedTypeEntity));
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
                prop.getCtx().warn("UNION LIST property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
            }

            body.append("            }");
        });

        body.append("        }");
        body.append("    }");
        body.append("}");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
