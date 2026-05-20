package io.apitomy.umg.pipe.java;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.TraitModel;

/**
 * Adds methods to all entity interfaces. This works by finding all the properties for the entity and then
 * deciding what methods should exist on the entity interface based on the name and type of the property.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateInterfaceMethodsStage extends AbstractCreateMethodsStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").forEach(entity -> {
            createEntityInterfaceMethods(entity);
        });
        getState().getConceptIndex().findTraits("").forEach(trait -> {
            createTraitInterfaceMethods(trait);
        });
    }

    private void createEntityInterfaceMethods(EntityModel entity) {
        Set<String> createdProperties = new HashSet<>();

        JavaInterfaceSource javaEntity = lookupJavaEntity(entity);
        entity.getProperties().values().forEach(property -> {
            createPropertyMethods(javaEntity,
                    PropertyModelWithOrigin.builder().property(property).origin(entity).build());
            createdProperties.add(property.getName());
        });
        // If this is a leaf entity, we need to redeclare all Trait based properties on the
        // entity's core interface.  This will be redundant (those same methods will be
        // declared on the Trait interfaces), but will make it more convenient to access
        // properties that make up the entity.  In other words, you will always be able to
        // access a property of a leaf Entity even if the property comes from one of its
        // Traits, without casting as the Trait.
        if (entity.isLeaf()) {
            getState().getConceptIndex().getEntityPropertiesFromTraits(entity).forEach(property -> {
                if (!createdProperties.contains(property.getProperty().getName())) {
                    createPropertyMethods(javaEntity, property);
                    createdProperties.add(property.getProperty().getName());
                }
            });
        }
    }

    private void createTraitInterfaceMethods(TraitModel trait) {
        trait.getProperties().values().forEach(property -> {
            createPropertyMethods(lookupJavaTrait(trait),
                    PropertyModelWithOrigin.builder().property(property).origin(trait).build());
        });
    }

    /**
     * When an entity has a "*" property, that means the entity is a wrapper around a map of values of a
     * particular type. In this case, the entity interface needs to extend the "MappedNode" interface.
     *
     * @param javaEntity
     * @param propertyWithOrigin
     */
    @Override
    protected void createMappedNodeMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        String mappedNodeFQN = getMappedNodeInterfaceFQN();
        JavaInterfaceSource mappedNodeInterface = getState().getJavaIndex().lookupInterface(mappedNodeFQN);

        javaEntity.addImport(mappedNodeInterface);
        String mappedNodeInterfaceWithType;

        // TODO use the JavaType helper instead of the following if/then/else
        //        JavaType jt = new JavaType(property.getType(), propertyWithOrigin.getOrigin().getNamespace().fullName());
        //        jt.addImportsTo(javaEntity);
        //        mappedNodeInterfaceWithType = jt.toJavaTypeString();

        if (isPrimitiveList(property)) {
            Class<?> listType = primitiveTypeToClass(property.getType().getNested().iterator().next());
            javaEntity.addImport(List.class);
            javaEntity.addImport(listType);
            String mappedType = "List<" + listType.getSimpleName() + ">";
            mappedNodeInterfaceWithType = mappedNodeInterface.getName() + "<" + mappedType + ">";
        } else if (isPrimitiveMap(property)) {
            Class<?> mapType = primitiveTypeToClass(property.getType().getNested().iterator().next());
            javaEntity.addImport(Map.class);
            javaEntity.addImport(mapType);
            String mappedType = "Map<String, " + mapType.getSimpleName() + ">";
            mappedNodeInterfaceWithType = mappedNodeInterface.getName() + "<" + mappedType + ">";
        } else if (isPrimitive(property)) {
            Class<?> primType = primitiveTypeToClass(property.getType());
            javaEntity.addImport(primType);
            mappedNodeInterfaceWithType = mappedNodeInterface.getName() + "<" + primType.getSimpleName()
            + ">";
        } else if (isEntity(property)) {
            JavaInterfaceSource entityType = resolveJavaEntityType(
                    propertyWithOrigin.getOrigin().getNamespace().fullName(), property);
            if (entityType == null) {
                error("Java interface for entity type not found: " + property.getType());
                return;
            } else {
                javaEntity.addImport(entityType);
                mappedNodeInterfaceWithType = mappedNodeInterface.getName() + "<" + entityType.getName()
                + ">";
            }
        } else {
            error("Unhandled STAR property from entity: " + javaEntity.getCanonicalName());
            return;
        }

        ((JavaInterfaceSource) javaEntity).addInterface(mappedNodeInterfaceWithType);
    }

    /*
     * Do nothing for the createXyzMethodBody() methods - we're creating interfaces, so the methods
     * shouldn't have a body.
     */

    @Override
    protected void createGetterBody(PropertyModel property, MethodSource<?> method) {
    }

    @Override
    protected void createSetterBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
    }

    @Override
    protected void createFactoryMethodBody(JavaSource<?> javaEntity, String entityName, MethodSource<?> method) {
    }

    @Override
    protected void createAddMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
    }

    @Override
    protected void createClearMethodBody(PropertyModel property, MethodSource<?> method) {
    }

    @Override
    protected void createRemoveMethodBody(PropertyModel property, MethodSource<?> method) {
    }

    @Override
    protected void createInsertMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
    }
}
