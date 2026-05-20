package io.apitomy.umg.pipe.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.models.concept.TraitModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.AbstractStage;

public abstract class AbstractJavaStage extends AbstractStage {

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

    protected String getTraverserClassName(SpecificationVersion specVersion) {
        return specVersion.getPrefix() + "Traverser";
    }

    protected String getTraverserPackageName(SpecificationVersion specVersion) {
        return specVersion.getNamespace() + ".visitors";
    }

    /**
     * Determines the package to use for the interface generated for the given visitor.
     * @param visitor
     */
    protected String getVisitorInterfacePackageName(VisitorModel visitor) {
        String packageName = visitor.getNamespace().fullName();
        String visitorPackageName = packageName + ".visitors";
        return visitorPackageName;
    }

    /**
     * Determines the prefix to use for the interface name for the given visitor.
     * @param visitor
     */
    protected String getVisitorInterfacePrefix(VisitorModel visitor) {
        return (visitor.getParent() == null) ? "" : getState().getSpecIndex().prefixForNS(visitor.getNamespace().fullName());
    }

    /**
     * Determines the interface name for the given visitor.
     * @param visitor
     */
    protected String getVisitorInterfaceName(VisitorModel visitor) {
        String visitorPrefix = getVisitorInterfacePrefix(visitor);
        String visitorInterfaceName = visitorPrefix + "Visitor";
        return visitorInterfaceName;
    }

