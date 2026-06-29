package io.apitomy.umg.pipe.java.method;

import java.util.Collection;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;

/**
 * Generates the {@code createModelReader(ModelType)} or
 * {@code createModelWriter(ModelType)} factory method — a static switch
 * that instantiates the correct spec-version reader/writer.
 *
 * <p>Parameterised by {@link Mode} so a single class handles both
 * Reader and Writer (they are structurally identical).
 */
public class IOFactoryMethod implements Method {

    /** Whether this method targets the reader or writer factory. */
    public enum Mode {
        READER("Reader", "reader"),
        WRITER("Writer", "writer");

        private final String label;
        private final String varName;

        Mode(String label, String varName) {
            this.label = label;
            this.varName = varName;
        }

        /** e.g. "Reader" or "Writer" */
        public String label() { return label; }

        /** e.g. "reader" or "writer" */
        public String varName() { return varName; }
    }

    private final Mode mode;
    private final Collection<SpecificationVersion> specVersions;
    private final CodeGenContext ctx;

    public IOFactoryMethod(Mode mode, Collection<SpecificationVersion> specVersions,
                           CodeGenContext ctx) {
        this.mode = mode;
        this.specVersions = specVersions;
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return "createModel" + mode.label();
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        JavaClassSource classSource = (JavaClassSource) target;

        JavaEnumSource modelTypeSource = ctx.getJavaIndex().lookupEnum(ctx.getModelTypeEnumFQN());
        classSource.addImport(modelTypeSource);

        String interfaceFQN = mode == Mode.READER
                ? ctx.getModelReaderInterfaceFQN()
                : ctx.getModelWriterInterfaceFQN();
        JavaInterfaceSource ioInterfaceSource = ctx.getJavaIndex().lookupInterface(interfaceFQN);
        classSource.addImport(ioInterfaceSource);

        MethodSource<JavaClassSource> factoryMethodSource = classSource.addMethod()
                .setName(getName()).setPublic().setStatic(true);
        factoryMethodSource.setReturnType(ioInterfaceSource);
        factoryMethodSource.addParameter(modelTypeSource.getName(), "modelType");

        String interfaceName = ioInterfaceSource.getName(); // e.g. "ModelReader"
        String varName = mode.varName();                    // e.g. "reader"

        BodyBuilder body = new BodyBuilder();
        body.addContext("interfaceName", interfaceName);
        body.addContext("varName", varName);
        body.append("${interfaceName} ${varName} = null;");
        body.append("switch (modelType) {");
        for (SpecificationVersion specVersion : specVersions) {
            String ioPackageName = specVersion.getNamespace() + ".io";
            String ioClassName = specVersion.getPrefix() + "Model" + mode.label();
            String ioFQN = ioPackageName + "." + ioClassName;

            JavaClassSource specIOSource = ctx.getJavaIndex().lookupClass(ioFQN);
            classSource.addImport(specIOSource);

            String modelTypeValue = specVersion.getPrefix().toUpperCase();
            body.addContext("modelTypeValue", modelTypeValue);
            body.addContext("ioClassName", specIOSource.getName());

            body.append("    case ${modelTypeValue}:");
            body.append("        ${varName} = new ${ioClassName}();");
            body.append("        break;");
        }
        body.append("}");
        body.append("return ${varName};");
        factoryMethodSource.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added during writeTo
    }
}
