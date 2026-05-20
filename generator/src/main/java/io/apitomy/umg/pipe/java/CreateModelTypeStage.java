package io.apitomy.umg.pipe.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaEnumSource;

/**
 * Generates an enum with an entry for each spec version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateModelTypeStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        String _package = getState().getConfig().getRootNamespace();
        String name = "ModelType";

        JavaEnumSource modelTypeEnum = Roaster.create(JavaEnumSource.class)
                .setPackage(_package)
                .setName(name)
                .setPublic();

        getState().getSpecIndex().getAllSpecificationVersions().stream().map(specVersion -> {
            return prefixToModelType(specVersion.getPrefix());
        }).sorted().forEach(specType -> {
            modelTypeEnum.addEnumConstant(specType);
        });

        getState().getJavaIndex().index(modelTypeEnum);
    }

}
