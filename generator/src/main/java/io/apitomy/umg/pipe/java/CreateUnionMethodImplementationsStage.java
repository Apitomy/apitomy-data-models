package io.apitomy.umg.pipe.java;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates is/as method implementations on union value classes and entity impl classes.
 * Iterates union types from the type index. For each variant, finds the implementation
 * class and generates the method bodies.
 * <p>
 * Must run AFTER CreateEntityImplementationsStage so entity impl classes exist.
 */
public class CreateUnionMethodImplementationsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findTypes("").stream()
                .filter(t -> t instanceof UnionType)
                .map(t -> (UnionType) t)
                .forEach(this::createMethodImplementations);
    }

    private void createMethodImplementations(UnionType unionType) {
        var nsModel = getState().getConceptIndex().lookupNamespace(unionType.getNamespace());
        var sortedTypes = new ArrayList<>(unionType.getTypes());
        sortedTypes.sort(Comparator.comparing(t -> JavaTypeFactory.getUnionComponentName(t).toLowerCase()));

        for (Type variantType : unionType.getTypes()) {
            List<JavaClassSource> implSources = findImplSources(variantType, unionType);
            for (JavaClassSource implSource : implSources) {
                createMethodsOnImpl(implSource, variantType, sortedTypes, nsModel);
            }
        }
    }

    private List<JavaClassSource> findImplSources(Type variantType, UnionType unionType) {
        List<JavaClassSource> result = new ArrayList<>();

        if (variantType instanceof EntityType entityType) {
            String entityName = entityType.getName();
            getState().getConceptIndex().findEntities("").stream()
                    .filter(e -> e.isLeaf() && e.getName().equals(entityName))
                    .forEach(leafEntity -> {
                        JavaClassSource impl = resolveJavaEntityImpl(leafEntity);
                        if (impl != null) result.add(impl);
                    });
        } else if (variantType instanceof PrimitiveUnionVariantType puv) {
            String typeName = StringUtils.capitalize(puv.getType().name().toLowerCase());
            String implFQN = getUnionTypeFQN(typeName + "UnionValueImpl");
            JavaClassSource impl = getState().getJavaIndex().lookupClass(implFQN);
            if (impl != null) result.add(impl);
        } else if (variantType instanceof ListType || variantType instanceof MapType) {
            String typeName = JavaTypeFactory.getUnionComponentName(variantType);
            String implFQN = getUnionTypeFQN(typeName + "UnionValueImpl");
            JavaClassSource impl = getState().getJavaIndex().lookupClass(implFQN);
            if (impl == null) {
                String pkg = resolveUnionPackage(unionType);
                impl = getState().getJavaIndex().lookupClass(pkg + "." + typeName + "UnionValueImpl");
            }
            if (impl != null) result.add(impl);
        }

        return result;
    }

    private void createMethodsOnImpl(JavaClassSource implSource, Type activeVariant,
                                      List<Type> allVariants, io.apitomy.umg.models.concept.NamespaceModel nsModel) {
        for (Type variantType : allVariants) {
            String typeName = JavaTypeFactory.getUnionComponentName(variantType);
            String isMethodName = "is" + typeName;
            String asMethodName = "as" + typeName;

            if (implSource.hasMethodSignature(isMethodName)) {
                continue;
            }

            var jt = getJavaTypeFactory().createJavaType(variantType, nsModel, true);
            String asMethodReturnType = jt.toJavaTypeString();
            boolean isActive = isSameVariant(activeVariant, variantType);

            // isXxx method
            MethodSource<JavaClassSource> isMethod = implSource.addMethod()
                    .setName(isMethodName).setReturnType(boolean.class).setPublic();
            isMethod.addAnnotation(Override.class);
            isMethod.setBody(isActive ? "return true;" : "return false;");

            // asXxx method
            MethodSource<JavaClassSource> asMethod = implSource.addMethod()
                    .setName(asMethodName).setReturnType(asMethodReturnType).setPublic();
            asMethod.addAnnotation(Override.class);
            jt.addImportsTo(implSource);

            if (isActive) {
                if (activeVariant instanceof EntityType) {
                    asMethod.setBody("return this;");
                } else {
                    asMethod.setBody("return getValue();");
                }
            } else {
                asMethod.setBody("throw new ClassCastException();");
            }
        }

        // Add unionValue() for entity impls
        if (activeVariant instanceof EntityType && !implSource.hasMethodSignature("unionValue")) {
            MethodSource<JavaClassSource> unionValueMethod = implSource.addMethod()
                    .setName("unionValue").setReturnType("Object").setPublic();
            unionValueMethod.addAnnotation(Override.class);
            unionValueMethod.setBody("return this;");
        }
    }

    private boolean isSameVariant(Type a, Type b) {
        return JavaTypeFactory.getUnionComponentName(a).equals(JavaTypeFactory.getUnionComponentName(b));
    }
}
