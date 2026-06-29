package io.apitomy.umg.pipe.java;


import org.jboss.forge.roaster.model.source.JavaSource;

import java.util.function.Predicate;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.ClearMethod;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.InsertMethod;
import io.apitomy.umg.pipe.java.method.RemoveMethod;
import io.apitomy.umg.pipe.java.method.SetterMethod;

/**
 * Base class for the stages that create methods for entity interfaces and impl classes both.  The
 * logic for these two stages is shared.
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractCreateMethodsStage extends AbstractJavaStage {

    private CodeGenContext ctx;

    protected CodeGenContext getCtx() {
        return ctx;
    }

    protected void initContext() {
        ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());
    }

    protected void createPropertyMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();
        if (property.getName().equals("*")) {
            if (isEntity(property) || isPrimitive(property) || isPrimitiveList(property)) {
                createMappedNodeMethods(javaEntity, propertyWithOrigin);
                if (isEntity(property)) {
                    createFactoryMethodForType(javaEntity, property.getResolvedType());
                }
            } else {
                error("STAR property type not handled: " + javaEntity.getCanonicalName() + "::" + property);
                return;
            }
        } else if (property.getName().startsWith("/") && (isEntity(property) || isPrimitive(property))) {
            if (property.getCollection() == null) {
                error("Regex property defined without a collection name: " + javaEntity.getCanonicalName() + "::" + property);
                return;
            }
            Type collectionResolvedType = MapType.builder()
                    .namespace(property.getResolvedType().getNamespace())
                    .name("{" + property.getResolvedType().getName() + "}")
                    .valueType(property.getResolvedType())
                    .build();
            PropertyModel collectionProperty = PropertyModel.builder()
                    .name(property.getCollection())
                    .resolvedType(collectionResolvedType)
                    .build();
            PropertyModelWithOrigin collectionPropertyWithOrigin = PropertyModelWithOrigin.builder().property(collectionProperty).origin(propertyWithOrigin.getOrigin()).build();

            if (isEntity(property)) {
                createFactoryMethodForType(javaEntity, collectionResolvedType);
            }
            new GetterMethod(collectionProperty, collectionPropertyWithOrigin, ctx).writeTo(javaEntity);
            new AddMethod(collectionProperty, collectionPropertyWithOrigin, ctx).writeTo(javaEntity);
            new ClearMethod(collectionProperty, ctx).writeTo(javaEntity);
            new RemoveMethod(collectionProperty, collectionPropertyWithOrigin, ctx).writeTo(javaEntity);
            new InsertMethod(collectionProperty, collectionPropertyWithOrigin, ctx).writeTo(javaEntity);
        } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
            new GetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new SetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
        } else if (resolvedTypeIs(property, Type::isEntityType)) {
            new GetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new SetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            createFactoryMethodForType(javaEntity, property.getResolvedType());
        } else if (resolvedTypeIs(property, t -> t.isEntityListType() || t.isEntityMapType())) {
            createFactoryMethodForType(javaEntity, property.getResolvedType());
            new GetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new AddMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new ClearMethod(property, ctx).writeTo(javaEntity);
            new RemoveMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new InsertMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
        } else if (resolvedTypeIs(property, Type::isUnionType)) {
            new GetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new SetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            createUnionFactoryMethods(javaEntity, propertyWithOrigin);
        } else if (resolvedTypeIs(property, t -> t.isUnionListType() || t.isUnionMapType())) {
            createUnionFactoryMethods(javaEntity, propertyWithOrigin);
            new GetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new AddMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new ClearMethod(property, ctx).writeTo(javaEntity);
            new RemoveMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new InsertMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
        } else if (resolvedTypeIs(property, Type::isCollectionType)) {
            new GetterMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new AddMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new ClearMethod(property, ctx).writeTo(javaEntity);
            new RemoveMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
            new InsertMethod(property, propertyWithOrigin, ctx).writeTo(javaEntity);
        } else {
            warn("Failed to create methods (not yet implemented) for property '" + property.getName() + "' of entity: " + javaEntity.getQualifiedName());
        }
    }

    private boolean resolvedTypeIs(PropertyModel property, Predicate<Type> predicate) {
        var resolved = property.getResolvedType();
        return resolved != null && predicate.test(resolved);
    }

    /**
     * When an entity has a "*" property, that means the entity is a wrapper around a map
     * of values of a particular type.  In this case, the entity needs to extend/implement
     * the "MappedNode" interface.
     */
    protected abstract void createMappedNodeMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin);

    /**
     * Creates a factory method for the entity type associated with the given type.
     * Unwraps collection types to get the effective entity type.
     */
    private void createFactoryMethodForType(JavaSource<?> javaEntity, Type type) {
        Type effectiveType = type;
        if (effectiveType.isMapType() || effectiveType.isListType()) {
            effectiveType = ((CollectionType) effectiveType).getValueType();
        }
        String entityName = effectiveType.getName();
        new FactoryMethod(javaEntity, entityName, ctx).writeTo(javaEntity);
    }

    /**
     * Create factory methods for any entity types in the union.  If the union is, for example, "boolean|string"
     * then this will do nothing.  But if the union is "Widget|string" then a factory method for Widgets will
     * be created.
     *
     * Named union type aliases (e.g. "SchemaOrBoolean") are treated as opaque types and do NOT
     * get individual factory methods for their entity variants — the old PropertyType-based code
     * never saw them as unions (only as simple type references), so we preserve that behavior.
     *
     */
    private void createUnionFactoryMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();
        Type resolvedType = property.getResolvedType();
        UnionType unionType;

        // Extract the union type - it might be directly a union, or wrapped in a list/map
        if (resolvedType.isUnionType()) {
            unionType = (UnionType) resolvedType;
        } else if (resolvedType.isUnionListType() || resolvedType.isUnionMapType()) {
            unionType = (UnionType) ((CollectionType) resolvedType).getValueType();
        } else {
            return;
        }

        // Skip factory method creation for named union type aliases
        if (unionType.getAliasName() != null) {
            return;
        }

        for (Type variantType : unionType.getTypes()) {
            if (variantType.isEntityType()) {
                createFactoryMethodForType(javaEntity, variantType);
            } else if (variantType.isEntityListType() || variantType.isEntityMapType()) {
                createFactoryMethodForType(javaEntity, ((CollectionType) variantType).getValueType());
            }
        }
    }

}
