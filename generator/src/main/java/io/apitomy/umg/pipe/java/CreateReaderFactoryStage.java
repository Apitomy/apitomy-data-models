package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.IODispatcherFactoryMethod;
import io.apitomy.umg.pipe.java.method.IOFactoryMethod;

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

        Collection<SpecificationVersion> specVersions =
                getState().getSpecIndex().getAllSpecificationVersions();
        CodeGenContext ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());

        new IOFactoryMethod(IOFactoryMethod.Mode.READER, specVersions, ctx)
                .writeTo(readerClassSource);
        new IODispatcherFactoryMethod(IOFactoryMethod.Mode.READER, specVersions, ctx)
                .writeTo(readerClassSource);

        getState().getJavaIndex().index(readerClassSource);
    }

}
