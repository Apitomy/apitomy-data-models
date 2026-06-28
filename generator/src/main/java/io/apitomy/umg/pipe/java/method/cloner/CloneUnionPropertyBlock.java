package io.apitomy.umg.pipe.java.method.cloner;

import io.apitomy.umg.pipe.java.AbstractJavaStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionVariantComparator;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.SetterMethod;
import io.apitomy.umg.pipe.java.method.UnionAsMethod;
import io.apitomy.umg.pipe.java.method.UnionIsMethod;

/**
 * Generates code to clone a union-typed property by dispatching on each variant type.
 */
public class CloneUnionPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource clonerClassSource;

    public CloneUnionPropertyBlock(PropertyCodeGen prop, JavaClassSource clonerClassSource) {
        this.prop = prop;
        this.clonerClassSource = clonerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        NamespaceModel nsContext = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        io.apitomy.umg.models.concept.type.UnionType effectiveUnionType = getEffectiveUnionType(property);

        body.addContext(Map.of(
                "unionJavaType", getUnionJavaTypeName(property, effectiveUnionType),
                "getterMethodName", GetterMethod.methodName(property),
                "setterMethodName", SetterMethod.methodName(property)
        ));

        body.appendBlock("""
{
    ${unionJavaType} srcUnion = source.${getterMethodName}();
    if (srcUnion != null) {
""");

        // Sort types alphabetically to match the old UnionPropertyType behavior
        List<Type> sortedTypes = effectiveUnionType.getTypes().stream()
                .sorted(UnionVariantComparator.INSTANCE)
                .collect(Collectors.toList());
        body.forEach(sortedTypes, (loopCtx, nestedType, isFirst) -> {
            String typeName = AbstractJavaStage.getTypeName(nestedType);
            String isMethodName = UnionIsMethod.methodName(typeName);
            String asMethodName = UnionAsMethod.methodName(typeName);

            loopCtx.set("isMethodName", isMethodName);
            loopCtx.set("asMethodName", asMethodName);

            if (nestedType.isPrimitiveType() || nestedType.isPrimitiveUnionVariantType()) {
                String unionValueInterfaceFQN = prop.getCtx().getUnionTypeFQN(typeName + "UnionValue");
                String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                JavaInterfaceSource unionValueInterface = prop.getCtx().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                JavaClassSource unionValueClass = prop.getCtx().getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueInterface != null && unionValueClass != null) {
                    clonerClassSource.addImport(unionValueInterface);
                    clonerClassSource.addImport(unionValueClass);
                    loopCtx.set("unionValueInterfaceName", unionValueInterface.getName());
                    loopCtx.set("unionValueClassName", unionValueClass.getName());
                    return """
        if (srcUnion.${isMethodName}()) {
            target.${setterMethodName}(new ${unionValueClassName}(srcUnion.${asMethodName}()));
        }
""";
                }
            } else if (nestedType.isListType() && ((io.apitomy.umg.models.concept.type.ListType) nestedType).getValueType().isPrimitiveType()) {
                String unionValueName = AbstractJavaStage.getTypeName(nestedType);
                String unionValueInterfaceFQN = prop.getCtx().getUnionTypeFQN(unionValueName + "UnionValue");
                String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                JavaInterfaceSource unionValueInterface = prop.getCtx().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                JavaClassSource unionValueClass = prop.getCtx().getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueInterface != null && unionValueClass != null) {
                    clonerClassSource.addImport(unionValueInterface);
                    clonerClassSource.addImport(unionValueClass);
                    clonerClassSource.addImport(ArrayList.class);
                    loopCtx.set("unionValueInterfaceName", unionValueInterface.getName());
                    loopCtx.set("unionValueClassName", unionValueClass.getName());
                    return """
        if (srcUnion.${isMethodName}()) {
            target.${setterMethodName}(new ${unionValueClassName}(new ArrayList<>(srcUnion.${asMethodName}())));
        }
""";
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
                        loopCtx.set("entityType", entityJavaSource.getName());
                        loopCtx.set("entityImplType", entityImplSource.getName());
                        loopCtx.set("cloneMethodName", CloneEntityPropertyBlock.cloneMethodName(nestedTypeEntity));
                        return """
        if (srcUnion.${isMethodName}()) {
            ${entityType} tgtEntity = new ${entityImplType}();
            this.${cloneMethodName}((${entityType}) srcUnion.${asMethodName}(), tgtEntity);
            target.${setterMethodName}(tgtEntity);
        }
""";
                    }
                }
            } else if (nestedType.isListType() && ((io.apitomy.umg.models.concept.type.ListType) nestedType).getValueType().isEntityType()) {
                Type listItemType = ((io.apitomy.umg.models.concept.type.ListType) nestedType).getValueType();
                String listItemEntityName = prop.getOwningEntity().getNamespace().fullName() + "." + listItemType.getName();
                EntityModel listItemEntity = prop.getCtx().getConceptIndex().lookupEntity(listItemEntityName);
                if (listItemEntity != null) {
                    JavaInterfaceSource listItemEntitySource = prop.getCtx().getJavaIndex().lookupInterface(prop.getCtx().getJavaEntityInterfaceFQN(listItemEntity));
                    if (listItemEntitySource != null) {
                        String unionValueName = AbstractJavaStage.getTypeName(nestedType);
                        String unionValueInterfaceFQN = prop.getCtx().getUnionTypeFQN(unionValueName + "UnionValue");
                        String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                        JavaInterfaceSource unionValueInterface = prop.getCtx().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                        JavaClassSource unionValueClass = prop.getCtx().getJavaIndex().lookupClass(unionValueClassFQN);
                        if (unionValueInterface == null) {
                            unionValueInterface = prop.getCtx().getJavaIndex().findInterfaceBySimpleName(unionValueName + "UnionValue");
                        }
                        if (unionValueClass == null) {
                            unionValueClass = prop.getCtx().getJavaIndex().findClassBySimpleName(unionValueName + "UnionValueImpl");
                        }
                        if (unionValueInterface != null && unionValueClass != null) {
                            clonerClassSource.addImport(listItemEntitySource);
                            clonerClassSource.addImport(unionValueInterface);
                            clonerClassSource.addImport(unionValueClass);
                            clonerClassSource.addImport(List.class);
                            clonerClassSource.addImport(ArrayList.class);
                            loopCtx.set("listItemType", listItemEntitySource.getName());
                            loopCtx.set("createMethodName", FactoryMethod.methodName(listItemEntity.getName()));
                            loopCtx.set("cloneMethodName", CloneEntityPropertyBlock.cloneMethodName(listItemEntity));
                            loopCtx.set("unionValueClassName", unionValueClass.getName());
                            return """
        if (srcUnion.${isMethodName}()) {
            List<${listItemType}> clonedList = new ArrayList<>();
            srcUnion.${asMethodName}().forEach(srcItem -> {
                ${listItemType} tgtItem = (${listItemType}) target.${createMethodName}();
                this.${cloneMethodName}((${listItemType}) srcItem, tgtItem);
                clonedList.add(tgtItem);
            });
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ${unionValueClassName} unionValue = new ${unionValueClassName}((List) clonedList);
            target.${setterMethodName}(unionValue);
        }
""";
                        }
                    }
                }
            } else {
                prop.getCtx().warn("UNION property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
            }

            return "";
        });

        body.appendBlock("""
    }
}
""");

        var unionJavaType = prop.getCtx().getJavaTypeFactory().createJavaType(effectiveUnionType, nsContext);
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
        var nsModel = prop.getPropertyWithOrigin().getOrigin().getNamespace();
        var jt = prop.getCtx().getJavaTypeFactory().createJavaType(unionType, nsModel);
        jt.addImportsTo(clonerClassSource);
        return jt.getSimpleName();
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to clonerClassSource during appendTo
    }
}
