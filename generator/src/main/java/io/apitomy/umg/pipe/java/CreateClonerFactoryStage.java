package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.pipe.java.method.ClonerFactoryMethod;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

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

        Collection<SpecificationVersion> specVersions =
                getState().getSpecIndex().getAllSpecificationVersions();
        CodeGenContext ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());

        new ClonerFactoryMethod(specVersions, ctx).writeTo(factoryClassSource);

        getState().getJavaIndex().index(factoryClassSource);
    }

}
