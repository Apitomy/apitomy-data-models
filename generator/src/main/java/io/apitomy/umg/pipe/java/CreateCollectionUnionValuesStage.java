package io.apitomy.umg.pipe.java;

import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 * Creates entity collection union value wrapper interfaces and implementations
 * by iterating union types in the type index and finding collection variants
 * that contain entities: lists or maps of an entity, and lists of a union type alias
 * whose variants include an entity (e.g. {@code boolean|Schema|[SchemaOrBoolean]}).
 */
public class CreateCollectionUnionValuesStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findTypes("").stream()
                .filter(t -> t instanceof UnionType)
                .map(t -> (UnionType) t)
                .forEach(this::processUnionType);
    }

    private void processUnionType(UnionType unionType) {
        for (var variantType : unionType.getTypes()) {
            if (variantType instanceof ListType listType && listType.getValueType() instanceof EntityType entityType) {
                createEntityCollectionUnionValue(unionType, entityType, true);
            } else if (variantType instanceof MapType mapType && mapType.getValueType() instanceof EntityType entityType) {
                createEntityCollectionUnionValue(unionType, entityType, false);
            } else if (variantType instanceof ListType listType && listType.getValueType() instanceof UnionType elementUnion) {
                if (elementUnion.getAliasName() == null) {
                    throw new IllegalStateException("Union '" + unionType.getName() + "' has a variant that is a list "
                            + "of an anonymous union. Declare the element union as a type alias.");
                }
                createUnionListUnionValue(unionType, elementUnion);
            }
        }
    }

    /**
     * Creates the wrapper for a list variant whose elements are values of a union type alias, e.g.
     * {@code SchemaOrBooleanListUnionValue} holding a {@code List<SchemaOrBoolean>}. It builds on the
     * entity-list base class, which traverses and attaches only the elements that are nodes.
     */
    private void createUnionListUnionValue(UnionType unionType, UnionType elementUnion) {
        String elementName = elementUnion.getAliasName();
        String unionValueName = elementName + "ListUnionValue";
        String unionValueImplName = unionValueName + "Impl";
        String _package = resolveUnionPackage(unionType);
        String sharedPackage = getUnionTypesPackageName();
        String unionValueFQN = _package + "." + unionValueName;
        if (getState().getJavaIndex().lookupInterface(unionValueFQN) != null) {
            return;
        }

        debug("Creating union list union value: %s", unionValueName);

        // The element union's interface is created by a later stage; reference it by name.
        String elementFQN = resolveUnionPackage(elementUnion) + "." + elementName;
        String baseName = "EntityListUnionValue";
        var baseSource = getState().getJavaIndex().lookupInterface(sharedPackage + "." + baseName);
        var baseImplSource = getState().getJavaIndex().lookupClass(sharedPackage + "." + baseName + "Impl");

        JavaInterfaceSource valueInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(unionValueName)
                .setPublic();
        valueInterface.addImport(baseSource);
        valueInterface.addImport(elementFQN);
        valueInterface.addInterface(baseName + "<" + elementName + ">");
        getState().getJavaIndex().index(valueInterface);

        JavaClassSource valueImpl = Roaster.create(JavaClassSource.class)
                .setPackage(_package)
                .setName(unionValueImplName)
                .setPublic();
        valueImpl.addImport(baseImplSource);
        valueImpl.addImport(elementFQN);
        valueImpl.addImport(java.util.List.class);
        valueImpl.addInterface(valueInterface);
        valueImpl.setSuperType(baseName + "Impl<" + elementName + ">");

        MethodSource<JavaClassSource> defaultConstructor = valueImpl.addMethod().setPublic().setConstructor(true);
        defaultConstructor.setBody("super();");

        MethodSource<JavaClassSource> valueConstructor = valueImpl.addMethod().setPublic().setConstructor(true);
        valueConstructor.addParameter("List<" + elementName + ">", "value");
        valueConstructor.setBody("super(value);");

        getState().getJavaIndex().index(valueImpl);
    }

    private void createEntityCollectionUnionValue(UnionType unionType, EntityType entityType, boolean isList) {
        String typeName = JavaTypeFactory.getUnionComponentName(entityType);
        String mapOrList = isList ? "List" : "Map";
        String unionValueName = typeName + mapOrList + "UnionValue";
        String unionValueImplName = unionValueName + "Impl";
        String _package = resolveUnionPackage(unionType);
        String sharedPackage = getUnionTypesPackageName();
        String unionValueFQN = _package + "." + unionValueName;

        if (getState().getJavaIndex().lookupInterface(unionValueFQN) != null) {
            return;
        }

        debug("Creating collection union value: %s", unionValueName);

        // Base class/interface (always in shared package)
        String entityCollectionUnionValueName = "Entity" + mapOrList + "UnionValue";
        String entityCollectionUnionValueImplName = entityCollectionUnionValueName + "Impl";
        String entityCollectionUnionValueFQN = sharedPackage + "." + entityCollectionUnionValueName;
        String entityCollectionUnionValueImplFQN = sharedPackage + "." + entityCollectionUnionValueImplName;

        // Resolve entity Java interface using common resolution
        var nsModel = getState().getConceptIndex().lookupNamespace(unionType.getNamespace());
        var entitySource = resolveCommonJavaEntity(nsModel, entityType.getName());
        var entityCollectionUnionValueSource = getState().getJavaIndex().lookupInterface(entityCollectionUnionValueFQN);
        var entityCollectionUnionValueImplSource = getState().getJavaIndex().lookupClass(entityCollectionUnionValueImplFQN);

        // Create wrapper interface
        JavaInterfaceSource valueInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(unionValueName)
                .setPublic();
        valueInterface.addImport(entityCollectionUnionValueSource);
        valueInterface.addImport(entitySource);
        if (isList) {
            valueInterface.addInterface(entityCollectionUnionValueName + "<" + entitySource.getName() + ">");
        } else {
            valueInterface.addInterface(entityCollectionUnionValueName + "<String, " + entitySource.getName() + ">");
        }
        getState().getJavaIndex().index(valueInterface);

        // Create wrapper impl
        JavaClassSource valueImpl = Roaster.create(JavaClassSource.class)
                .setPackage(_package)
                .setName(unionValueImplName)
                .setPublic();
        valueImpl.addImport(entityCollectionUnionValueImplSource);
        valueImpl.addImport(entitySource);
        valueImpl.addImport(java.util.List.class);
        valueImpl.addInterface(valueInterface);
        if (isList) {
            valueImpl.setSuperType(entityCollectionUnionValueImplName + "<" + entitySource.getName() + ">");
        } else {
            valueImpl.setSuperType(entityCollectionUnionValueImplName + "<String, " + entitySource.getName() + ">");
        }

        MethodSource<JavaClassSource> defaultConstructor = valueImpl.addMethod().setPublic().setConstructor(true);
        defaultConstructor.setBody("super();");

        MethodSource<JavaClassSource> valueConstructor = valueImpl.addMethod().setPublic().setConstructor(true);
        valueConstructor.addParameter("List<" + entitySource.getName() + ">", "value");
        valueConstructor.setBody("super(value);");

        getState().getJavaIndex().index(valueImpl);
    }
}
