package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;

/**
 * A union type has already been created (as an interface like StringWidgetUnion) and now must be
 * applied.  This means that the impl classes for "String" and "Widget" (from the StringWidgetUnion
 * example) must implement the union type interface.  For primitive types and collection types, we need
 * to update the wrapper/value interfaces to extend the union type interface.  For entity types
 * we update the generated entity interfaces.
 *
 * To continue with the example above, we need to update the "StringUnionValue" and "Widget" interfaces
 * to extend the "StringWidgetUnion" union type.
 */
public class ApplyUnionTypesStage extends AbstractUnionTypeJavaStage {

    @Override
    protected void doProcess(PropertyModelWithOrigin property) {
        applyUnionType(property);
    }

    /**
     * @param property
     */
    private void applyUnionType(PropertyModelWithOrigin property) {
        // Extract the actual union type: for simple unions it's the property type itself,
        // for union maps/lists it's the nested type
        PropertyType actualUnionType = property.getProperty().getType();
        if (isUnionList(property.getProperty()) || isUnionMap(property.getProperty())) {
            actualUnionType = property.getProperty().getType().getNested().iterator().next();
        }
        UnionPropertyType unionType = new UnionPropertyType(actualUnionType);
        String unionTypeFQN = getUnionTypeFQN(unionType.getName());
        JavaInterfaceSource unionTypeSource = getState().getJavaIndex().lookupInterface(unionTypeFQN);

        unionType.getNestedTypes().forEach(nestedType -> {
            JavaType nestedJT = new JavaType(nestedType, property.getOrigin().getNamespace());
            JavaInterfaceSource unionValueSource = null;
            if (nestedJT.isPrimitive() || nestedJT.isPrimitiveList() || nestedJT.isPrimitiveMap()) {
                String typeName = getTypeName(nestedType);
                String unionValueFQN = getUnionTypeFQN(typeName + "UnionValue");
                unionValueSource = getState().getJavaIndex().lookupInterface(unionValueFQN);
            } else if (nestedJT.isEntity()) {
                unionValueSource = resolveJavaEntity(property.getOrigin().getNamespace().fullName(), nestedType.getSimpleType());
            } else if (nestedJT.isEntityList()) {
                String typeName = getTypeName(nestedType);
                String unionValueFQN = getUnionTypeFQN(typeName + "UnionValue");
                unionValueSource = getState().getJavaIndex().lookupInterface(unionValueFQN);
            }
            if (unionValueSource == null) {
                throw new RuntimeException("[ApplyUnionTypesStage] Union type value NOT supported: " + nestedType);
            }

            unionValueSource.addImport(unionTypeSource);
            unionValueSource.addInterface(unionTypeSource);
        });
    }
}
