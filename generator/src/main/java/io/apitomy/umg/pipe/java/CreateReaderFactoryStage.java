package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates a reader factory that knows how to create the correct reader for
 * a given model type.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateReaderFactoryStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        String readerFactoryPackageName = getState().getConfig().getRootNamespace() + ".io";
        String readerFactoryClassName = "ModelReaderFactory";

        // Create java source code for the reader
        JavaClassSource readerClassSource = Roaster.create(JavaClassSource.class)
                .setPackage(readerFactoryPackageName)
                .setName(readerFactoryClassName)
                .setPublic();

        createReaderFactoryMethod(readerClassSource);
        createReaderDispatcherFactoryMethod(readerClassSource);

        getState().getJavaIndex().index(readerClassSource);
    }

    private void createReaderFactoryMethod(JavaClassSource readerClassSource) {
        JavaEnumSource modelTypeSource = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        readerClassSource.addImport(modelTypeSource);
        JavaInterfaceSource modelReaderSource = getState().getJavaIndex().lookupInterface(getModelReaderInterfaceFQN());
        readerClassSource.addImport(modelReaderSource);

        MethodSource<JavaClassSource> factoryMethodSource = readerClassSource.addMethod()
                .setName("createModelReader").setPublic().setStatic(true);
        factoryMethodSource.setReturnType(modelReaderSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");

        BodyBuilder body = new BodyBuilder();
        body.append("ModelReader reader = null;");
        body.append("switch (modelType) {");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String specModelReaderFQN = getReaderPackageName(specVersion) + "." + getReaderClassName(specVersion);
            JavaClassSource specModelReaderSource = getState().getJavaIndex().lookupClass(specModelReaderFQN);
            readerClassSource.addImport(specModelReaderSource);

            String modelTypeValue = prefixToModelType(specVersion.getPrefix());
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("modelReaderClassName", specModelReaderSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        reader = new ${modelReaderClassName}();");
            body.append("        break;");
        });
        body.append("}");
        body.append("return reader;");
        factoryMethodSource.setBody(body.toString());
    }

    private void createReaderDispatcherFactoryMethod(JavaClassSource readerClassSource) {
        JavaEnumSource modelTypeSource = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        readerClassSource.addImport(modelTypeSource);
        JavaInterfaceSource rootVisitorInterfaceSource = getState().getJavaIndex().lookupInterface(getRootVisitorInterfaceFQN());
        readerClassSource.addImport(rootVisitorInterfaceSource);

        readerClassSource.addImport(ObjectNode.class);

        MethodSource<JavaClassSource> factoryMethodSource = readerClassSource.addMethod()
                .setName("createModelReaderDispatcher").setPublic().setStatic(true);
        factoryMethodSource.setReturnType(rootVisitorInterfaceSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");
        factoryMethodSource.addParameter(ObjectNode.class.getSimpleName(), "json");

        BodyBuilder body = new BodyBuilder();
        body.addContext("visitorName", rootVisitorInterfaceSource.getName());

        body.append("ModelReader reader = ModelReaderFactory.createModelReader(modelType);");
        body.append("${visitorName} visitor = null;");
        body.append("switch (modelType) {");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String readerPackageName = getReaderPackageName(specVersion);
            String readerClassName = getReaderClassName(specVersion);
            String readerFQN = readerPackageName + "." + readerClassName;
            String dispatcherPackageName = readerPackageName;
            String dispatcherClassName = readerClassName + "Dispatcher";
            String dispatcherFQN = dispatcherPackageName + "." + dispatcherClassName;

            JavaClassSource readerSource = getState().getJavaIndex().lookupClass(readerFQN);
            readerClassSource.addImport(readerSource);
            JavaClassSource readerDispatcherSource = getState().getJavaIndex().lookupClass(dispatcherFQN);
            readerClassSource.addImport(readerDispatcherSource);

            String modelTypeValue = prefixToModelType(specVersion.getPrefix());
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("readerClassName", readerSource.getName());
            body.addContext("dispatcherClassName", readerDispatcherSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        visitor = new ${dispatcherClassName}(json, (${readerClassName}) reader);");
            body.append("        break;");
        });
        body.append("}");
        body.append("return visitor;");
        factoryMethodSource.setBody(body.toString());
    }

}
