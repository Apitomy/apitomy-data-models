package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;

/**
 * Creates the union type interface.  For example, if the union type is "boolean|[string]" then
 * a new Java interface named "BooleanStringListUnion" will be created to represent a property
 * that can be either a boolean or a string list.
 *
 * Also for example, if there is a union type property that is "number|Widget", then a new Java
 * interface named "NumberWidgetUnion" will be created to represent that property (the value of
 * which can be either a number or a Widget entity).
 */
public class CreateUnionTypesStage extends AbstractUnionTypeJavaStage {

    @Override
    protected void doProcess(PropertyModelWithOrigin property) {
        createUnionType(property);
    }

    /**
     * @param property
     */
    private void createUnionType(PropertyModelWithOrigin property) {
        debug("Creating union type for: " + property.getProperty().getName());

        // Extract the resolved union type
        Type resolvedType = property.getProperty().getResolvedType();
        if (resolvedType instanceof CollectionType collectionType) {
            resolvedType = collectionType.getValueType();
        }

        String name;
        if (resolvedType instanceof UnionType resolvedUnionType && resolvedUnionType.getAliasName() != null) {
            name = resolvedUnionType.getAliasName();
        } else {
            // Extract the actual union type from PropertyType for anonymous unions
            PropertyType actualUnionType = property.getProperty().getType();
            if (isUnionList(property.getProperty()) || isUnionMap(property.getProperty())) {
                actualUnionType = property.getProperty().getType().getNested().iterator().next();
            }
            UnionPropertyType ut = new UnionPropertyType(actualUnionType);
            name = ut.getName();
        }

        String _package = getUnionTypesPackageName();

        // Skip if already created (type aliases may be referenced by multiple properties)
        if (getState().getJavaIndex().lookupInterface(_package + "." + name) != null) {
            return;
        }

        // Create the main union type interface
        JavaInterfaceSource unionTypeInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(name)
                .setPublic();

        // If this union is the root type, it must extend RootCapable
        if (resolvedType instanceof UnionType ut && ut.isRoot()) {
            String rootCapableFQN = getRootNodeInterfaceFQN();
            JavaInterfaceSource rootCapableSource = getState().getJavaIndex().lookupInterface(rootCapableFQN);
            unionTypeInterface.addImport(rootCapableSource);
            unionTypeInterface.addInterface(rootCapableSource);
        }

        // It must extend the "Union" interface
        String unionFQN = getUnionInterfaceFQN();
        JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(unionFQN);
        unionTypeInterface.addImport(unionValueSource);
        unionTypeInterface.addInterface(unionValueSource);

        // Create the union methods
        if (resolvedType instanceof UnionType resolvedUnionType) {
            createUnionMethods(resolvedUnionType, unionTypeInterface, property.getOrigin().getNamespace());
        } else {
            PropertyType actualUnionType = property.getProperty().getType();
            if (isUnionList(property.getProperty()) || isUnionMap(property.getProperty())) {
                actualUnionType = property.getProperty().getType().getNested().iterator().next();
            }
            createUnionMethods(new UnionPropertyType(actualUnionType), unionTypeInterface, property.getOrigin().getNamespace());
        }

        getState().getJavaIndex().index(unionTypeInterface);
    }

    private void createUnionMethods(UnionPropertyType unionType, JavaInterfaceSource unionTypeInterface, NamespaceModel nsContext) {
        unionType.getNestedTypes().forEach(nestedType -> {
            String typeName = getTypeName(nestedType);
            String isMethodName = "is" + typeName;
            String asMethodName = "as" + typeName;

            JavaType jt = new JavaType(nestedType, nsContext.fullName()).useCommonEntityResolution();

            String asMethodReturnType = jt.toJavaTypeString();

            unionTypeInterface.addMethod().setName(isMethodName).setReturnType(boolean.class).setPublic();
            unionTypeInterface.addMethod().setName(asMethodName).setReturnType(asMethodReturnType).setPublic();
            jt.addImportsTo(unionTypeInterface);
        });
    }

    private void createUnionMethods(UnionType resolvedUnionType, JavaInterfaceSource unionTypeInterface, NamespaceModel nsContext) {
        var sortedTypes = new java.util.ArrayList<>(resolvedUnionType.getTypes());
        sortedTypes.sort(java.util.Comparator.comparing(t -> io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(t).toLowerCase()));
        sortedTypes.forEach(variantType -> {
            String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);
            String isMethodName = "is" + typeName;
            String asMethodName = "as" + typeName;

            var jt = getJavaTypeFactory().createJavaType(variantType, nsContext, true);
            String asMethodReturnType = jt.toJavaTypeString();

            unionTypeInterface.addMethod().setName(isMethodName).setReturnType(boolean.class).setPublic();
            unionTypeInterface.addMethod().setName(asMethodName).setReturnType(asMethodReturnType).setPublic();
            jt.addImportsTo(unionTypeInterface);
        });
    }
}
