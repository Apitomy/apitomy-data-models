package io.apitomy.umg.pipe.java.method.cloner;

import io.apitomy.umg.pipe.java.AbstractJavaStage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionVariantComparator;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Generates code to clone a union-typed property by dispatching on each variant type.
 */
public class CloneUnionPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource clonerClassSource;
    private final CodeGenContext ctx;

    public CloneUnionPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
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
        io.apitomy.umg.models.concept.type.UnionType effectiveUnionType = getEffectiveUnionType(property);

        body.addContext("unionJavaType", getUnionJavaTypeName(property, effectiveUnionType));
        body.addContext("getterMethodName", ctx.getterMethodName(property));
        body.addContext("setterMethodName", ctx.setterMethodName(property));

        body.append("{");
        body.append("    ${unionJavaType} srcUnion = source.${getterMethodName}();");
        body.append("    if (srcUnion != null) {");

        // Sort types alphabetically to match the old UnionPropertyType behavior
        List<Type> sortedTypes = effectiveUnionType.getTypes().stream()
                .sorted(UnionVariantComparator.INSTANCE)
                .collect(Collectors.toList());
        sortedTypes.forEach(nestedType -> {
            String typeName = AbstractJavaStage.getTypeName(nestedType);
            String isMethodName = "is" + typeName;
            String asMethodName = "as" + typeName;

            body.addContext("isMethodName", isMethodName);
            body.addContext("asMethodName", asMethodName);

            body.append("        if (srcUnion.${isMethodName}()) {");

            if (nestedType.isPrimitiveType() || nestedType.isPrimitiveUnionVariantType()) {
                String unionValueInterfaceFQN = ctx.getUnionTypeFQN(typeName + "UnionValue");
                String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                JavaInterfaceSource unionValueInterface = ctx.getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueInterface != null && unionValueClass != null) {
                    clonerClassSource.addImport(unionValueInterface);
                    clonerClassSource.addImport(unionValueClass);
                    body.addContext("unionValueInterfaceName", unionValueInterface.getName());
                    body.addContext("unionValueClassName", unionValueClass.getName());
                    body.append("            target.${setterMethodName}(new ${unionValueClassName}(srcUnion.${asMethodName}()));");
                }
            } else if (nestedType.isListType() && ((io.apitomy.umg.models.concept.type.ListType) nestedType).getValueType().isPrimitiveType()) {
                String unionValueName = AbstractJavaStage.getTypeName(nestedType);
                String unionValueInterfaceFQN = ctx.getUnionTypeFQN(unionValueName + "UnionValue");
                String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                JavaInterfaceSource unionValueInterface = ctx.getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueInterface != null && unionValueClass != null) {
                    clonerClassSource.addImport(unionValueInterface);
                    clonerClassSource.addImport(unionValueClass);
                    clonerClassSource.addImport(ArrayList.class);
                    body.addContext("unionValueInterfaceName", unionValueInterface.getName());
                    body.addContext("unionValueClassName", unionValueClass.getName());
                    body.append("            target.${setterMethodName}(new ${unionValueClassName}(new ArrayList<>(srcUnion.${asMethodName}())));");
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
                        body.append("            ${entityType} tgtEntity = new ${entityImplType}();");
                        body.append("            this.${cloneMethodName}((${entityType}) srcUnion.${asMethodName}(), tgtEntity);");
                        body.append("            target.${setterMethodName}(tgtEntity);");
                    }
                }
            } else if (nestedType.isListType() && ((io.apitomy.umg.models.concept.type.ListType) nestedType).getValueType().isEntityType()) {
                Type listItemType = ((io.apitomy.umg.models.concept.type.ListType) nestedType).getValueType();
                String listItemEntityName = entityModel.getNamespace().fullName() + "." + listItemType.getName();
                EntityModel listItemEntity = ctx.getConceptIndex().lookupEntity(listItemEntityName);
                if (listItemEntity != null) {
                    JavaInterfaceSource listItemEntitySource = ctx.getJavaIndex().lookupInterface(ctx.getJavaEntityInterfaceFQN(listItemEntity));
                    if (listItemEntitySource != null) {
                        String unionValueName = AbstractJavaStage.getTypeName(nestedType);
                        String unionValueInterfaceFQN = ctx.getUnionTypeFQN(unionValueName + "UnionValue");
                        String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                        JavaInterfaceSource unionValueInterface = ctx.getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                        JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
                        if (unionValueInterface == null) {
                            unionValueInterface = ctx.getJavaIndex().findInterfaceBySimpleName(unionValueName + "UnionValue");
                        }
                        if (unionValueClass == null) {
                            unionValueClass = ctx.getJavaIndex().findClassBySimpleName(unionValueName + "UnionValueImpl");
                        }
                        if (unionValueInterface != null && unionValueClass != null) {
                            clonerClassSource.addImport(listItemEntitySource);
                            clonerClassSource.addImport(unionValueInterface);
                            clonerClassSource.addImport(unionValueClass);
                            clonerClassSource.addImport(List.class);
                            clonerClassSource.addImport(ArrayList.class);
                            body.addContext("listItemType", listItemEntitySource.getName());
                            body.addContext("createMethodName", ctx.createMethodName(listItemEntity));
                            body.addContext("cloneMethodName", CloneEntityPropertyBlock.cloneMethodName(listItemEntity));
                            body.addContext("unionValueClassName", unionValueClass.getName());
                            body.append("            List<${listItemType}> clonedList = new ArrayList<>();");
                            body.append("            srcUnion.${asMethodName}().forEach(srcItem -> {");
                            body.append("                ${listItemType} tgtItem = (${listItemType}) target.${createMethodName}();");
                            body.append("                this.${cloneMethodName}((${listItemType}) srcItem, tgtItem);");
                            body.append("                clonedList.add(tgtItem);");
                            body.append("            });");
                            body.append("            @SuppressWarnings({ \"unchecked\", \"rawtypes\" })");
                            body.append("            ${unionValueClassName} unionValue = new ${unionValueClassName}((List) clonedList);");
                            body.append("            target.${setterMethodName}(unionValue);");
                        }
                    }
                }
            } else {
                ctx.warn("UNION property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
            }

            body.append("        }");
        });

        body.append("    }");
        body.append("}");

        var unionJavaType = ctx.getJavaTypeFactory().createJavaType(effectiveUnionType, nsContext);
        unionJavaType.addImportsTo(clonerClassSource);
    }

    private io.apitomy.umg.models.concept.type.UnionType getEffectiveUnionType(PropertyModel property) {
        Type resolved = property.getResolvedType();
        if (resolved.isUnionType()) {
            return (io.apitomy.umg.models.concept.type.UnionType) resolved;
        }
        if (resolved.isCollectionType()) {
            Type valueType = ((io.apitomy.umg.models.concept.type.CollectionType) resolved).getValueType();
            if (valueType.isUnionType()) {
                return (io.apitomy.umg.models.concept.type.UnionType) valueType;
            }
        }
        throw new IllegalStateException("Could not extract union type from: " + resolved);
    }

    private String getUnionJavaTypeName(PropertyModel property, io.apitomy.umg.models.concept.type.UnionType unionType) {
        var nsModel = propertyWithOrigin.getOrigin().getNamespace();
        var jt = ctx.getJavaTypeFactory().createJavaType(unionType, nsModel);
        jt.addImportsTo(clonerClassSource);
        return jt.getSimpleName();
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
