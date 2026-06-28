package io.apitomy.umg.pipe.java;

import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 * Creates primitive union value wrapper interfaces and implementations
 * by iterating all {@link PrimitiveUnionVariantType} instances in the type index.
 * <p>
 * Replaces hand-written classes: BooleanUnionValue(Impl), StringUnionValue(Impl),
 * ObjectUnionValue(Impl), AnyUnionValue(Impl).
 */
public class CreatePrimitiveUnionValuesStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findTypes("").stream()
                .filter(t -> t instanceof PrimitiveUnionVariantType)
                .map(t -> (PrimitiveUnionVariantType) t)
                .forEach(this::createPrimitiveUnionValue);
    }

    private void createPrimitiveUnionValue(PrimitiveUnionVariantType puv) {
        String typeName = StringUtils.capitalize(puv.getType().name().toLowerCase());
        Class<?> javaClass = PrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(puv.getType().name().toLowerCase());
        if (javaClass == null) return;

        String _package = getUnionTypesPackageName();
        String interfaceName = typeName + "UnionValue";
        String implName = interfaceName + "Impl";
        String interfaceFQN = _package + "." + interfaceName;

        if (getState().getJavaIndex().lookupInterface(interfaceFQN) != null) {
            return;
        }

        debug("Creating primitive union value: %s", interfaceName);

        // Create interface: XxxUnionValue extends PrimitiveUnionValue<Xxx>
        String primitiveUnionValueFQN = _package + ".PrimitiveUnionValue";
        JavaInterfaceSource primitiveUnionValueSource = getState().getJavaIndex().lookupInterface(primitiveUnionValueFQN);

        JavaInterfaceSource valueInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(interfaceName)
                .setPublic();
        valueInterface.addImport(primitiveUnionValueSource);
        valueInterface.addImport(javaClass);
        valueInterface.addInterface("PrimitiveUnionValue<" + javaClass.getSimpleName() + ">");
        getState().getJavaIndex().index(valueInterface);

        // Create impl: XxxUnionValueImpl extends PrimitiveUnionValueImpl<Xxx> implements XxxUnionValue
        String primitiveUnionValueImplFQN = _package + ".PrimitiveUnionValueImpl";
        JavaClassSource primitiveUnionValueImplSource = getState().getJavaIndex().lookupClass(primitiveUnionValueImplFQN);

        JavaClassSource valueImpl = Roaster.create(JavaClassSource.class)
                .setPackage(_package)
                .setName(implName)
                .setPublic();
        valueImpl.addImport(primitiveUnionValueImplSource);
        valueImpl.addImport(javaClass);
        valueImpl.addImport(valueInterface);
        valueImpl.setSuperType("PrimitiveUnionValueImpl<" + javaClass.getSimpleName() + ">");
        valueImpl.addInterface(valueInterface);

        // Constructor with value
        MethodSource<JavaClassSource> constructor = valueImpl.addMethod().setPublic().setConstructor(true);
        constructor.addParameter(javaClass.getSimpleName(), "value");
        constructor.setBody("super(value);");

        // Constructor with value + ModelType (for root union values)
        String modelTypeFQN = getModelTypeEnumFQN();
        JavaInterfaceSource modelTypeSource = getState().getJavaIndex().lookupInterface(modelTypeFQN);
        if (modelTypeSource == null) {
            var modelTypeEnum = getState().getJavaIndex().lookupEnum(modelTypeFQN);
            if (modelTypeEnum != null) {
                valueImpl.addImport(modelTypeEnum);
            }
        }
        MethodSource<JavaClassSource> rootConstructor = valueImpl.addMethod().setPublic().setConstructor(true);
        rootConstructor.addParameter(javaClass.getSimpleName(), "value");
        rootConstructor.addParameter("ModelType", "modelType");
        rootConstructor.setBody("super(value, modelType);");

        getState().getJavaIndex().index(valueImpl);
    }
}
