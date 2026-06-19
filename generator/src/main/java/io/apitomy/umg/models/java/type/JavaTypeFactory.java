package io.apitomy.umg.models.java.type;

import io.apitomy.umg.index.java.JavaIndex;
import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.concept.SpecificationIndex;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.PrimitiveType;
import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Creates {@link JavaType} instances from concept {@link Type} objects by resolving
 * entity references against the Java index.
 * <p>
 * This replaces the inner {@code JavaType} class in {@code AbstractJavaStage} with
 * a proper factory that uses the resolved Type hierarchy.
 */
public class JavaTypeFactory {

    private final ConceptIndex conceptIndex;
    private final SpecificationIndex specIndex;
    private final JavaIndex javaIndex;
    private final String unionTypesPackage;

    public JavaTypeFactory(ConceptIndex conceptIndex, SpecificationIndex specIndex,
                           JavaIndex javaIndex, String unionTypesPackage) {
        this.conceptIndex = conceptIndex;
        this.specIndex = specIndex;
        this.javaIndex = javaIndex;
        this.unionTypesPackage = unionTypesPackage;
    }

    /**
     * Create a JavaType from a concept Type, resolving entity references
     * using the namespace context's prefix to construct the Java FQN.
     * This matches the old behavior where the origin entity's namespace
     * determines the prefix used.
     */
    public JavaType createJavaType(Type type, NamespaceModel namespaceContext) {
        return createJavaType(type, namespaceContext, false);
    }

    /**
     * Create a JavaType from a concept Type.
     *
     * @param useCommonResolution if true, resolve entities to their most-parent
     *                            common entity (used for union type stages).
     */
    public JavaType createJavaType(Type type, NamespaceModel namespaceContext, boolean useCommonResolution) {
        if (type instanceof PrimitiveType primitive) {
            return new PrimitiveJavaType(primitive);
        }

        if (type instanceof EntityType entityType) {
            var interfaceSource = resolveEntityInterface(entityType, namespaceContext, useCommonResolution);
            return new EntityJavaType(entityType, interfaceSource);
        }

        if (type instanceof ListType listType) {
            var valueJavaType = createJavaType(listType.getValueType(), namespaceContext, useCommonResolution);
            return new ListJavaType(listType, valueJavaType);
        }

        if (type instanceof MapType mapType) {
            var valueJavaType = createJavaType(mapType.getValueType(), namespaceContext, useCommonResolution);
            return new MapJavaType(mapType, valueJavaType);
        }

        if (type instanceof UnionType unionType) {
            return createUnionJavaType(unionType, namespaceContext, useCommonResolution);
        }

        if (type instanceof PrimitiveUnionVariantType variant) {
            return new PrimitiveJavaType(variant.getType());
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getClass().getSimpleName());
    }

    private UnionJavaType createUnionJavaType(UnionType unionType, NamespaceModel namespaceContext,
                                               boolean useCommonResolution) {
        var variantJavaTypes = unionType.getTypes().stream()
                .map(t -> createJavaType(t, namespaceContext, useCommonResolution))
                .collect(Collectors.toList());

        String unionName;
        if (unionType.getAliasName() != null) {
            unionName = unionType.getAliasName();
        } else {
            var sortedTypes = new ArrayList<>(unionType.getTypes());
            sortedTypes.sort(Comparator.comparing(t -> getUnionComponentName(t).toLowerCase()));
            unionName = sortedTypes.stream()
                    .map(JavaTypeFactory::getUnionComponentName)
                    .collect(Collectors.joining()) + "Union";
        }
        String unionPackage = resolveUnionPackage(unionType);
        var unionFQN = unionPackage + "." + unionName;

        return new UnionJavaType(unionType, unionName, unionFQN, variantJavaTypes);
    }

    private String resolveUnionPackage(UnionType unionType) {
        for (var variant : unionType.getTypes()) {
            EntityType entityType = null;
            if (variant instanceof EntityType et) {
                entityType = et;
            } else if (variant instanceof ListType lt && lt.getValueType() instanceof EntityType et) {
                entityType = et;
            } else if (variant instanceof MapType mt && mt.getValueType() instanceof EntityType et) {
                entityType = et;
            }
            if (entityType != null) {
                var common = conceptIndex.lookupCommonEntity(
                        unionType.getNamespace(), entityType.getName());
                if (common != null) {
                    return common.getNamespace().fullName();
                }
            }
        }
        return unionTypesPackage;
    }

    public static String getUnionComponentName(Type type) {
        if (type instanceof EntityType entityType) {
            return entityType.getName();
        } else if (type instanceof PrimitiveType primitiveType) {
            return StringUtils.capitalize(primitiveType.name().toLowerCase());
        } else if (type instanceof PrimitiveUnionVariantType variant) {
            return StringUtils.capitalize(variant.getType().name().toLowerCase());
        } else if (type instanceof ListType listType) {
            return getUnionComponentName(listType.getValueType()) + "List";
        } else if (type instanceof MapType mapType) {
            return getUnionComponentName(mapType.getValueType()) + "Map";
        } else if (type instanceof UnionType unionType) {
            return unionType.getTypes().stream()
                    .map(JavaTypeFactory::getUnionComponentName)
                    .collect(Collectors.joining()) + "Union";
        }
        throw new IllegalArgumentException("Unsupported type for union naming: " + type);
    }

    private JavaInterfaceSource resolveEntityInterface(EntityType entityType, NamespaceModel namespaceContext,
                                                        boolean useCommonResolution) {
        var entityName = entityType.getName();
        if (useCommonResolution) {
            var commonEntity = conceptIndex.lookupCommonEntity(namespaceContext.fullName(), entityName);
            if (commonEntity != null) {
                return lookupJavaEntityInterface(commonEntity);
            }
        }
        // Default: resolve using the namespace context's prefix, matching old behavior
        // where the origin entity's namespace determines the Java type prefix
        return lookupJavaEntityInterfaceByNamespace(namespaceContext.fullName(), entityName);
    }

    private JavaInterfaceSource lookupJavaEntityInterfaceByNamespace(String namespace, String entityName) {
        var prefix = specIndex.prefixForNS(namespace);
        var fqn = namespace + "." + (prefix == null ? "" : prefix) + entityName;
        var result = javaIndex.lookupInterface(fqn);
        if (result == null) {
            throw new RuntimeException("Java interface not found: " + fqn);
        }
        return result;
    }

    private JavaInterfaceSource lookupJavaEntityInterface(EntityModel entity) {
        var namespace = entity.getNamespace().fullName();
        var prefix = specIndex.prefixForNS(namespace);
        var fqn = namespace + "." + (prefix == null ? "" : prefix) + entity.getName();
        var result = javaIndex.lookupInterface(fqn);
        if (result == null) {
            throw new RuntimeException("Java interface not found for entity: " + fqn);
        }
        return result;
    }
}
