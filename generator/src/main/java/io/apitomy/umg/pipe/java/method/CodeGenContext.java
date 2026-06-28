package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.modeshape.common.text.Inflector;

import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.concept.SpecificationIndex;
import io.apitomy.umg.index.java.JavaIndex;
import io.apitomy.umg.logging.Logger;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.java.Util;

/**
 * Self-contained context that provides entity resolution, naming logic, and
 * index lookups needed by code-generation blocks and method classes.
 * Replaces the former CodeGenContext interface and ImplMethodContext interface.
 */
public class CodeGenContext {

    private static final Inflector inflector = new Inflector();

    private final ConceptIndex conceptIndex;
    private final JavaIndex javaIndex;
    private final JavaTypeFactory javaTypeFactory;
    private final String rootNamespace;
    private final SpecificationIndex specIndex;
    private final String logPrefix;

    public CodeGenContext(ConceptIndex conceptIndex, JavaIndex javaIndex,
                          JavaTypeFactory javaTypeFactory, String rootNamespace,
                          SpecificationIndex specIndex, String logPrefix) {
        this.conceptIndex = conceptIndex;
        this.javaIndex = javaIndex;
        this.javaTypeFactory = javaTypeFactory;
        this.rootNamespace = rootNamespace;
        this.specIndex = specIndex;
        this.logPrefix = logPrefix;
    }

    // --- Index accessors ---

    public ConceptIndex getConceptIndex() {
        return conceptIndex;
    }

    public JavaIndex getJavaIndex() {
        return javaIndex;
    }

    public JavaTypeFactory getJavaTypeFactory() {
        return javaTypeFactory;
    }

    // --- FQN construction ---

    public String getJavaEntityInterfaceFQN(EntityModel entity) {
        return getJavaEntityInterfacePackage(entity) + "." + getJavaEntityInterfaceName(entity);
    }

    public String getJavaEntityClassFQN(EntityModel entity) {
        return getJavaEntityClassPackage(entity) + "." + getJavaEntityClassName(entity);
    }

    public String getNodeEntityClassFQN() {
        return rootNamespace + ".NodeImpl";
    }

    public String getDataModelUtilFQCN() {
        return rootNamespace + ".util.DataModelUtil";
    }

    public String getParentPropertyTypeEnumFQN() {
        return rootNamespace + ".ParentPropertyType";
    }

    public String getUnionValueInterfaceFQN() {
        return getUnionTypesPackageName() + ".UnionValue";
    }

    public String getUnionTypeFQN(String name) {
        return getUnionTypesPackageName() + "." + name;
    }

    // --- Naming helpers ---

    private String getJavaEntityInterfaceName(EntityModel entity) {
        String prefix = getPrefix(entity.getNamespace().fullName());
        return prefix + entity.getName();
    }

    private String getJavaEntityClassName(EntityModel entity) {
        String prefix = getPrefix(entity.getNamespace().fullName());
        return prefix + entity.getName() + "Impl";
    }

    private String getJavaEntityInterfacePackage(EntityModel entity) {
        return entity.getNamespace().fullName();
    }

    private String getJavaEntityClassPackage(EntityModel entity) {
        return entity.getNamespace().fullName();
    }

    private String getPrefix(String namespace) {
        String prefix = specIndex.getNsToPrefix().get(namespace);
        return prefix == null ? "" : prefix;
    }

    private String getUnionTypesPackageName() {
        return rootNamespace + ".union";
    }

    // --- Entity resolution ---

    public JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, PropertyModel property) {
        var entityType = (io.apitomy.umg.models.concept.type.EntityType) property.getResolvedType();
        return resolveJavaEntity(namespace.fullName(), entityType.getName());
    }

    public JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, Type type) {
        return resolveJavaEntity(namespace.fullName(), type.getName());
    }

    public JavaInterfaceSource resolveJavaEntity(EntityModel entityModel) {
        return resolveJavaEntity(entityModel.getNamespace().fullName(), entityModel.getName());
    }

    private JavaInterfaceSource resolveJavaEntity(String namespace, String entityName) {
        String prefix = getPrefix(namespace);
        String fqn = namespace + "." + prefix + entityName;
        return lookupJavaEntity(fqn);
    }

    public JavaInterfaceSource resolveCommonJavaEntity(EntityModel entityModel) {
        EntityModel commonEntity = conceptIndex.lookupCommonEntity(
                entityModel.getNamespace().fullName(), entityModel.getName());
        return lookupJavaEntity(commonEntity);
    }

    private JavaInterfaceSource lookupJavaEntity(EntityModel entity) {
        return lookupJavaEntity(getJavaEntityInterfaceFQN(entity));
    }

    private JavaInterfaceSource lookupJavaEntity(String fullyQualifiedName) {
        return javaIndex.lookupInterface(fullyQualifiedName);
    }

    public JavaClassSource lookupJavaEntityImpl(EntityModel entity) {
        return lookupJavaEntityImpl(getJavaEntityClassFQN(entity));
    }

    public JavaClassSource lookupJavaEntityImpl(String fullyQualifiedName) {
        return javaIndex.lookupClass(fullyQualifiedName);
    }

    // --- Primitive type mapping ---

    public Class<?> primitiveTypeToClass(Type type) {
        return PrimitiveTypeHelper.primitiveTypeToClass(type);
    }

    public String singularize(String name) {
        return inflector.singularize(name);
    }

    // --- Field name ---

    public String getFieldName(PropertyModel property) {
        if (property.getName().equals("*")) {
            return "_items";
        }
        if (property.getName().startsWith("/")) {
            return sanitizeFieldName(property.getCollection());
        }
        return sanitizeFieldName(property.getName());
    }

    private String sanitizeFieldName(String name) {
        if (name == null) {
            return null;
        }
        return Util.JAVA_KEYWORD_MAP.getOrDefault(name, name);
    }

    // --- Logging ---

    public void warn(String message) {
        Logger.warn("[" + logPrefix + "] " + message);
    }

    public void error(String message) {
        Logger.error("[" + logPrefix + "] " + message);
    }
}
