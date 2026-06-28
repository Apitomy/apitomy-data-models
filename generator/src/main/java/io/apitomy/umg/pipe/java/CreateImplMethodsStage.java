package io.apitomy.umg.pipe.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClearMethod;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.InsertMethod;
import io.apitomy.umg.pipe.java.method.RemoveMethod;
import io.apitomy.umg.pipe.java.method.SetterMethod;

/**
 * Creates methods on all entity implementation classes.  This follows the same algorithm
 * as {@link CreateInterfaceMethodsStage} except it rolls up all of the methods from the
 * entire entity and trait hierarchies so that the Impl class can implement them.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateImplMethodsStage extends AbstractCreateMethodsStage {

    private CodeGenContext ctx;

    @Override
    protected void doProcess() {
        ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());
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

        var jt = getJavaTypeFactory().createJavaType(property.getResolvedType(), propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);
        mappedNodeType = jt.toJavaTypeString();

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
                body.append("    ((NodeImpl) item)._setParent(this);");
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
                body.append("    ((NodeImpl) item)._setParent(this);");
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
            body.addContext("mappedNodeType", mappedNodeType);
            body.append("${mappedNodeType} removed = this._items.remove(name);");
            if (isEntity(property)) {
                body.append("if (removed != null) removed.detach();");
            }
            body.append("return removed;");
            method.setBody(body.toString());
        }

        // void clearItems()
        {
            MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("clearItems").setPublic();
            method.addAnnotation(Override.class);
            method.setReturnTypeVoid();
            BodyBuilder body = new BodyBuilder();
            if (isEntity(property)) {
                body.append("this._items.values().forEach(item -> {");
                body.append("    if (item != null) item.detach();");
                body.append("});");
            }
            body.append("this._items.clear();");
            method.setBody(body.toString());
        }
    }

    @Override
    protected void createGetterBody(PropertyModel property, MethodSource<?> method) {
        new GetterMethod(property, ctx).writeTo(method);
    }

    @Override
    protected void createSetterBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
        SetterMethod setter = new SetterMethod(javaEntity, property, ctx);
        setter.addImportsTo(javaEntity);
        setter.writeTo(method);
    }

    @Override
    protected void createFactoryMethodBody(JavaSource<?> javaEntity, String entityName, MethodSource<?> method) {
        new FactoryMethod(javaEntity, entityName, ctx).writeTo(method);
    }

    @Override
    protected void createAddMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
        AddMethod add = new AddMethod(javaEntity, property, ctx);
        add.addImportsTo(javaEntity);
        add.writeTo(method);
    }

    @Override
    protected void createClearMethodBody(PropertyModel property, MethodSource<?> method) {
        new ClearMethod(property, ctx).writeTo(method);
    }

    @Override
    protected void createRemoveMethodBody(PropertyModel property, MethodSource<?> method) {
        new RemoveMethod(property, ctx).writeTo(method);
    }

    @Override
    protected void createInsertMethodBody(JavaSource<?> javaEntity, PropertyModel property, MethodSource<?> method) {
        InsertMethod insert = new InsertMethod(javaEntity, property, ctx);
        insert.addImportsTo(javaEntity);
        insert.writeTo(method);
    }

    @Override
    protected void addAnnotations(MethodSource<?> method) {
        method.addAnnotation(Override.class);
    }

}
