package io.apitomy.umg.pipe.java;

import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Applies union type interfaces to their variant types.
 * For each union type in the type index, each variant's interface/wrapper
 * is made to extend the union interface.
 */
public class ApplyUnionInterfacesToTypesStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findTypes("").stream()
                .filter(t -> t instanceof UnionType)
                .map(t -> (UnionType) t)
                .forEach(this::applyUnionInterface);
    }

    private void applyUnionInterface(UnionType unionType) {
        String unionName = getUnionName(unionType);
        String unionFQN = getUnionTypeFQN(unionName);
        JavaInterfaceSource unionInterface = getState().getJavaIndex().lookupInterface(unionFQN);
        if (unionInterface == null) {
            warn("Union interface not found: %s", unionFQN);
            return;
        }

        for (Type variantType : unionType.getTypes()) {
            JavaInterfaceSource variantSource = resolveVariantInterface(variantType, unionType);
            if (variantSource == null) {
                warn("Could not resolve variant interface for: %s in union %s", variantType, unionName);
                continue;
            }

            variantSource.addImport(unionInterface);
            variantSource.addInterface(unionInterface);
        }
    }

    private JavaInterfaceSource resolveVariantInterface(Type variantType, UnionType unionType) {
        if (variantType instanceof EntityType entityType) {
            var nsModel = getState().getConceptIndex().lookupNamespace(unionType.getNamespace());
            return resolveCommonJavaEntity(nsModel, entityType.getName());
        } else if (variantType instanceof PrimitiveUnionVariantType puv) {
            String typeName = StringUtils.capitalize(puv.getType().name().toLowerCase());
            return getState().getJavaIndex().lookupInterface(getUnionTypeFQN(typeName + "UnionValue"));
        } else if (variantType instanceof ListType listType) {
            String typeName = JavaTypeFactory.getUnionComponentName(listType);
            return getState().getJavaIndex().lookupInterface(getUnionTypeFQN(typeName + "UnionValue"));
        } else if (variantType instanceof MapType mapType) {
            String typeName = JavaTypeFactory.getUnionComponentName(mapType);
            return getState().getJavaIndex().lookupInterface(getUnionTypeFQN(typeName + "UnionValue"));
        } else if (variantType instanceof UnionType nestedUnion) {
            // Nested union (type alias as variant) — skip, its own variants are handled separately
            return null;
        }
        return null;
    }

    private String getUnionName(UnionType unionType) {
        if (unionType.getAliasName() != null) {
            return unionType.getAliasName();
        }
        var sortedTypes = new ArrayList<>(unionType.getTypes());
        sortedTypes.sort(Comparator.comparing(t -> JavaTypeFactory.getUnionComponentName(t).toLowerCase()));
        return sortedTypes.stream()
                .map(JavaTypeFactory::getUnionComponentName)
                .collect(Collectors.joining()) + "Union";
    }
}
