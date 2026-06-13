package io.apitomy.umg.pipe.concept;

import io.apitomy.umg.beans.Property;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.PrimitiveType;
import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.models.concept.type.RawType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.pipe.AbstractStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Creates property models AND resolves their types into the new Type hierarchy.
 * <p>
 * This stage replaces {@link CreatePropertyModelsStage} with an extended version that:
 * <ol>
 *   <li>Creates {@link PropertyModel} with parsed {@link PropertyType} (legacy, for backward compat)</li>
 *   <li>Also resolves each property's type into a {@link Type} object and sets {@link PropertyModel#setResolvedType}</li>
 *   <li>Indexes named types (type aliases) in the concept index for later reference</li>
 * </ol>
 * <p>
 * Type resolution means entity type names are looked up in the concept index to get actual
 * {@link EntityModel} references, and union/list/map types build a proper type tree.
 */
public class CreatePropertyAndTypeModelsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        info("-- Creating Property and Type Models --");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            var namespace = specVersion.getNamespace();

            // Process type aliases FIRST so they're available for property type resolution
            if (specVersion.getTypeAliases() != null) {
                specVersion.getTypeAliases().forEach(alias -> {
                    var rawType = RawType.parse(alias.getType());
                    var resolvedType = resolveType(rawType, namespace);
                    if (resolvedType instanceof UnionType unionType) {
                        unionType.setAliasName(alias.getName());
                        if (alias.getUnionRules() != null && !alias.getUnionRules().isEmpty()) {
                            unionType.setUnionRules(alias.getUnionRules());
                        }
                    }
                    var fqName = namespace + "." + alias.getName();
                    getState().getConceptIndex().indexType(fqName, resolvedType);
                    debug("Indexed type alias: %s -> %s", fqName, resolvedType);
                });
            }

            // Process trait properties
            specVersion.getTraits().forEach(trait -> {
                var fqTraitName = namespace + "." + trait.getName();
                var traitModel = getState().getConceptIndex().lookupTrait(fqTraitName);
                trait.getProperties().forEach(property -> {
                    var propertyModel = createPropertyModel(property, namespace, false, null);
                    traitModel.getProperties().put(property.getName(), propertyModel);
                });
            });

            // Process entity properties
            specVersion.getEntities().forEach(entity -> {
                var fqEntityName = namespace + "." + entity.getName();
                var entityModel = getState().getConceptIndex().lookupEntity(fqEntityName);
                entity.getProperties().forEach(property -> {
                    var propertyModel = createPropertyModel(property, namespace, true, entityModel);
                    entityModel.getProperties().put(property.getName(), propertyModel);
                });
            });

            // Process root type — mark root-capable entities
            if (specVersion.getRoot() != null && specVersion.getRoot().getType() != null) {
                processRootType(specVersion.getRoot().getType(), namespace);
            }
        });
    }

    private void processRootType(String rootTypeName, String namespace) {
        // Try type alias first
        var rootType = getState().getConceptIndex().lookupType(namespace, rootTypeName);
        if (rootType instanceof UnionType unionType) {
            unionType.setRoot(true);
            for (var variantType : unionType.getTypes()) {
                if (variantType instanceof EntityType entityType && entityType.getEntity() != null) {
                    entityType.getEntity().setRoot(true);
                    debug("Marked entity as root-capable: %s", entityType.getEntity().fullyQualifiedName());
                }
            }
            return;
        }
        // Try direct entity name
        var entity = getState().getConceptIndex().lookupEntity(namespace, rootTypeName);
        if (entity != null) {
            entity.setRoot(true);
            debug("Marked entity as root: %s", entity.fullyQualifiedName());
            return;
        }
        warn("Root type '%s' not found in namespace '%s'", rootTypeName, namespace);
    }

    private PropertyModel createPropertyModel(Property property, String namespace,
                                               boolean checkShading, EntityModel entityModel) {
        var propertyType = PropertyType.parse(property.getType());
        var rawType = RawType.parse(property.getType());
        var resolvedType = resolveType(rawType, namespace);

        var builder = PropertyModel.builder()
                .name(property.getName())
                .collection(property.getCollection())
                .discriminator(property.getDiscriminator())
                .unionRules(property.getUnionRules())
                .rawType(property.getType())
                .type(propertyType)
                .resolvedType(resolvedType);

        if (checkShading && entityModel != null) {
            builder.shaded(isPropertyShaded(property, entityModel));
        }

        return builder.build();
    }

    /**
     * Resolve a parsed {@link RawType} into a semantic {@link Type} by looking up
     * entity names in the concept index.
     */
    private Type resolveType(RawType rawType, String namespace) {
        if (rawType.isSimple()) {
            if (rawType.isPrimitiveType()) {
                return PrimitiveType.getByName(rawType.getSimpleType());
            }
            // Entity type — look up in concept index
            var entityName = rawType.getSimpleType();
            var entity = getState().getConceptIndex().lookupEntity(namespace, entityName);
            if (entity == null) {
                // Try looking up as a type alias (will be used in Step 2)
                var type = getState().getConceptIndex().lookupType(namespace, entityName);
                if (type != null) {
                    return type;
                }
                warn("Entity not found for type '%s' in namespace '%s'", entityName, namespace);
                // Fall back to creating an unresolved entity type
                return EntityType.builder()
                        .namespace(namespace)
                        .name(entityName)
                        .rawType(rawType)
                        .build();
            }
            return EntityType.fromEntity(entity);
        }

        if (rawType.isList()) {
            var valueRawType = rawType.getNested().get(0);
            var valueType = resolveType(valueRawType, namespace);
            return ListType.builder()
                    .namespace(namespace)
                    .name("[" + valueType.getName() + "]")
                    .rawType(rawType)
                    .valueType(valueType)
                    .build();
        }

        if (rawType.isMap()) {
            var valueRawType = rawType.getNested().get(0);
            var valueType = resolveType(valueRawType, namespace);
            return MapType.builder()
                    .namespace(namespace)
                    .name("{" + valueType.getName() + "}")
                    .rawType(rawType)
                    .valueType(valueType)
                    .build();
        }

        if (rawType.isUnion()) {
            var types = new ArrayList<Type>();
            for (var nestedRawType : rawType.getNested()) {
                var nestedType = resolveType(nestedRawType, namespace);
                // Wrap primitives in PrimitiveUnionVariantType when they appear in unions
                if (nestedType instanceof PrimitiveType primitive) {
                    types.add(PrimitiveUnionVariantType.builder()
                            .namespace(PrimitiveType.NAMESPACE)
                            .name(primitive.getName())
                            .rawType(nestedRawType)
                            .type(primitive)
                            .build());
                } else {
                    types.add(nestedType);
                }
            }
            return UnionType.builder()
                    .namespace(namespace)
                    .name(rawType.asRawType())
                    .rawType(rawType)
                    .types(types)
                    .build();
        }

        throw new IllegalStateException("Unknown raw type structure: " + rawType);
    }

    private boolean isPropertyShaded(Property property, EntityModel entityModel) {
        var type = PropertyType.parse(property.getType());
        if (type != null && type.isPrimitiveType()) {
            return false;
        }
        var traits = entityModel.getTraits();
        if (traits != null) {
            for (var trait : traits) {
                if (trait.getProperties().containsKey(property.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
