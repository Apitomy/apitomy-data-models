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
 * that contain entities.
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
            }
        }
    }

    private void createEntityCollectionUnionValue(UnionType unionType, EntityType entityType, boolean isList) {
        String typeName = JavaTypeFactory.getUnionComponentName(entityType);
        String mapOrList = isList ? "List" : "Map";
        String unionValueName = typeName + mapOrList + "UnionValue";
        String unionValueImplName = unionValueName + "Impl";
        String _package = getUnionTypesPackageName();
        String unionValueFQN = _package + "." + unionValueName;

        if (getState().getJavaIndex().lookupInterface(unionValueFQN) != null) {
            return;
        }

        debug("Creating collection union value: %s", unionValueName);

        // Base class/interface
        String entityCollectionUnionValueName = "Entity" + mapOrList + "UnionValue";
        String entityCollectionUnionValueImplName = entityCollectionUnionValueName + "Impl";
        String entityCollectionUnionValueFQN = _package + "." + entityCollectionUnionValueName;
        String entityCollectionUnionValueImplFQN = _package + "." + entityCollectionUnionValueImplName;

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
