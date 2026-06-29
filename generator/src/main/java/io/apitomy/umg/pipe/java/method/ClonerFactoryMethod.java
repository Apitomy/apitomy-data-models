package io.apitomy.umg.pipe.java.method;

import java.util.Collection;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;

/**
 * Generates the {@code createModelCloner(ModelType)} factory method — a
 * static switch that instantiates the correct spec-version cloner wrapped
 * in its dispatcher.
 *
 * <p>The cloner factory differs from reader/writer because the switch
 * body creates {@code new ClonerDispatcher(new Cloner())} rather than a
 * plain {@code new Cloner()}.
 */
public class ClonerFactoryMethod implements Method {

    private final Collection<SpecificationVersion> specVersions;
    private final CodeGenContext ctx;

    public ClonerFactoryMethod(Collection<SpecificationVersion> specVersions,
                               CodeGenContext ctx) {
        this.specVersions = specVersions;
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return "createModelCloner";
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        JavaClassSource classSource = (JavaClassSource) target;

        JavaEnumSource modelTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getModelTypeEnumFQN());
        classSource.addImport(modelTypeSource);
        JavaInterfaceSource modelClonerSource =
                ctx.getJavaIndex().lookupInterface(ctx.getModelClonerInterfaceFQN());
        classSource.addImport(modelClonerSource);

        MethodSource<JavaClassSource> factoryMethodSource = classSource.addMethod()
                .setName(getName()).setPublic().setStatic(true);
        factoryMethodSource.setReturnType(modelClonerSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");

        BodyBuilder body = new BodyBuilder();
        body.append("ModelCloner cloner = null;");
        body.append("switch (modelType) {");
        for (SpecificationVersion specVersion : specVersions) {
            String clonerPackageName = specVersion.getNamespace() + ".io";
            String clonerClassName = specVersion.getPrefix() + "ModelCloner";
            String clonerFQN = clonerPackageName + "." + clonerClassName;
            String dispatcherClassName = clonerClassName + "Dispatcher";
            String dispatcherFQN = clonerPackageName + "." + dispatcherClassName;

            JavaClassSource clonerSource = ctx.getJavaIndex().lookupClass(clonerFQN);
            classSource.addImport(clonerSource);
            JavaClassSource dispatcherSource = ctx.getJavaIndex().lookupClass(dispatcherFQN);
            classSource.addImport(dispatcherSource);

            String modelTypeValue = specVersion.getPrefix().toUpperCase();
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("clonerClassName", clonerSource.getName());
            body.addContext("dispatcherClassName", dispatcherSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        cloner = new ${dispatcherClassName}(new ${clonerClassName}());");
            body.append("        break;");
        }
        body.append("}");
        body.append("return cloner;");
        factoryMethodSource.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added during writeTo
    }
}
