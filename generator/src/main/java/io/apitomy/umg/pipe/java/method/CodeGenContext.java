package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;
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
import java.util.Map;

import static java.util.Map.entry;

/**
 * Self-contained context that provides entity resolution, naming logic, and
 * index lookups needed by code-generation blocks and method classes.
 * Replaces the former CodeGenContext interface and ImplMethodContext interface.
 */
public class CodeGenContext {

    private static final Inflector inflector = new Inflector();

    public static final Map<String, String> JAVA_KEYWORD_MAP = Map.ofEntries(
            entry("default", "_default"),
            entry("enum", "_enum"),
            entry("const", "_const"),
            entry("if", "_if"),
            entry("else", "_else"));

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

    public String getModelTypeEnumFQN() {
        return rootNamespace + ".ModelType";
    }

    /**
     * Resolves the package for union value impl classes, preferring the common-entity
     * namespace when a variant references a common entity.
     */
    public String resolveUnionPackage(io.apitomy.umg.models.concept.type.UnionType unionType) {
        for (var variant : unionType.getTypes()) {
            io.apitomy.umg.models.concept.type.EntityType entityType = null;
            if (variant instanceof io.apitomy.umg.models.concept.type.EntityType et) {
                entityType = et;
            } else if (variant instanceof io.apitomy.umg.models.concept.type.ListType lt
                    && lt.getValueType() instanceof io.apitomy.umg.models.concept.type.EntityType et) {
                entityType = et;
            } else if (variant instanceof io.apitomy.umg.models.concept.type.MapType mt
                    && mt.getValueType() instanceof io.apitomy.umg.models.concept.type.EntityType et) {
                entityType = et;
            }
            if (entityType != null) {
                EntityModel common = conceptIndex.lookupCommonEntity(
                        unionType.getNamespace(), entityType.getName());
                if (common != null) {
                    return common.getNamespace().fullName();
                }
            }
        }
        return getUnionTypesPackageName();
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

    public JavaInterfaceSource resolveJavaEntityType(String namespace, Type type) {
        return resolveJavaEntity(namespace, type.getName());
    }

    public JavaInterfaceSource resolveJavaEntityType(String namespace, String entityName) {
        return resolveJavaEntity(namespace, entityName);
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

    public JavaInterfaceSource lookupJavaEntity(EntityModel entity) {
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

    /**
     * Checks whether the given method holder already contains a method with the given name.
     */
    public boolean hasNamedMethod(MethodHolderSource<?> holder, String methodName) {
        for (MethodSource<?> method : holder.getMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
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
        return JAVA_KEYWORD_MAP.getOrDefault(name, name);
    }

    // --- Logging ---

    public void warn(String message) {
        Logger.warn("[" + logPrefix + "] " + message);
    }

    public void error(String message) {
        Logger.error("[" + logPrefix + "] " + message);
    }
}
