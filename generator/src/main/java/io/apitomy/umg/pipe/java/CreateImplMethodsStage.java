package io.apitomy.umg.pipe.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates methods on all entity implementation classes.  This follows the same algorithm
 * as {@link CreateInterfaceMethodsStage} except it rolls up all of the methods from the
 * entire entity and trait hierarchies so that the Impl class can implement them.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateImplMethodsStage extends AbstractCreateMethodsStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createEntityImplMethods(entity);
        });
    }

    /**
     * Creates implementations of all methods needed for an implementation of the given
     * entity model.
     * @param entity
     */
    private void createEntityImplMethods(EntityModel entity) {
        JavaClassSource javaEntity = lookupJavaEntityImpl(entity);

        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entity);
        allProperties.forEach(property -> {
            createPropertyMethods(javaEntity, property);
        });
    }

    /**
     * When an entity has a "*" property, that means the entity is a wrapper around a map
     * of values of a particular type.  In this case, the entity interface needs to extend
     * the "MappedNode" interface.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    @Override
    protected void createMappedNodeMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        String mappedNodeType;
        javaEntity.addImport(List.class);
        javaEntity.addImport(ArrayList.class);

        if (isPrimitiveList(property)) {
            Class<?> listType = primitiveTypeToClass(property.getType().getNested().iterator().next());
            javaEntity.addImport(listType);
            mappedNodeType = "List<" + listType.getSimpleName() + ">";
        } else if (isPrimitiveMap(property)) {
            Class<?> mapType = primitiveTypeToClass(property.getType().getNested().iterator().next());
            javaEntity.addImport(Map.class);
            javaEntity.addImport(mapType);
            mappedNodeType = "Map<String, " + mapType.getSimpleName() + ">";
        } else if (isPrimitive(property)) {
            Class<?> primType = primitiveTypeToClass(property.getType());
            javaEntity.addImport(primType);
            mappedNodeType = primType.getSimpleName();
        } else if (isEntity(property)) {
            JavaInterfaceSource entityType = resolveJavaEntityType(propertyWithOrigin.getOrigin().getNamespace().fullName(), property);
            if (entityType == null) {
                error("Java interface for entity type not found: " + property.getType());
                return;
            } else {
                javaEntity.addImport(entityType);
                mappedNodeType = entityType.getName();
            }
        } else {
            error("Unhandled STAR property from entity: " + javaEntity.getCanonicalName());
            return;
        }

        // T getItem(String name)
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("getItem").setPublic();
            method.addAnnotation(Override.class);
            method.addParameter("String", "name");
            method.setReturnType(mappedNodeType);
            BodyBuilder body = new BodyBuilder();
            body.append("return this._items.get(name);");
            method.setBody(body.toString());
        }

        // List<T> getItems()
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("getItems").setPublic();
            method.addAnnotation(Override.class);
            method.setReturnType("List<" + mappedNodeType + ">");
            BodyBuilder body = new BodyBuilder();
            body.addContext("itemType", mappedNodeType);
            body.append("List<${itemType}> rval = new ArrayList<>();");
            body.append("rval.addAll(this._items.values());");
            body.append("return rval;");
            method.setBody(body.toString());
        }

        // List<String> getItemNames()
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("getItemNames").setPublic();
            method.addAnnotation(Override.class);
            method.setReturnType("List<String>");
            BodyBuilder body = new BodyBuilder();
            body.addContext("itemType", mappedNodeType);
            body.append("List<String> rval = new ArrayList<>();");
            body.append("rval.addAll(this._items.keySet());");
            body.append("return rval;");
            method.setBody(body.toString());
        }

        // void addItem(String name, T item)
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("addItem").setPublic().setReturnTypeVoid();
            method.addAnnotation(Override.class);
            method.addParameter("String", "name");
            method.addParameter(mappedNodeType, "item");
            BodyBuilder body = new BodyBuilder();
            body.append("this._items.put(name, item);");
            if (isEntity(property)) {
                JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                javaEntity.addImport(parentPropertyTypeSource);
                JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                javaEntity.addImport(nodeImplSource);

                body.append("if (item != null) {");
                body.append("    ((NodeImpl) item)._setParentPropertyName(null);");
                body.append("    ((NodeImpl) item)._setParentPropertyType(ParentPropertyType.map);");
                body.append("    ((NodeImpl) item)._setMapPropertyName(name);");
                body.append("}");
            }
            method.setBody(body.toString());
        }

        // void insertItem(String name, T item, int atIndex)
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("insertItem").setPublic().setReturnTypeVoid();
            method.addAnnotation(Override.class);
            method.addParameter("String", "name");
            method.addParameter(mappedNodeType, "item");
            method.addParameter("int", "atIndex");

            JavaClassSource dataModelUtilSource = getState().getJavaIndex().lookupClass(getDataModelUtilFQCN());
            javaEntity.addImport(dataModelUtilSource);

            BodyBuilder body = new BodyBuilder();
            body.append("this._items = DataModelUtil.insertMapEntry(this._items, name, item, atIndex);");
            if (isEntity(property)) {
                JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                javaEntity.addImport(parentPropertyTypeSource);
                JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                javaEntity.addImport(nodeImplSource);

                body.append("if (item != null) {");
                body.append("    ((NodeImpl) item)._setParentPropertyName(null);");
                body.append("    ((NodeImpl) item)._setParentPropertyType(ParentPropertyType.map);");
                body.append("    ((NodeImpl) item)._setMapPropertyName(name);");
                body.append("}");
            }
            method.setBody(body.toString());
        }

        // T removeItem(String name)
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("removeItem").setPublic();
            method.addAnnotation(Override.class);
            method.addParameter("String", "name");
            method.setReturnType(mappedNodeType);
            BodyBuilder body = new BodyBuilder();
            body.append("return this._items.remove(name);");
            method.setBody(body.toString());
        }

        // void clearItems()
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("clearItems").setPublic();
            method.addAnnotation(Override.class);
            method.setReturnTypeVoid();
            BodyBuilder body = new BodyBuilder();
            body.append("this._items.clear();");
            method.setBody(body.toString());
        }
    }

    @Override
    protected void createGetterBody(PropertyModel property, MethodSource<?> method) {
        String fieldName = getFieldName(property);
        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.append("return ${fieldName};");
        method.setBody(body.toString());
    }

    @Override
    protected void createSetterBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
        String fieldName = getFieldName(property);
        String propertyName = property.getName();

        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.addContext("propertyName", propertyName);
        body.append("this.${fieldName} = value;");
        if (isEntity(property)) {
            JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
            javaEntity.addImport(parentPropertyTypeSource);
            JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
            javaEntity.addImport(nodeImplSource);

            body.append("if (value != null) {");
            body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
            body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);");
            body.append("}");
        } else if (isUnion(property)) {
            JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
            javaEntity.addImport(parentPropertyTypeSource);
            JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
            javaEntity.addImport(nodeImplSource);
            JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(getUnionValueInterfaceFQN());
            javaEntity.addImport(unionValueSource);

            javaEntity.addImport(Collection.class);
            javaEntity.addImport(List.class);
            javaEntity.addImport(Map.class);

            body.append("if (value != null) {");
            body.append("    if (value.isEntity()) {");
            body.append("        ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
            body.append("        ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);");
            body.append("    } else if (value.isEntityList()) {");
            body.append("        List<?> entityList = (List<?>) ((UnionValue<?>) value).getValue();");
            body.append("        for (Object entity : entityList) {");
            body.append("            if (entity != null) {");
            body.append("                ((NodeImpl) entity)._setParentPropertyName(\"${propertyName}\");");
            body.append("                ((NodeImpl) entity)._setParentPropertyType(ParentPropertyType.array);");
            body.append("            }");
            body.append("        }");
            body.append("    } else if (value.isEntityMap()) {");
            body.append("        Map<String, ?> entityMap = (Map<String, ?>) ((UnionValue<?>) value).getValue();");
            body.append("        Collection<String> keys = entityMap.keySet();");
            body.append("        for (String key : keys) {");
            body.append("            NodeImpl entity = (NodeImpl) entityMap.get(key);");
            body.append("            if (entity != null) {");
            body.append("                entity._setParentPropertyName(\"${propertyName}\");");
            body.append("                entity._setParentPropertyType(ParentPropertyType.map);");
            body.append("                entity._setMapPropertyName(key);");
            body.append("            }");
            body.append("        }");
            body.append("    }");
            body.append("}");
        }
        method.setBody(body.toString());
    }

    @Override
    protected void createFactoryMethodBody(JavaSource<?> javaEntity, String entityName, MethodSource<?> method) {
        String entityFQN = javaEntity.getPackage() + "." + entityName;
        EntityModel entityModel = getState().getConceptIndex().lookupEntity(entityFQN);
        String implFQN = getJavaEntityClassFQN(entityModel);

        JavaClassSource entityImpl = lookupJavaEntityImpl(implFQN);
        if (entityImpl == null) {
            error("Could not resolve entity type (impl): " + implFQN);
            return;
        }
        javaEntity.addImport(entityImpl);

        BodyBuilder body = new BodyBuilder();
        body.addContext("implClass", entityImpl.getName());
        body.append("${implClass} node = new ${implClass}();");
        body.append("node.setParent(this);");
        body.append("return node;");
        method.setBody(body.toString());
    }

    @Override
    protected void createAddMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
        PropertyType type = property.getType().getNested().iterator().next();
        String fieldName = getFieldName(property);
        String propertyName = property.getName();

        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.addContext("propertyName", propertyName);

        if (type.isEntityType() || type.isPrimitiveType() || type.isUnion()) {
            if (property.getType().isMap()) {
                javaEntity.addImport(LinkedHashMap.class);

                body.append("if (this.${fieldName} == null) {");
                body.append("    this.${fieldName} = new LinkedHashMap<>();");
                body.append("}");
                body.append("this.${fieldName}.put(name, value);");
                if (type.isEntityType()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);

                    body.append("if (value != null) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);");
                    body.append("    ((NodeImpl) value)._setMapPropertyName(name);");
                    body.append("}");
                } else if (type.isUnion()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);
                    JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(getUnionValueInterfaceFQN());
                    javaEntity.addImport(unionValueSource);

                    body.append("if (value != null && value.isEntity()) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);");
                    body.append("    ((NodeImpl) value)._setMapPropertyName(name);");
                    body.append("}");
                }
            } else {
                javaEntity.addImport(ArrayList.class);

                body.append("if (this.${fieldName} == null) {");
                body.append("    this.${fieldName} = new ArrayList<>();");
                body.append("}");
                body.append("this.${fieldName}.add(value);");
                if (type.isEntityType()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);

                    body.append("if (value != null) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);");
                    body.append("}");
                } else if (type.isUnion()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);
                    JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(getUnionValueInterfaceFQN());
                    javaEntity.addImport(unionValueSource);

                    body.append("if (value != null && value.isEntity()) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);");
                    body.append("}");
                }
            }
        }

        method.setBody(body.toString());
    }

    @Override
    protected void createClearMethodBody(PropertyModel property, MethodSource<?> method) {
        String fieldName = getFieldName(property);
        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.append("if (this.${fieldName} != null) {");
        body.append("    this.${fieldName}.clear();");
        body.append("}");
        method.setBody(body.toString());
    }

    @Override
    protected void createRemoveMethodBody(PropertyModel property, MethodSource<?> method) {
        String fieldName = getFieldName(property);
        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);

        body.append("if (this.${fieldName} != null) {");
        if (property.getType().isList()) {
            body.append("    this.${fieldName}.remove(value);");
        } else {
            body.append("    this.${fieldName}.remove(name);");
        }
        body.append("}");

        method.setBody(body.toString());
    }

    @Override
    protected void createInsertMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
        PropertyType type = property.getType().getNested().iterator().next();
        String fieldName = getFieldName(property);
        String propertyName = property.getName();

        BodyBuilder body = new BodyBuilder();
        body.addContext("fieldName", fieldName);
        body.addContext("propertyName", propertyName);

        if (type.isEntityType() || type.isPrimitiveType() || type.isUnion()) {
            if (property.getType().isMap()) {
                JavaClassSource dataModelUtilSource = getState().getJavaIndex().lookupClass(getDataModelUtilFQCN());
                javaEntity.addImport(dataModelUtilSource);
                javaEntity.addImport(LinkedHashMap.class);

                body.append("if (this.${fieldName} == null) {");
                body.append("    this.${fieldName} = new LinkedHashMap<>();");
                body.append("    this.${fieldName}.put(name, value);");
                body.append("} else {");
                body.append("    this.${fieldName} = DataModelUtil.insertMapEntry(this.${fieldName}, name, value, atIndex);");
                body.append("}");

                if (type.isEntityType()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);

                    body.append("if (value != null) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);");
                    body.append("    ((NodeImpl) value)._setMapPropertyName(name);");
                    body.append("}");
                } else if (type.isUnion()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);
                    JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(getUnionValueInterfaceFQN());
                    javaEntity.addImport(unionValueSource);

                    body.append("if (value != null && value.isEntity()) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);");
                    body.append("    ((NodeImpl) value)._setMapPropertyName(name);");
                    body.append("}");
                }
            } else {
                JavaClassSource dataModelUtilSource = getState().getJavaIndex().lookupClass(getDataModelUtilFQCN());
                javaEntity.addImport(dataModelUtilSource);
                javaEntity.addImport(ArrayList.class);

                body.append("if (this.${fieldName} == null) {");
                body.append("    this.${fieldName} = new ArrayList<>();");
                body.append("    this.${fieldName}.add(value);");
                body.append("} else {");
                body.append("    this.${fieldName} = DataModelUtil.insertListEntry(this.${fieldName}, value, atIndex);");
                body.append("}");
                if (type.isEntityType()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);

                    body.append("if (value != null) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);");
                    body.append("}");
                } else if (type.isUnion()) {
                    JavaEnumSource parentPropertyTypeSource = getState().getJavaIndex().lookupEnum(getParentPropertyTypeEnumFQN());
                    javaEntity.addImport(parentPropertyTypeSource);
                    JavaClassSource nodeImplSource = getState().getJavaIndex().lookupClass(getNodeEntityClassFQN());
                    javaEntity.addImport(nodeImplSource);
                    JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(getUnionValueInterfaceFQN());
                    javaEntity.addImport(unionValueSource);

                    body.append("if (value != null && value.isEntity()) {");
                    body.append("    ((NodeImpl) value)._setParentPropertyName(\"${propertyName}\");");
                    body.append("    ((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);");
                    body.append("}");
                }
            }
        }

        method.setBody(body.toString());
    }

    @Override
    protected void addAnnotations(MethodSource<?> method) {
        method.addAnnotation(Override.class);
    }

}
