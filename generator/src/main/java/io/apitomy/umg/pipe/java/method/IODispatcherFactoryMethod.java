package io.apitomy.umg.pipe.java.method;

import java.util.Collection;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;

/**
 * Generates the {@code createModelReaderDispatcher(ModelType, ObjectNode)} or
 * {@code createModelWriterDispatcher(ModelType, ObjectNode)} factory method
 * — a static switch that creates the correct dispatcher for a given spec
 * version.
 *
 * <p>Parameterised by {@link IOFactoryMethod.Mode} so a single class handles
 * both Reader and Writer dispatchers (they are structurally identical).
 */
public class IODispatcherFactoryMethod implements Method {

    private final IOFactoryMethod.Mode mode;
    private final Collection<SpecificationVersion> specVersions;
    private final CodeGenContext ctx;

    public IODispatcherFactoryMethod(IOFactoryMethod.Mode mode,
                                     Collection<SpecificationVersion> specVersions,
                                     CodeGenContext ctx) {
        this.mode = mode;
        this.specVersions = specVersions;
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return "createModel" + mode.label() + "Dispatcher";
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        JavaClassSource classSource = (JavaClassSource) target;

        JavaEnumSource modelTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getModelTypeEnumFQN());
        classSource.addImport(modelTypeSource);

        JavaInterfaceSource rootVisitorInterfaceSource =
                ctx.getJavaIndex().lookupInterface(ctx.getRootVisitorInterfaceFQN());
        classSource.addImport(rootVisitorInterfaceSource);

        classSource.addImport(ObjectNode.class);

        MethodSource<JavaClassSource> factoryMethodSource = classSource.addMethod()
                .setName(getName()).setPublic().setStatic(true);
        factoryMethodSource.setReturnType(rootVisitorInterfaceSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");
        factoryMethodSource.addParameter(ObjectNode.class.getSimpleName(), "json");

        String varName = mode.varName();         // "reader" or "writer"
        String label = mode.label();             // "Reader" or "Writer"
        String visitorName = rootVisitorInterfaceSource.getName();

        BodyBuilder body = new BodyBuilder();
        body.addContext("visitorName", visitorName);
        body.addContext("interfaceName", "Model" + label);
        body.addContext("factoryClassName", "Model" + label + "Factory");
        body.addContext("createMethodName", "createModel" + label);
        body.addContext("varName", varName);

        body.append("${interfaceName} ${varName} = ${factoryClassName}.${createMethodName}(modelType);");
        body.append("${visitorName} visitor = null;");
        body.append("switch (modelType) {");
        for (SpecificationVersion specVersion : specVersions) {
            String ioPackageName = specVersion.getNamespace() + ".io";
            String ioClassName = specVersion.getPrefix() + "Model" + label;
            String ioFQN = ioPackageName + "." + ioClassName;
            String dispatcherClassName = ioClassName + "Dispatcher";
            String dispatcherFQN = ioPackageName + "." + dispatcherClassName;

            JavaClassSource ioSource = ctx.getJavaIndex().lookupClass(ioFQN);
            classSource.addImport(ioSource);
            JavaClassSource dispatcherSource = ctx.getJavaIndex().lookupClass(dispatcherFQN);
            classSource.addImport(dispatcherSource);

            String modelTypeValue = specVersion.getPrefix().toUpperCase();
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("ioClassName", ioSource.getName());
            body.addContext("dispatcherClassName", dispatcherSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        visitor = new ${dispatcherClassName}(json, (${ioClassName}) ${varName});");
            body.append("        break;");
        }
        body.append("}");
        body.append("return visitor;");
        factoryMethodSource.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added during writeTo
    }
}
