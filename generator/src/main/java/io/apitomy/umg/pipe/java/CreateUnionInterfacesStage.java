package io.apitomy.umg.pipe.java;

import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.java.method.UnionAsMethod;
import io.apitomy.umg.pipe.java.method.UnionIsMethod;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Creates union type interfaces by iterating the type index.
 * Each indexed {@link UnionType} gets a Java interface with is/as methods
 * for each variant. Root unions extend {@code RootCapable}.
 */
public class CreateUnionInterfacesStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findTypes("").stream()
                .filter(t -> t instanceof UnionType)
                .map(t -> (UnionType) t)
                .forEach(this::createUnionInterface);
    }

    private void createUnionInterface(UnionType unionType) {
        String name = getUnionName(unionType);
        String _package = resolveUnionPackage(unionType);
        String fqn = _package + "." + name;

        if (getState().getJavaIndex().lookupInterface(fqn) != null) {
            return;
        }

        debug("Creating union interface: %s", name);

        JavaInterfaceSource unionInterface = Roaster.create(JavaInterfaceSource.class)
                .setPackage(_package)
                .setName(name)
                .setPublic();

        // Extend RootCapable if this is a root union
        if (unionType.isRoot()) {
            String rootCapableFQN = getRootNodeInterfaceFQN();
            JavaInterfaceSource rootCapableSource = getState().getJavaIndex().lookupInterface(rootCapableFQN);
            unionInterface.addImport(rootCapableSource);
            unionInterface.addInterface(rootCapableSource);
        }

        // Extend the base Union interface
        String unionFQN = getUnionInterfaceFQN();
        JavaInterfaceSource unionValueSource = getState().getJavaIndex().lookupInterface(unionFQN);
        unionInterface.addImport(unionValueSource);
        unionInterface.addInterface(unionValueSource);

        // Add is/as methods for each variant
        var nsModel = getState().getConceptIndex().lookupNamespace(unionType.getNamespace());
        var sortedTypes = new ArrayList<>(unionType.getTypes());
        sortedTypes.sort(Comparator.comparing(t -> JavaTypeFactory.getUnionComponentName(t).toLowerCase()));

        for (Type variantType : sortedTypes) {
            String typeName = JavaTypeFactory.getUnionComponentName(variantType);
            var jt = getJavaTypeFactory().createJavaType(variantType, nsModel, true);

            unionInterface.addMethod().setName(new UnionIsMethod(typeName).getName()).setReturnType(boolean.class).setPublic();
            unionInterface.addMethod().setName(new UnionAsMethod(typeName).getName()).setReturnType(jt.toJavaTypeString()).setPublic();
            jt.addImportsTo(unionInterface);
        }

        getState().getJavaIndex().index(unionInterface);
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
