package io.apitomy.umg.pipe.java.method;

import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;

/**
 * Generates all 7 MappedNode methods on entity implementation classes:
 * {@code getItem}, {@code getItems}, {@code getItemNames}, {@code addItem},
 * {@code insertItem}, {@code removeItem}, and {@code clearItems}.
 * <p>
 * For interface targets, adds the {@code MappedNode<T>} superinterface instead.
 */
public class MappedNodeMethods {

    private final PropertyModel property;
    private final PropertyModelWithOrigin propertyWithOrigin;
    private final CodeGenContext ctx;

    public MappedNodeMethods(PropertyModel property, PropertyModelWithOrigin propertyWithOrigin,
                             CodeGenContext ctx) {
        this.property = property;
        this.propertyWithOrigin = propertyWithOrigin;
        this.ctx = ctx;
    }

    public void writeTo(JavaSource<?> target) {
        if (target instanceof JavaInterfaceSource) {
            writeInterfaceMethods((JavaInterfaceSource) target);
        } else {
            writeImplMethods(target);
        }
    }

    private void writeInterfaceMethods(JavaInterfaceSource javaEntity) {
        String mappedNodeFQN = ctx.getMappedNodeInterfaceFQN();
        JavaInterfaceSource mappedNodeInterface = ctx.getJavaIndex().lookupInterface(mappedNodeFQN);

        javaEntity.addImport(mappedNodeInterface);

        var jt = ctx.getJavaTypeFactory().createJavaType(
                property.getResolvedType(),
                propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);
        String mappedNodeInterfaceWithType = mappedNodeInterface.getName() + "<" + jt.toJavaTypeString() + ">";

        javaEntity.addInterface(mappedNodeInterfaceWithType);
    }

    private void writeImplMethods(JavaSource<?> javaEntity) {
        javaEntity.addImport(List.class);
        javaEntity.addImport(ArrayList.class);

        var jt = ctx.getJavaTypeFactory().createJavaType(
                property.getResolvedType(),
                propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntity);
        String mappedNodeType = jt.toJavaTypeString();

        boolean entityProperty = property.getResolvedType().isEntityType();

        writeGetItem(javaEntity, mappedNodeType);
        writeGetItems(javaEntity, mappedNodeType);
        writeGetItemNames(javaEntity);
        writeAddItem(javaEntity, mappedNodeType, entityProperty);
        writeInsertItem(javaEntity, mappedNodeType, entityProperty);
        writeRemoveItem(javaEntity, mappedNodeType, entityProperty);
        writeClearItems(javaEntity, entityProperty);
    }

    private void writeGetItem(JavaSource<?> javaEntity, String mappedNodeType) {
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("getItem").setPublic();
        method.addAnnotation(Override.class);
        method.addParameter("String", "name");
        method.setReturnType(mappedNodeType);
        BodyBuilder body = new BodyBuilder();
        body.append("return this._items.get(name);");
        method.setBody(body.toString());
    }

    private void writeGetItems(JavaSource<?> javaEntity, String mappedNodeType) {
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

    private void writeGetItemNames(JavaSource<?> javaEntity) {
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("getItemNames").setPublic();
        method.addAnnotation(Override.class);
        method.setReturnType("List<String>");
        BodyBuilder body = new BodyBuilder();
        body.append("List<String> rval = new ArrayList<>();");
        body.append("rval.addAll(this._items.keySet());");
        body.append("return rval;");
        method.setBody(body.toString());
    }

    private void writeAddItem(JavaSource<?> javaEntity, String mappedNodeType, boolean entityProperty) {
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("addItem").setPublic().setReturnTypeVoid();
        method.addAnnotation(Override.class);
        method.addParameter("String", "name");
        method.addParameter(mappedNodeType, "item");
        BodyBuilder body = new BodyBuilder();
        body.append("this._items.put(name, item);");
        if (entityProperty) {
            addParentTrackingImports(javaEntity);
            body.appendBlock("""
if (item != null) {
    ((NodeImpl) item)._setParent(this);
    ((NodeImpl) item)._setParentPropertyName(null);
    ((NodeImpl) item)._setParentPropertyType(ParentPropertyType.map);
    ((NodeImpl) item)._setMapPropertyName(name);
}
""");
        }
        method.setBody(body.toString());
    }

    private void writeInsertItem(JavaSource<?> javaEntity, String mappedNodeType, boolean entityProperty) {
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("insertItem").setPublic().setReturnTypeVoid();
        method.addAnnotation(Override.class);
        method.addParameter("String", "name");
        method.addParameter(mappedNodeType, "item");
        method.addParameter("int", "atIndex");

        JavaClassSource dataModelUtilSource = ctx.getJavaIndex().lookupClass(ctx.getDataModelUtilFQCN());
        javaEntity.addImport(dataModelUtilSource);

        BodyBuilder body = new BodyBuilder();
        body.append("this._items = DataModelUtil.insertMapEntry(this._items, name, item, atIndex);");
        if (entityProperty) {
            addParentTrackingImports(javaEntity);
            body.appendBlock("""
if (item != null) {
    ((NodeImpl) item)._setParent(this);
    ((NodeImpl) item)._setParentPropertyName(null);
    ((NodeImpl) item)._setParentPropertyType(ParentPropertyType.map);
    ((NodeImpl) item)._setMapPropertyName(name);
}
""");
        }
        method.setBody(body.toString());
    }

    private void writeRemoveItem(JavaSource<?> javaEntity, String mappedNodeType, boolean entityProperty) {
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("removeItem").setPublic();
        method.addAnnotation(Override.class);
        method.addParameter("String", "name");
        method.setReturnType(mappedNodeType);
        BodyBuilder body = new BodyBuilder();
        body.addContext("mappedNodeType", mappedNodeType);
        body.append("${mappedNodeType} removed = this._items.remove(name);");
        if (entityProperty) {
            body.append("if (removed != null) removed.detach();");
        }
        body.append("return removed;");
        method.setBody(body.toString());
    }

    private void writeClearItems(JavaSource<?> javaEntity, boolean entityProperty) {
        MethodSource<?> method = ((MethodHolderSource<?>) javaEntity).addMethod().setName("clearItems").setPublic();
        method.addAnnotation(Override.class);
        method.setReturnTypeVoid();
        BodyBuilder body = new BodyBuilder();
        if (entityProperty) {
            body.append("for (Object item : this._items.values()) {");
            body.append("    if (item != null) ((Node) item).detach();");
            body.append("}");
        }
        body.append("this._items.clear();");
        method.setBody(body.toString());
    }

    private void addParentTrackingImports(JavaSource<?> javaEntity) {
        JavaEnumSource parentPropertyTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getParentPropertyTypeEnumFQN());
        javaEntity.addImport(parentPropertyTypeSource);
        JavaClassSource nodeImplSource = ctx.getJavaIndex().lookupClass(ctx.getNodeEntityClassFQN());
        javaEntity.addImport(nodeImplSource);
    }

}
