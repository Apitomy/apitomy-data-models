package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;

import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.AbstractStage;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

public abstract class AbstractJavaStage extends AbstractStage {

    private JavaTypeFactory javaTypeFactory;

    public JavaTypeFactory getJavaTypeFactory() {
        if (javaTypeFactory == null) {
            javaTypeFactory = new JavaTypeFactory(
                    getState().getConceptIndex(),
                    getState().getSpecIndex(),
                    getState().getJavaIndex(),
                    getUnionTypesPackageName());
        }
        return javaTypeFactory;
    }

    protected String getReaderClassName(SpecificationVersion specVersion) {
        return specVersion.getPrefix() + "ModelReader";
    }

    protected String getReaderPackageName(SpecificationVersion specVersion) {
        return specVersion.getNamespace() + ".io";
    }

    protected String getWriterClassName(SpecificationVersion specVersion) {
        return specVersion.getPrefix() + "ModelWriter";
    }

    protected String getWriterPackageName(SpecificationVersion specVersion) {
        return specVersion.getNamespace() + ".io";
    }

    protected String getClonerClassName(SpecificationVersion specVersion) {
        return specVersion.getPrefix() + "ModelCloner";
    }

    protected String getClonerPackageName(SpecificationVersion specVersion) {
        return specVersion.getNamespace() + ".io";
    }

    protected String getTraverserClassName(SpecificationVersion specVersion) {
        return specVersion.getPrefix() + "Traverser";
    }

    protected String getTraverserPackageName(SpecificationVersion specVersion) {
        return specVersion.getNamespace() + ".visitors";
    }

    /**
     * Determines the package to use for the interface generated for the given visitor.
     */
    protected String getVisitorInterfacePackageName(VisitorModel visitor) {
        String packageName = visitor.getNamespace().fullName();
        String visitorPackageName = packageName + ".visitors";
        return visitorPackageName;
    }

    /**
     * Determines the prefix to use for the interface name for the given visitor.
     */
    protected String getVisitorInterfacePrefix(VisitorModel visitor) {
        return (visitor.getParent() == null) ? "" : getState().getSpecIndex().prefixForNS(visitor.getNamespace().fullName());
    }

    /**
     * Determines the interface name for the given visitor.
     */
    protected String getVisitorInterfaceName(VisitorModel visitor) {
        String visitorPrefix = getVisitorInterfacePrefix(visitor);
        String visitorInterfaceName = visitorPrefix + "Visitor";
        return visitorInterfaceName;
    }

    /**
     * Determines the fully qualified name of the Java interface for a given visitor.
     */
    protected String getVisitorInterfaceFullName(VisitorModel visitor) {
        String packageName = visitor.getNamespace().fullName();
        String visitorPackageName = packageName + ".visitors";
        String visitorPrefix = getVisitorInterfacePrefix(visitor);
        String visitorInterfaceName = visitorPrefix + "Visitor";
        return visitorPackageName + "." + visitorInterfaceName;
    }

    protected String getFieldName(PropertyModel property) {
        if (property.getName().equals("*")) {
            return "_items";
        }
        if (property.getName().startsWith("/")) {
            return sanitizeFieldName(property.getCollection());
        }
        return sanitizeFieldName(property.getName());
    }

    protected String sanitizeFieldName(String name) {
        if (name == null) {
            return null;
        }
        return CodeGenContext.JAVA_KEYWORD_MAP.getOrDefault(name, name);
    }

    protected String getPrefix(NamespaceModel namespace) {
        return getPrefix(namespace.fullName());
    }

    protected String getPrefix(String namespace) {
        String prefix = getState().getSpecIndex().getNsToPrefix().get(namespace);
        return prefix == null ? "" : prefix;
    }

    public String getJavaEntityInterfaceFQN(EntityModel entity) {
        return getJavaEntityInterfacePackage(entity) + "." + getJavaEntityInterfaceName(entity);
    }

    protected String getJavaTraitInterfaceFQN(TraitModel trait) {
        return getJavaTraitInterfacePackage(trait) + "." + getJavaTraitInterfaceName(trait);
    }

    public String getJavaEntityClassFQN(EntityModel entity) {
        return getJavaEntityClassPackage(entity) + "." + getJavaEntityClassName(entity);
    }

    protected String getJavaEntityInterfaceName(EntityModel entity) {
        String prefix = getState().getSpecIndex().getNsToPrefix().get(entity.getNamespace().fullName());
        return (prefix == null ? "" : prefix) + entity.getName();
    }

    protected String getJavaTraitInterfaceName(TraitModel trait) {
        String prefix = getState().getSpecIndex().getNsToPrefix().get(trait.getNamespace().fullName());
        return (prefix == null ? "" : prefix) + trait.getName();
    }

    protected String getJavaEntityClassName(EntityModel entity) {
        String prefix = getState().getSpecIndex().getNsToPrefix().get(entity.getNamespace().fullName());
        return (prefix == null ? "" : prefix) + entity.getName() + "Impl";
    }

    protected String getJavaEntityInterfacePackage(EntityModel entity) {
        return getPackage(entity.getNamespace());
    }

    protected String getJavaTraitInterfacePackage(TraitModel trait) {
        return getPackage(trait.getNamespace());
    }

    protected String getJavaEntityClassPackage(EntityModel entity) {
        return getPackage(entity.getNamespace());
    }

    protected String getPackage(NamespaceModel namespace) {
        return namespace.fullName();
    }

