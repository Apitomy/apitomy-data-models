package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.IODispatcherFactoryMethod;
import io.apitomy.umg.pipe.java.method.IOFactoryMethod;

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

        Collection<SpecificationVersion> specVersions =
                getState().getSpecIndex().getAllSpecificationVersions();
        CodeGenContext ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());

        new IOFactoryMethod(IOFactoryMethod.Mode.WRITER, specVersions, ctx)
                .writeTo(writerClassSource);
        new IODispatcherFactoryMethod(IOFactoryMethod.Mode.WRITER, specVersions, ctx)
                .writeTo(writerClassSource);

        getState().getJavaIndex().index(writerClassSource);
    }

}
