package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates a writer factory that knows how to create the correct writer for
 * a given model type.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateWriterFactoryStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        String writerFactoryPackageName = getState().getConfig().getRootNamespace() + ".io";
        String writerFactoryClassName = "ModelWriterFactory";

        // Create java source code for the writer
        JavaClassSource writerClassSource = Roaster.create(JavaClassSource.class)
                .setPackage(writerFactoryPackageName)
                .setName(writerFactoryClassName)
                .setPublic();

        createWriterFactoryMethod(writerClassSource);
        createWriterDispatcherFactoryMethod(writerClassSource);

        getState().getJavaIndex().index(writerClassSource);
    }

    private void createWriterFactoryMethod(JavaClassSource writerClassSource) {
        // Some imports (model type, model writer interface, etc)
        JavaEnumSource modelTypeSource = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        writerClassSource.addImport(modelTypeSource);
        JavaInterfaceSource modelWriterSource = getState().getJavaIndex().lookupInterface(getModelWriterInterfaceFQN());
        writerClassSource.addImport(modelWriterSource);

        // Create the factory method.
        MethodSource<JavaClassSource> factoryMethodSource = writerClassSource.addMethod().setName("createModelWriter").setPublic().setStatic(true);
        factoryMethodSource.setReturnType(modelWriterSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");

        BodyBuilder body = new BodyBuilder();
        body.append("ModelWriter writer = null;");
        body.append("switch (modelType) {");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String specModelWriterFQN = getWriterPackageName(specVersion) + "." + getWriterClassName(specVersion);
            JavaClassSource specModelWriterSource = getState().getJavaIndex().lookupClass(specModelWriterFQN);
            writerClassSource.addImport(specModelWriterSource);

            String modelTypeValue = prefixToModelType(specVersion.getPrefix());
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("modelWriterClassName", specModelWriterSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        writer = new ${modelWriterClassName}();");
            body.append("        break;");
        });
        body.append("}");
        body.append("return writer;");
        factoryMethodSource.setBody(body.toString());
    }

    private void createWriterDispatcherFactoryMethod(JavaClassSource writerClassSource) {
        JavaEnumSource modelTypeSource = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        writerClassSource.addImport(modelTypeSource);
        JavaInterfaceSource rootVisitorInterfaceSource = getState().getJavaIndex().lookupInterface(getRootVisitorInterfaceFQN());
        writerClassSource.addImport(rootVisitorInterfaceSource);

        writerClassSource.addImport(ObjectNode.class);

        MethodSource<JavaClassSource> factoryMethodSource = writerClassSource.addMethod()
                .setName("createModelWriterDispatcher").setPublic().setStatic(true);
        factoryMethodSource.setReturnType(rootVisitorInterfaceSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");
        factoryMethodSource.addParameter(ObjectNode.class.getSimpleName(), "json");

        BodyBuilder body = new BodyBuilder();
        body.addContext("visitorName", rootVisitorInterfaceSource.getName());

        body.append("ModelWriter writer = ModelWriterFactory.createModelWriter(modelType);");
        body.append("${visitorName} visitor = null;");
        body.append("switch (modelType) {");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String writerPackageName = getWriterPackageName(specVersion);
            String writerClassName = getWriterClassName(specVersion);
            String writerFQN = writerPackageName + "." + writerClassName;
            String dispatcherPackageName = writerPackageName;
            String dispatcherClassName = writerClassName + "Dispatcher";
            String dispatcherFQN = dispatcherPackageName + "." + dispatcherClassName;

            JavaClassSource writerSource = getState().getJavaIndex().lookupClass(writerFQN);
            writerClassSource.addImport(writerSource);
            JavaClassSource writerDispatcherSource = getState().getJavaIndex().lookupClass(dispatcherFQN);
            writerClassSource.addImport(writerDispatcherSource);

            String modelTypeValue = prefixToModelType(specVersion.getPrefix());
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("writerClassName", writerSource.getName());
            body.addContext("dispatcherClassName", writerDispatcherSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        visitor = new ${dispatcherClassName}(json, (${writerClassName}) writer);");
            body.append("        break;");
        });
        body.append("}");
        body.append("return visitor;");
        factoryMethodSource.setBody(body.toString());
    }

}
