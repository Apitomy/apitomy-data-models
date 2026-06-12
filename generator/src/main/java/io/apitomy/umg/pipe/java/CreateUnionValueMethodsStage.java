package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * This stage creates method implementations for all union types.  This follows on from
 * the ApplyUnionTypesStage, which adds the union type interfaces to all implementation
 * classes that are needed.  But ApplyUnionTypesStage doesn't create the necessary method
 * implementations to actually implement the interface(s).  This stage is responsible
 * for that.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateUnionValueMethodsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            Collection<PropertyModelWithOrigin> entityProperties = getState().getConceptIndex().getAllEntityProperties(entity);
            entityProperties.stream().filter(pwo ->
                isUnion(pwo.getProperty()) || isUnionList(pwo.getProperty()) || isUnionMap(pwo.getProperty())
            ).forEach(pwo -> {
                createUnionValueMethods(pwo.getProperty(), entity, pwo.getOrigin().getNamespace());
            });
        });
    }

    /**
     * @param property
     * @param origin the leaf entity being processed
     * @param propertyOriginNamespace the namespace of the entity/trait that owns the property
     *        (may differ from origin's namespace if the property was normalized to a parent)
     */
    private void createUnionValueMethods(PropertyModel property, EntityModel origin,
            NamespaceModel propertyOriginNamespace) {
        // Also extract the resolved union type if available
        final UnionType resolvedUnionType = extractResolvedUnionType(property);

        // Extract the actual union type from PropertyType for legacy path
        final UnionPropertyType unionType;
        if (property.getType().isUnion() ||
                ((property.getType().isList() || property.getType().isMap()) &&
                        property.getType().getNested().iterator().next().isUnion())) {
            PropertyType actualUnionType = property.getType();
            if ((property.getType().isList() || property.getType().isMap())) {
                actualUnionType = property.getType().getNested().iterator().next();
            }
            unionType = new UnionPropertyType(actualUnionType);
        } else {
            unionType = null;
        }

        debug("Creating union value methods for property '" + property.getName() + "' of type '"
                + property.getRawType() +"' on entity: " + origin.fullyQualifiedName());

        // For type aliases, use the resolved union type exclusively
        if (unionType == null && resolvedUnionType != null) {
            resolvedUnionType.getTypes().forEach(variantType -> {
                if (variantType instanceof io.apitomy.umg.models.concept.type.PrimitiveType
                        || variantType instanceof io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType) {
                    String unionValueTypeName = JavaTypeFactory.getUnionComponentName(variantType);
                    String unionValueImplFQN = getUnionTypeFQN(unionValueTypeName + "UnionValueImpl");
                    JavaClassSource unionValueImplSource = getState().getJavaIndex().lookupClass(unionValueImplFQN);
                    if (unionValueImplSource != null) {
                        createUnionImplMethods(null, resolvedUnionType, unionValueImplSource, unionValueTypeName, false, origin.getNamespace());
                    }
                } else if (variantType instanceof io.apitomy.umg.models.concept.type.EntityType entityType) {
                    String entityName = entityType.getName();
                    String scopeNs = propertyOriginNamespace.fullName();
                    List<EntityModel> leafEntities = getState().getConceptIndex().findEntities("").stream()
                            .filter(e -> e.isLeaf() && e.getName().equals(entityName)
                                    && e.getNamespace().fullName().startsWith(scopeNs))
                            .collect(Collectors.toList());
                    for (EntityModel leafEntity : leafEntities) {
                        JavaClassSource implSource = resolveJavaEntityImpl(leafEntity);
                        if (implSource != null) {
                            createUnionImplMethods(null, resolvedUnionType, implSource, entityName, true, leafEntity.getNamespace());
                        }
                    }
                }
            });
            return;
        }

        if (unionType == null) {
            return;
        }

        unionType.getNestedTypes().forEach(nestedType -> {
            JavaType nestedJT = new JavaType(nestedType, origin.getNamespace());

            // For primitives, find the appropriate union value (wrapper) impl class
            // For entities, find ALL leaf entity implementation classes
            // For entity collections (maps/lists), find the appropriate union value (wrapper) class

            if (nestedJT.isPrimitive() || nestedJT.isPrimitiveList() || nestedJT.isPrimitiveMap()) {
                String unionValueTypeName = getTypeName(nestedType);
                String unionValueImplFQN = getUnionTypeFQN(unionValueTypeName + "UnionValueImpl");
                JavaClassSource unionValueImplSource = getState().getJavaIndex().lookupClass(unionValueImplFQN);
                if (unionValueImplSource == null) {
                    throw new RuntimeException("[CreateUnionValueMethodsStage] Union type value not supported: " + nestedType);
                }
                createUnionImplMethods(unionType, resolvedUnionType, unionValueImplSource, unionValueTypeName, false, origin.getNamespace());
            } else if (nestedJT.isEntity()) {
                // The union interface may have been applied to a common parent entity interface
                // (via ApplyUnionTypesStage after property normalization), which means ALL leaf
                // entity impl classes under that namespace inherit it and need the method
                // implementations.  Scope the search to the property origin's namespace to avoid
                // generating methods on entities from other specs that happen to share the same name.
                String entityName = nestedType.getSimpleType();
                String scopeNs = propertyOriginNamespace.fullName();
                List<EntityModel> leafEntities = getState().getConceptIndex().findEntities("").stream()
                        .filter(e -> e.isLeaf() && e.getName().equals(entityName)
                                && e.getNamespace().fullName().startsWith(scopeNs))
                        .collect(Collectors.toList());
                for (EntityModel leafEntity : leafEntities) {
                    JavaClassSource implSource = resolveJavaEntityImpl(leafEntity);
                    if (implSource != null) {
                        createUnionImplMethods(unionType, resolvedUnionType, implSource, entityName, true, leafEntity.getNamespace());
                    }
                }
            } else if (nestedJT.isEntityList()) {
                String unionValueTypeName = getTypeName(nestedType);
                String unionValueFQN = getUnionTypeFQN(unionValueTypeName + "UnionValueImpl");
                JavaClassSource unionValueImplSource = getState().getJavaIndex().lookupClass(unionValueFQN);
                if (unionValueImplSource == null) {
                    throw new RuntimeException("[CreateUnionValueMethodsStage] Union type value not supported: " + nestedType);
                }
                createUnionImplMethods(unionType, resolvedUnionType, unionValueImplSource, unionValueTypeName, false, origin.getNamespace());
            } else {
                throw new RuntimeException("[CreateUnionValueMethodsStage] Union type value not supported: " + nestedType);
            }
        });
    }

    private void createUnionImplMethods(UnionPropertyType unionType, UnionType resolvedUnionType,
            JavaClassSource unionValueClass,
            String unionValueTypeName, boolean unionValueIsEntity, NamespaceModel nsContext) {

        if (resolvedUnionType != null) {
            var sortedTypes = new java.util.ArrayList<>(resolvedUnionType.getTypes());
            sortedTypes.sort(java.util.Comparator.comparing(t -> JavaTypeFactory.getUnionComponentName(t).toLowerCase()));
            sortedTypes.forEach(variantType -> {
                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                String isMethodName = "is" + typeName;
                String asMethodName = "as" + typeName;

                var jt = getJavaTypeFactory().createJavaType(variantType, nsContext, true);
                String asMethodReturnType = jt.toJavaTypeString();

                if (!unionValueClass.hasMethodSignature(isMethodName)) {
                    MethodSource<JavaClassSource> isMethod = unionValueClass.addMethod().setName(isMethodName).setReturnType(boolean.class).setPublic();
                    isMethod.addAnnotation(Override.class);
                    BodyBuilder isMethodBody = new BodyBuilder();

                    MethodSource<JavaClassSource> asMethod = unionValueClass.addMethod().setName(asMethodName).setReturnType(asMethodReturnType).setPublic();
                    asMethod.addAnnotation(Override.class);
                    BodyBuilder asMethodBody = new BodyBuilder();

                    if (typeName.equals(unionValueTypeName)) {
                        isMethodBody.append("return true;");
                        isMethod.setBody(isMethodBody.toString());
                        if (variantType instanceof io.apitomy.umg.models.concept.type.EntityType) {
                            asMethodBody.append("return this;");
                        } else {
                            asMethodBody.append("return getValue();");
                        }
                        asMethod.setBody(asMethodBody.toString());
                    } else {
                        isMethodBody.append("return false;");
                        isMethod.setBody(isMethodBody.toString());
                        asMethodBody.append("throw new ClassCastException();");
                        asMethod.setBody(asMethodBody.toString());
                    }

                    jt.addImportsTo(unionValueClass);
                }
            });
        } else {
            createUnionImplMethodsLegacy(unionType, unionValueClass, unionValueTypeName, nsContext);
        }

        // If this is an entity, add the "unionValue" method.
        if (unionValueIsEntity && !unionValueClass.hasMethodSignature("unionValue")) {
            MethodSource<JavaClassSource> unionValueMethod = unionValueClass.addMethod().setName("unionValue").setReturnType("Object").setPublic();
            unionValueMethod.addAnnotation(Override.class);
            BodyBuilder unionValueMethodBody = new BodyBuilder();
            unionValueMethodBody.append("return this;");
            unionValueMethod.setBody(unionValueMethodBody.toString());
        }

    }

    private void createUnionImplMethodsLegacy(UnionPropertyType unionType, JavaClassSource unionValueClass,
            String unionValueTypeName, NamespaceModel nsContext) {

        unionType.getNestedTypes().forEach(nestedType -> {
            String typeName = getTypeName(nestedType);
            String isMethodName = "is" + typeName;
            String asMethodName = "as" + typeName;

            JavaType jt = new JavaType(nestedType, nsContext.fullName()).useCommonEntityResolution();

            String asMethodReturnType = jt.toJavaTypeString();

            if (!unionValueClass.hasMethodSignature(isMethodName)) {
                // Create the "isXyz" method for this type
                MethodSource<JavaClassSource> isMethod = unionValueClass.addMethod().setName(isMethodName).setReturnType(boolean.class).setPublic();
                isMethod.addAnnotation(Override.class);
                BodyBuilder isMethodBody = new BodyBuilder();

                // Create the "asXyz" method for this type
                MethodSource<JavaClassSource> asMethod = unionValueClass.addMethod().setName(asMethodName).setReturnType(asMethodReturnType).setPublic();
                asMethod.addAnnotation(Override.class);
                BodyBuilder asMethodBody = new BodyBuilder();

                if (typeName.equals(unionValueTypeName)) {
                    isMethodBody.append("return true;");
                    isMethod.setBody(isMethodBody.toString());
                    if (nestedType.isEntityType()) {
                        asMethodBody.append("return this;");
                    } else {
                        asMethodBody.append("return getValue();");
                    }
                    asMethod.setBody(asMethodBody.toString());
                } else {
                    isMethodBody.append("return false;");
                    isMethod.setBody(isMethodBody.toString());
                    asMethodBody.append("throw new ClassCastException();");
                    asMethod.setBody(asMethodBody.toString());
                }

                jt.addImportsTo(unionValueClass);
            }
        });
    }

    private UnionType extractResolvedUnionType(PropertyModel property) {
        if (property.getResolvedType() == null) {
            return null;
        }
        Type resolved = property.getResolvedType();
        if (resolved instanceof CollectionType collectionType) {
            resolved = collectionType.getValueType();
        }
        return resolved instanceof UnionType ut ? ut : null;
    }
}
