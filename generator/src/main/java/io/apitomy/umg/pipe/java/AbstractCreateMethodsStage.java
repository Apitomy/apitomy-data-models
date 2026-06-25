package io.apitomy.umg.pipe.java;

import java.util.Collections;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.util.function.Predicate;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.models.concept.type.CollectionType;
import io.apitomy.umg.models.concept.type.Type;

/**
 * Base class for the stages that create methods for entity interfaces and impl classes both.  The
 * logic for these two stages is shared.
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractCreateMethodsStage extends AbstractJavaStage {

    protected void createPropertyMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();
        if (property.getName().equals("*")) {
            if (isEntity(property) || isPrimitive(property) || isPrimitiveList(property)) {
                createMappedNodeMethods(javaEntity, propertyWithOrigin);
                if (isEntity(property)) {
                    createFactoryMethod(javaEntity, propertyWithOrigin);
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
            PropertyType collectionPropertyType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            Type collectionResolvedType = property.getResolvedType() != null
                    ? io.apitomy.umg.models.concept.type.MapType.builder()
                        .namespace(property.getResolvedType().getNamespace())
                        .name("{" + property.getResolvedType().getName() + "}")
                        .valueType(property.getResolvedType())
                        .build()
                    : null;
            PropertyModel collectionProperty = PropertyModel.builder()
                    .name(property.getCollection())
                    .type(collectionPropertyType)
                    .resolvedType(collectionResolvedType)
                    .build();
            PropertyModelWithOrigin collectionPropertyWithOrigin = PropertyModelWithOrigin.builder().property(collectionProperty).origin(propertyWithOrigin.getOrigin()).build();

            if (isEntity(property)) {
                createFactoryMethod(javaEntity, collectionPropertyWithOrigin);
            }
            createGetter(javaEntity, collectionPropertyWithOrigin);
            createAddMethod(javaEntity, collectionPropertyWithOrigin);
            createClearMethod(javaEntity, collectionPropertyWithOrigin);
            createRemoveMethod(javaEntity, collectionPropertyWithOrigin);
            createInsertMethod(javaEntity, collectionPropertyWithOrigin);
        } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
            createGetter(javaEntity, propertyWithOrigin);
            createSetter(javaEntity, propertyWithOrigin);
        } else if (resolvedTypeIs(property, t -> t.isEntityType())) {
            createGetter(javaEntity, propertyWithOrigin);
            createSetter(javaEntity, propertyWithOrigin);
            createFactoryMethod(javaEntity, propertyWithOrigin);
        } else if (resolvedTypeIs(property, t -> t.isCollectionType()
                && ((io.apitomy.umg.models.concept.type.CollectionType) t).getValueType().isEntityType())) {
            createFactoryMethod(javaEntity, propertyWithOrigin);
            createGetter(javaEntity, propertyWithOrigin);
            createAddMethod(javaEntity, propertyWithOrigin);
            createClearMethod(javaEntity, propertyWithOrigin);
            createRemoveMethod(javaEntity, propertyWithOrigin);
            createInsertMethod(javaEntity, propertyWithOrigin);
        } else if (resolvedTypeIs(property, t -> t.isUnionType())) {
            createGetter(javaEntity, propertyWithOrigin);
            createSetter(javaEntity, propertyWithOrigin);
            createUnionFactoryMethods(javaEntity, propertyWithOrigin);
        } else if (resolvedTypeIs(property, t -> t.isCollectionType()
                && ((io.apitomy.umg.models.concept.type.CollectionType) t).getValueType().isUnionType())) {
            createUnionFactoryMethods(javaEntity, propertyWithOrigin);
            createGetter(javaEntity, propertyWithOrigin);
            createAddMethod(javaEntity, propertyWithOrigin);
            createClearMethod(javaEntity, propertyWithOrigin);
            createRemoveMethod(javaEntity, propertyWithOrigin);
            createInsertMethod(javaEntity, propertyWithOrigin);
        } else if (resolvedTypeIs(property, t -> t.isCollectionType())) {
            createGetter(javaEntity, propertyWithOrigin);
            createAddMethod(javaEntity, propertyWithOrigin);
            createClearMethod(javaEntity, propertyWithOrigin);
            createRemoveMethod(javaEntity, propertyWithOrigin);
            createInsertMethod(javaEntity, propertyWithOrigin);
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
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected abstract void createMappedNodeMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin);

    /**
     * Creates a standard java getter method for the given property.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createGetter(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName(getterMethodName(property)).setPublic();
        addAnnotations(method);

        var jt = getJavaTypeFactory().createJavaType(property.getResolvedType(), propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);
        method.setReturnType(jt.toJavaTypeString());

        createGetterBody(property, method);
    }
    abstract protected void createGetterBody(PropertyModel property, MethodSource<?> method);

    /**
     * Creates a standard java setter method for the given property.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createSetter(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName(setterMethodName(property)).setReturnTypeVoid().setPublic();
        addAnnotations(method);

        var jt = getJavaTypeFactory().createJavaType(property.getResolvedType(), propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);
        method.addParameter(jt.toJavaTypeString(), "value");

        createSetterBody(javaEntity, property, method);
    }
    abstract protected void createSetterBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method);

    /**
     * Creates a factory method for the entity type associated with the given
     * property.  This method will only be called for entity properties, either
     * simple entity properties or collection entity properties (list/map).
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createFactoryMethod(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();
        createFactoryMethod(javaEntity, property.getType());
    }
    protected void createFactoryMethod(JavaSource<?> javaEntity, PropertyType propertyType) {
        String _package = javaEntity.getPackage();
        PropertyType type = propertyType;
        if (type.isMap() || type.isList()) {
            type = type.getNested().iterator().next();
        }
        String entityName = type.getSimpleType();
        String methodName = createMethodName(entityName);
        // The name of the "create" method is based on the type, so it's possible to have
        // duplicates.  Let's not do that.
        if (!hasNamedMethod(((MethodHolderSource<?>) javaEntity), methodName)) {
            JavaInterfaceSource entityType = resolveJavaEntityType(_package, type);
            if (entityType == null) {
                error("Could not resolve entity type: " + _package + "::" + type);
                return;
            }

            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setPublic().setName(methodName).setReturnType(entityType);
            addAnnotations(method);

            createFactoryMethodBody(javaEntity, entityName, method);
        }
    }
    abstract protected void createFactoryMethodBody(JavaSource<?> javaEntity, String entityName, MethodSource<?> method);

    /**
     * Creates an "add" method for the given property.  The type of the property must be a
     * collection of entities.  The add method will accept a single entity and add it to the collection.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createAddMethod(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        String methodName = addMethodName(singularize(property.getName()));
        var resolvedValueType = extractValueType(property.getResolvedType());
        if (resolvedValueType == null) {
            warn("Type not supported for 'add' method: " + methodName + " with type: " + property.getResolvedType());
            return;
        }

        var jt = getJavaTypeFactory().createJavaType(resolvedValueType, propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);

        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setPublic().setName(methodName).setReturnTypeVoid();
        addAnnotations(method);
        if (property.getResolvedType().isMapType()) {
            method.addParameter("String", "name");
        }
        method.addParameter(jt.toJavaTypeString(), "value");

        createAddMethodBody(javaEntity, property, method);
    }
    abstract protected void createAddMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method);

    /**
     * Creates a "clear" method for the given property.  The type of the property must be a
     * collection of entities.  The clear method will remove all items from the collection.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createClearMethod(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        String methodName = clearMethodName(property.getName());

        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setPublic().setName(methodName).setReturnTypeVoid();
        addAnnotations(method);

        createClearMethodBody(property, method);
    }
    abstract protected void createClearMethodBody(PropertyModel property, MethodSource<?> method);

    /**
     * Creates a "remove" method for the given property.  The type of the property must be a
     * collection of entities.  The remove method will remove one item from the collection.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createRemoveMethod(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        String methodName = removeMethodName(singularize(property.getName()));
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setPublic().setName(methodName).setReturnTypeVoid();
        addAnnotations(method);

        if (property.getResolvedType().isListType()) {
            var resolvedValueType = extractValueType(property.getResolvedType());
            var jt = getJavaTypeFactory().createJavaType(resolvedValueType, propertyWithOrigin.getOrigin().getNamespace());
            jt.addImportsTo(javaEntity);
            method.addParameter(jt.toJavaTypeString(), "value");
        } else {
            method.addParameter("String", "name");
        }

        createRemoveMethodBody(property, method);
    }
    abstract protected void createRemoveMethodBody(PropertyModel property, MethodSource<?> method);

    /**
     * Creates an "insert" method for the given property.  The type of the property must be a
     * collection of entities.  The insert method will add one item to the collection at
     * a specific index (if possible).
     * @param javaEntity
     * @param propertyWithOrigin
     */
    protected void createInsertMethod(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        String methodName = insertMethodName(singularize(property.getName()));
        var resolvedValueType = extractValueType(property.getResolvedType());
        if (resolvedValueType == null) {
            warn("Type not supported for 'insert' method: " + methodName + " with type: " + property.getResolvedType());
            return;
        }

        var jt = getJavaTypeFactory().createJavaType(resolvedValueType, propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);

        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setPublic().setName(methodName).setReturnTypeVoid();
        addAnnotations(method);
        if (property.getResolvedType().isMapType()) {
            method.addParameter("String", "name");
        }
        method.addParameter(jt.toJavaTypeString(), "value");
        method.addParameter("int", "atIndex");

        createInsertMethodBody(javaEntity, property, method);
    }
    abstract protected void createInsertMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method);

    /**
     * Create factory methods for any entity types in the union.  If the union is, for example, "boolean|string"
     * then this will do nothing.  But if the union is "Widget|string" then a factory method for Widgets will
     * be created.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    private void createUnionFactoryMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();
        PropertyType unionType;

        // Extract the union type - it might be directly a union, or wrapped in a list/map
        if (property.getType().isUnion()) {
            unionType = property.getType();
        } else if ((property.getType().isList() || property.getType().isMap()) &&
                   property.getType().getNested().iterator().next().isUnion()) {
            unionType = property.getType().getNested().iterator().next();
        } else {
            return;
        }

        UnionPropertyType ut = new UnionPropertyType(unionType);
        ut.getNestedTypes().forEach(nestedType -> {
            if (nestedType.isEntityType()) {
                createFactoryMethod(javaEntity, nestedType);
            } else if ((nestedType.isList() || nestedType.isMap()) && nestedType.getNested().iterator().next().isEntityType()) {
                createFactoryMethod(javaEntity, nestedType.getNested().iterator().next());
            }
        });
    }

    /**
     * Gives subclasses an opportunity to add annotations to the created method.
     * @param method
     */
    protected void addAnnotations(MethodSource<?> method) {
    }

    private Type extractValueType(Type type) {
        if (type instanceof CollectionType collectionType) {
            return collectionType.getValueType();
        }
        return null;
    }

}