    /**
     * Determines the fully qualified name of the Java interface for a given visitor.
     * @param visitor
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
        return Util.JAVA_KEYWORD_MAP.getOrDefault(name, name);
    }

    protected String getPrefix(NamespaceModel namespace) {
        return getPrefix(namespace.fullName());
    }

    protected String getPrefix(String namespace) {
        String prefix = getState().getSpecIndex().getNsToPrefix().get(namespace);
        return prefix == null ? "" : prefix;
    }

    protected String getJavaEntityInterfaceFQN(EntityModel entity) {
        return getJavaEntityInterfacePackage(entity) + "." + getJavaEntityInterfaceName(entity);
    }

    protected String getJavaTraitInterfaceFQN(TraitModel trait) {
        return getJavaTraitInterfacePackage(trait) + "." + getJavaTraitInterfaceName(trait);
    }

    protected String getJavaEntityClassFQN(EntityModel entity) {
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
        return getState().getConfig().getRootNamespace() + ".RootNode";
    }

    protected String getMappedNodeInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".MappedNode";
    }

    protected String getNodeEntityClassFQN() {
        return getState().getConfig().getRootNamespace() + ".NodeImpl";
    }

    protected String getRootNodeEntityClassFQN() {
        return getState().getConfig().getRootNamespace() + ".RootNodeImpl";
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

    protected String getRootVisitorInterfaceFQN() {
        return getState().getConfig().getRootNamespace() + ".visitors.Visitor";
    }

    protected String getAbstractTraverserFQN() {
        return getState().getConfig().getRootNamespace() + ".visitors.AbstractTraverser";
    }

    protected String getUnionInterfaceFQN() {
        return getUnionTypesPackageName() + ".Union";
    }

    protected String getUnionValueInterfaceFQN() {
        return getUnionTypesPackageName() + ".UnionValue";
    }

    protected String createMethodName(EntityModel entityModel) {
        return createMethodName(entityModel.getName());
    }

    protected String createMethodName(PropertyModel propertyModel) {
        return createMethodName(propertyModel.getName());
    }

    protected String createMethodName(String entityName) {
        return "create" + StringUtils.capitalize(entityName);
    }

    protected String addMethodName(EntityModel entityModel) {
        return addMethodName(entityModel.getName());
    }

    protected String addMethodName(PropertyModel propertyModel) {
        return addMethodName(propertyModel.getName());
    }

    protected String addMethodName(String name) {
        return "add" + StringUtils.capitalize(name);
    }

    protected String insertMethodName(EntityModel entityModel) {
        return insertMethodName(entityModel.getName());
    }

    protected String insertMethodName(PropertyModel propertyModel) {
        return insertMethodName(propertyModel.getName());
    }

    protected String insertMethodName(String name) {
        return "insert" + StringUtils.capitalize(name);
    }

    protected String clearMethodName(EntityModel entityModel) {
        return clearMethodName(entityModel.getName());
    }

    protected String clearMethodName(PropertyModel propertyModel) {
        return clearMethodName(propertyModel.getName());
    }

    protected String clearMethodName(String name) {
        return "clear" + StringUtils.capitalize(name);
    }

    protected String removeMethodName(EntityModel entityModel) {
        return removeMethodName(entityModel.getName());
    }

    protected String removeMethodName(PropertyModel propertyModel) {
        return removeMethodName(propertyModel.getName());
    }

    protected String removeMethodName(String name) {
        return "remove" + StringUtils.capitalize(name);
    }

    protected String readMethodName(EntityModel entityModel) {
        return readMethodName(entityModel.getName());
    }

    protected String readMethodName(String entityName) {
        return "read" + StringUtils.capitalize(entityName);
    }

    protected String getterMethodName(PropertyModel propertyModel) {
        String name = propertyModel.getName();
        if (name.startsWith("/")) {
            name = propertyModel.getCollection();
        }
        return getterMethodName(name, propertyModel.getType());
    }

    protected String getterMethodName(String propertyName, PropertyType type) {
        boolean isBool = type.isPrimitiveType() && type.getSimpleType().equals("boolean");
        return (isBool ? "is" : "get") + StringUtils.capitalize(propertyName);
    }

    protected String setterMethodName(PropertyModel propertyModel) {
        return "set" + StringUtils.capitalize(propertyModel.getName());
    }

    protected Class<?> primitiveTypeToClass(PropertyType type) {
        if (!type.isPrimitiveType()) {
            throw new UnsupportedOperationException("Property type not primitive: " + type);
        }
        Class<?> rval = Util.PRIMITIVE_TYPE_MAP.get(type.getSimpleType());
        if (rval == null) {
            throw new UnsupportedOperationException("Primitive-to-class mapping not found for: " + type.getSimpleType());
        }
        return rval;
    }

    protected JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, PropertyModel property) {
        return resolveJavaEntityType(namespace.fullName(), property.getType());
    }

    protected JavaInterfaceSource resolveJavaEntityType(String namespace, PropertyModel property) {
        return resolveJavaEntityType(namespace, property.getType());
    }

    protected JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, PropertyType type) {
        return resolveJavaEntity(namespace.fullName(), type.getSimpleType());
    }

    protected JavaInterfaceSource resolveJavaEntityType(String namespace, PropertyType type) {
        return resolveJavaEntity(namespace, type.getSimpleType());
    }

    protected JavaInterfaceSource resolveJavaEntity(EntityModel entityModel) {
        return resolveJavaEntity(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    protected JavaClassSource resolveJavaEntityImpl(EntityModel entityModel) {
        return resolveJavaEntityImpl(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    protected JavaInterfaceSource resolveJavaEntity(String namespace, String entityName) {
        String _package = namespace;
        String prefix = getPrefix(namespace);
        String fqn = _package + "." + prefix + entityName;
        return lookupJavaEntity(fqn);
    }

    protected JavaClassSource resolveJavaEntityImpl(String namespace, String entityName) {
        String _package = namespace;
        String prefix = getPrefix(namespace);
        String fqn = _package + "." + prefix + entityName + "Impl";
        return lookupJavaEntityImpl(fqn);
    }

    protected JavaInterfaceSource resolveCommonJavaEntity(EntityModel entityModel) {
        return resolveCommonJavaEntity(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    protected JavaInterfaceSource resolveCommonJavaEntity(NamespaceModel namespace, String entityName) {
        EntityModel commonEntity = getState().getConceptIndex().lookupCommonEntity(namespace.fullName(), entityName);
        return lookupJavaEntity(commonEntity);
    }

    protected JavaInterfaceSource resolveCommonJavaEntity(String namespace, String entityName) {
        EntityModel commonEntity = getState().getConceptIndex().lookupCommonEntity(namespace, entityName);
        return lookupJavaEntity(commonEntity);
    }

    protected boolean hasNamedMethod(MethodHolderSource<?> entityInterface, String methodName) {
        for (MethodSource<?> method : entityInterface.getMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
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

    protected JavaClassSource lookupJavaEntityImpl(EntityModel entity) {
        return lookupJavaEntityImpl(getJavaEntityClassFQN(entity));
    }

    protected JavaClassSource lookupJavaEntityImpl(String fullyQualifiedName) {
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

    protected String getUnionTypeFQN(String name) {
        return getUnionTypesPackageName() + "." + name;
    }

    public class JavaType {
        private final PropertyType propertyType;
        private final String namespaceContext;
        private boolean useCommonEntityResolution;

        public JavaType(PropertyType type, String namespaceContext) {
            this.propertyType = type;
            this.namespaceContext = namespaceContext;
        }

        public JavaType(PropertyType type, NamespaceModel namespace) {
            this(type, namespace.fullName());
        }

        public JavaType(PropertyModelWithOrigin property) {
            this(property.getProperty().getType(), property.getOrigin().getNamespace());
        }

        public JavaType useCommonEntityResolution() {
            this.useCommonEntityResolution = true;
            return this;
        }

        public boolean isEntityList() {
            return propertyType.isList() && propertyType.getNested().iterator().next().isEntityType();
        }

        public boolean isEntityMap() {
            return propertyType.isMap() && propertyType.getNested().iterator().next().isEntityType();
        }

        public boolean isEntity() {
            return propertyType.isEntityType();
        }

        public boolean isUnion() {
            return propertyType.isUnion();
        }

        public boolean isPrimitive() {
            return propertyType.isPrimitiveType();
        }

        public boolean isPrimitiveList() {
            return propertyType.isList() && propertyType.getNested().iterator().next().isPrimitiveType();
        }

        public boolean isPrimitiveMap() {
            return propertyType.isMap() && propertyType.getNested().iterator().next().isPrimitiveType();
        }

        public boolean isUnionList() {
            return propertyType.isList() && propertyType.getNested().iterator().next().isUnion();
        }

        public boolean isUnionMap() {
            return propertyType.isMap() && propertyType.getNested().iterator().next().isUnion();
        }

        public Class<?> toClass() {
            return toClass(propertyType);
        }

        private Class<?> toClass(PropertyType type) {
            if (!type.isPrimitiveType()) {
                throw new UnsupportedOperationException("Only allowed for primitive types: " + type);
            }
            Class<?> rval = Util.PRIMITIVE_TYPE_MAP.get(type.getSimpleType());
            if (rval == null) {
                throw new UnsupportedOperationException("Primitive-to-class mapping not found for: " + type.getSimpleType());
            }
            return rval;
        }

        public void addImportsTo(Importer<?> importer) {
            // Handle map of list of primitives.
            if (propertyType.isMap() && propertyType.getNested().iterator().next().isList() &&
                    propertyType.getNested().iterator().next().getNested().iterator().next().isPrimitiveType()) {
                PropertyType listType = propertyType.getNested().iterator().next().getNested().iterator().next();
                Class<?> pType = primitiveTypeToClass(listType);
                importer.addImport(pType);
                importer.addImport(List.class);
                importer.addImport(Map.class);
            } else if (isPrimitiveList()) {
                Class<?> listType = primitiveTypeToClass(propertyType.getNested().iterator().next());
                importer.addImport(List.class);
                importer.addImport(listType);
            } else if (isPrimitiveMap()) {
                Class<?> mapType = primitiveTypeToClass(propertyType.getNested().iterator().next());
                importer.addImport(Map.class);
                importer.addImport(mapType);
            } else if (isPrimitive()) {
                Class<?> returnType = primitiveTypeToClass(propertyType);
                importer.addImport(returnType);
            } else if (isEntity()) {
                JavaInterfaceSource entityType = useCommonEntityResolution ?
                        resolveCommonJavaEntity(namespaceContext, propertyType.getSimpleType()) :
                            resolveJavaEntityType(namespaceContext, propertyType);
                if (entityType == null) {
                    throw new UnsupportedOperationException("Java interface for entity type not found: " + propertyType);
                } else {
                    importer.addImport(entityType);
                }
            } else if (isUnion()) {
                UnionPropertyType ut = new UnionPropertyType(propertyType);
                ut.addImportsTo(importer);
            } else if (isUnionList()) {
                PropertyType unionType = propertyType.getNested().iterator().next();
                UnionPropertyType ut = new UnionPropertyType(unionType);
                ut.addImportsTo(importer);
                importer.addImport(List.class);
            } else if (isUnionMap()) {
                PropertyType unionType = propertyType.getNested().iterator().next();
                UnionPropertyType ut = new UnionPropertyType(unionType);
                ut.addImportsTo(importer);
                importer.addImport(Map.class);
            } else if (isEntityList()) {
                JavaInterfaceSource listType = useCommonEntityResolution ?
                        resolveCommonJavaEntity(namespaceContext, propertyType.getNested().iterator().next().getSimpleType()) :
                            resolveJavaEntityType(namespaceContext, propertyType.getNested().iterator().next());
                if (listType == null) {
                    throw new UnsupportedOperationException("Java interface for entity type not found: " + propertyType);
                } else {
                    importer.addImport(List.class);
                    importer.addImport(listType);
                }
            } else if (isEntityMap()) {
                JavaInterfaceSource mapType = useCommonEntityResolution ?
                        resolveCommonJavaEntity(namespaceContext, propertyType.getNested().iterator().next().getSimpleType()) :
                            resolveJavaEntityType(namespaceContext, propertyType.getNested().iterator().next());
                if (mapType == null) {
                    throw new UnsupportedOperationException("Java interface for entity type not found: " + propertyType);
                } else {
                    importer.addImport(Map.class);
                    importer.addImport(mapType);
                }
            } else {
                throw new UnsupportedOperationException("Java type not supported: " + propertyType);
            }
        }

        public String toJavaTypeString() {
            if (propertyType.isMap() && propertyType.getNested().iterator().next().isList() &&
                    propertyType.getNested().iterator().next().getNested().iterator().next().isPrimitiveType()) {
                PropertyType listType = propertyType.getNested().iterator().next().getNested().iterator().next();
                Class<?> pType = primitiveTypeToClass(listType);
                return "Map<String, List<" + pType.getSimpleName() + ">>";
            } else if (isPrimitiveList()) {
                Class<?> listType = primitiveTypeToClass(propertyType.getNested().iterator().next());
                return "List<" + listType.getSimpleName() + ">";
            } else if (isPrimitiveMap()) {
                Class<?> mapType = primitiveTypeToClass(propertyType.getNested().iterator().next());
                return "Map<String, " + mapType.getSimpleName() + ">";
            } else if (isPrimitive()) {
                Class<?> returnType = primitiveTypeToClass(propertyType);
                return returnType.getSimpleName();
            } else if (isEntity()) {
                JavaInterfaceSource entityType = useCommonEntityResolution ?
                        resolveCommonJavaEntity(namespaceContext, propertyType.getSimpleType()) :
                            resolveJavaEntityType(namespaceContext, propertyType);
                if (entityType == null) {
                    throw new UnsupportedOperationException("Java interface for entity type not found: " + propertyType);
                } else {
                    return entityType.getName();
                }
            } else if (isUnion()) {
                UnionPropertyType ut = new UnionPropertyType(propertyType);
                return ut.toJavaTypeString();
            } else if (isUnionList()) {
                PropertyType unionType = propertyType.getNested().iterator().next();
                UnionPropertyType ut = new UnionPropertyType(unionType);
                return "List<" + ut.toJavaTypeString() + ">";
            } else if (isUnionMap()) {
                PropertyType unionType = propertyType.getNested().iterator().next();
                UnionPropertyType ut = new UnionPropertyType(unionType);
                return "Map<String, " + ut.toJavaTypeString() + ">";
            } else if (isEntityList()) {
                JavaInterfaceSource listType = useCommonEntityResolution ?
                        resolveCommonJavaEntity(namespaceContext, propertyType.getNested().iterator().next().getSimpleType()) :
                            resolveJavaEntityType(namespaceContext, propertyType.getNested().iterator().next());
                if (listType == null) {
                    throw new UnsupportedOperationException("Java interface for entity type not found: " + propertyType);
                } else {
                    return "List<" + listType.getName() + ">";
                }
            } else if (isEntityMap()) {
                JavaInterfaceSource mapType = useCommonEntityResolution ?
                        resolveCommonJavaEntity(namespaceContext, propertyType.getNested().iterator().next().getSimpleType()) :
                            resolveJavaEntityType(namespaceContext, propertyType.getNested().iterator().next());
                if (mapType == null) {
                    throw new UnsupportedOperationException("Java interface for entity type not found: " + propertyType);
                } else {
                    return "Map<String, " + mapType.getName() + ">";
                }
            } else {
                throw new UnsupportedOperationException("Java type not supported: " + propertyType);
            }
        }
    }

    public static String getTypeName(PropertyType type) {
        if (type.isEntityType()) {
            return type.getSimpleType();
        } else if (type.isPrimitiveType()) {
            return StringUtils.capitalize(type.getSimpleType());
        } else if (type.isUnion()) {
            List<PropertyType> nestedTypes = new ArrayList<>(type.getNested());
            return getUnionTypeName(nestedTypes);
        } else if (type.isList()) {
            return getTypeName(type.getNested().iterator().next()) + "List";
        } else if (type.isMap()) {
            return getTypeName(type.getNested().iterator().next()) + "Map";
        } else {
            throw new RuntimeException("Unsupported type in union: " + type);
        }
    }

    private static String getUnionTypeName(List<PropertyType> unionNestedTypes) {
        return unionNestedTypes.stream().map(pt -> getTypeName(pt)).reduce((t, u) -> t + u).orElseThrow() + "Union";
    }

    public class UnionPropertyType {

        public UnionPropertyType(PropertyType pType) {
            List<PropertyType> nt = new ArrayList<>(pType.getNested().size());
            nt.addAll(pType.getNested());
            nt.sort(new Comparator<PropertyType>() {
                @Override
                public int compare(PropertyType o1, PropertyType o2) {
                    return o1.toString().compareToIgnoreCase(o2.toString());
                }
            });

            String name = getUnionTypeName(nt);
            this.name = name;
            this.nestedTypes = nt;
        }

        private final String name;
        private final List<PropertyType> nestedTypes;

        public void addImportsTo(Importer<?> importer) {
            String unionTypeFQN = getUnionTypeFQN(name);
            importer.addImport(unionTypeFQN);
        }

        public String getName() {
            return name;
        }

        public List<PropertyType> getNestedTypes() {
            return nestedTypes;
        }

        public String toJavaTypeString() {
            return name;
        }

    }

}
