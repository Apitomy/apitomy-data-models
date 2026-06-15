package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates a cloner factory that knows how to create the correct cloner for
 * a given model type.
 */
public class CreateClonerFactoryStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        String clonerFactoryPackageName = getState().getConfig().getRootNamespace() + ".io";
        String clonerFactoryClassName = "ModelClonerFactory";

        JavaClassSource factoryClassSource = Roaster.create(JavaClassSource.class)
                .setPackage(clonerFactoryPackageName)
                .setName(clonerFactoryClassName)
                .setPublic();

        createClonerFactoryMethod(factoryClassSource);

        getState().getJavaIndex().index(factoryClassSource);
    }

    private void createClonerFactoryMethod(JavaClassSource factoryClassSource) {
        JavaEnumSource modelTypeSource = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        factoryClassSource.addImport(modelTypeSource);
        JavaInterfaceSource modelClonerSource = getState().getJavaIndex().lookupInterface(getModelClonerInterfaceFQN());
        factoryClassSource.addImport(modelClonerSource);

        MethodSource<JavaClassSource> factoryMethodSource = factoryClassSource.addMethod()
                .setName("createModelCloner").setPublic().setStatic(true);
        factoryMethodSource.setReturnType(modelClonerSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");

        BodyBuilder body = new BodyBuilder();
        body.append("ModelCloner cloner = null;");
        body.append("switch (modelType) {");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String clonerPackageName = getClonerPackageName(specVersion);
            String clonerClassName = getClonerClassName(specVersion);
            String clonerFQN = clonerPackageName + "." + clonerClassName;
            String dispatcherClassName = clonerClassName + "Dispatcher";
            String dispatcherFQN = clonerPackageName + "." + dispatcherClassName;

            JavaClassSource clonerSource = getState().getJavaIndex().lookupClass(clonerFQN);
            factoryClassSource.addImport(clonerSource);
            JavaClassSource dispatcherSource = getState().getJavaIndex().lookupClass(dispatcherFQN);
            factoryClassSource.addImport(dispatcherSource);

            String modelTypeValue = prefixToModelType(specVersion.getPrefix());
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("clonerClassName", clonerSource.getName());
            body.addContext("dispatcherClassName", dispatcherSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        cloner = new ${dispatcherClassName}(new ${clonerClassName}());");
            body.append("        break;");
        });
        body.append("}");
        body.append("return cloner;");
        factoryMethodSource.setBody(body.toString());
    }

}