    protected String getNodeEntityInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".Node";
    }

    protected String getRootNodeInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".RootCapable";
    }


    public String getNodeEntityClassFQN() {
        return getState().getConfig().getRootNamespace() + ".NodeImpl";
    }

    protected String getRootNodeEntityClassFQN() {
        return getState().getConfig().getRootNamespace() + ".RootCapableImpl";
    }

    protected String getDataModelUtilFQCN() {
        return getState().getConfig().getRootNamespace() + ".util.DataModelUtil";
    }

    protected String getParentPropertyTypeEnumFQN() {
        return getState().getConfig().getRootNamespace() + ".ParentPropertyType";
    }

    protected String getModelTypeEnumFQN() {
        return getState().getConfig().getRootNamespace() + ".ModelType";
    }

    protected String getModelReaderInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".io.ModelReader";
    }

    protected String getModelWriterInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".io.ModelWriter";
    }

    protected String getModelClonerInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".io.ModelCloner";
    }

    protected String getRootVisitorInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".visitors.Visitor";
    }

    protected String getAbstractTraverserFQN() {
        return getState().getConfig().getRootNamespace() + ".visitors.AbstractTraverser";
    }

    protected String getUnionInterfaceFQN() {
        return getUnionTypesPackageName() + ".Union";
    }



    public JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, PropertyModel property) {
        var entityType = (EntityType) property.getResolvedType();
        return resolveJavaEntity(namespace.fullName(), entityType.getName());
    }

    public JavaInterfaceSource resolveJavaEntityType(String namespace, PropertyModel property) {
        var entityType = (EntityType) property.getResolvedType();
        return resolveJavaEntity(namespace, entityType.getName());
    }

    public JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, Type type) {
        return resolveJavaEntity(namespace.fullName(), type.getName());
    }

    public JavaInterfaceSource resolveJavaEntityType(String namespace, Type type) {
        return resolveJavaEntity(namespace, type.getName());
    }

    public JavaInterfaceSource resolveJavaEntity(EntityModel entityModel) {
        return resolveJavaEntity(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    public JavaClassSource resolveJavaEntityImpl(EntityModel entityModel) {
        return resolveJavaEntityImpl(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    public JavaInterfaceSource resolveJavaEntity(String namespace, String entityName) {
        String _package = namespace;
        String prefix = getPrefix(namespace);
        String fqn = _package + "." + prefix + entityName;
        return lookupJavaEntity(fqn);
    }

    public JavaClassSource resolveJavaEntityImpl(String namespace, String entityName) {
        String _package = namespace;
        String prefix = getPrefix(namespace);
        String fqn = _package + "." + prefix + entityName + "Impl";
        return lookupJavaEntityImpl(fqn);
    }

    public JavaInterfaceSource resolveCommonJavaEntity(EntityModel entityModel) {
        return resolveCommonJavaEntity(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    public JavaInterfaceSource resolveCommonJavaEntity(NamespaceModel namespace, String entityName) {
        EntityModel commonEntity = getState().getConceptIndex().lookupCommonEntity(namespace.fullName(), entityName);
        return lookupJavaEntity(commonEntity);
    }

    public JavaInterfaceSource resolveCommonJavaEntity(String namespace, String entityName) {
        EntityModel commonEntity = getState().getConceptIndex().lookupCommonEntity(namespace, entityName);
        return lookupJavaEntity(commonEntity);
    }


    protected JavaInterfaceSource lookupJavaEntity(EntityModel entity) {
        return lookupJavaEntity(getJavaEntityInterfaceFQN(entity));
    }

    protected JavaInterfaceSource lookupJavaEntity(String fullyQualifiedName) {
        return getState().getJavaIndex().lookupInterface(fullyQualifiedName);
    }

    protected JavaInterfaceSource lookupJavaTrait(TraitModel trait) {
        return getState().getJavaIndex().lookupInterface(getJavaTraitInterfaceFQN(trait));
    }

    public JavaClassSource lookupJavaEntityImpl(EntityModel entity) {
        return lookupJavaEntityImpl(getJavaEntityClassFQN(entity));
    }

    public JavaClassSource lookupJavaEntityImpl(String fullyQualifiedName) {
        return getState().getJavaIndex().lookupClass(fullyQualifiedName);
    }

    protected JavaInterfaceSource lookupJavaVisitor(VisitorModel visitor) {
        String interfaceFQN = getVisitorInterfaceFullName(visitor);
        JavaInterfaceSource _interface = getState().getJavaIndex().lookupInterface(interfaceFQN);
        if (_interface == null) {
            warn("Visitor interface not found: " + interfaceFQN);
        }
        return _interface;
    }

    protected String prefixToModelType(String prefix) {
        return prefix.toUpperCase();
    }

    protected String getUnionTypesPackageName() {
        return getState().getConfig().getRootNamespace() + ".union";
    }

    public String getUnionTypeFQN(String name) {
        return getUnionTypesPackageName() + "." + name;
    }

    public String getUnionTypeFQN(String name, String namespace) {
        String pkg = namespace != null ? namespace : getUnionTypesPackageName();
        return pkg + "." + name;
    }

    protected String resolveUnionPackage(UnionType unionType) {
        for (var variant : unionType.getTypes()) {
            EntityType entityType = null;
            if (variant instanceof EntityType et) {
                entityType = et;
            } else if (variant instanceof ListType lt
                    && lt.getValueType() instanceof EntityType et) {
                entityType = et;
            } else if (variant instanceof MapType mt
                    && mt.getValueType() instanceof EntityType et) {
                entityType = et;
            }
            if (entityType != null) {
                EntityModel common = getState().getConceptIndex().lookupCommonEntity(
                        unionType.getNamespace(), entityType.getName());
                if (common != null) {
                    return common.getNamespace().fullName();
                }
            }
        }
        return getUnionTypesPackageName();
    }


}
