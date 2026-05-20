package io.apitomy.umg.pipe.java;

import java.util.Collections;

import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.pipe.AbstractStage;

/**
 * Removes any imports that are not needed.  This works around an issue where Roaster
 * will sometimes add imports for classes or interfaces used but in the same package.
 *
 * @author eric.wittmann@gmail.com
 */
public class RemoveUnusedImportsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getJavaIndex().getClasses().values().forEach(javaClass -> {
            removeUnusedImports(javaClass);
        });
        getState().getJavaIndex().getInterfaces().values().forEach(javaInterface -> {
            removeUnusedImports(javaInterface);
        });
    }

    private void removeUnusedImports(JavaSource<?> javaClass) {
        String _package = javaClass.getPackage();
        Collections.unmodifiableList(javaClass.getImports()).stream().filter(_import -> _import.getPackage().equals(_package)).forEach(_unusedImport -> {
            javaClass.removeImport(_unusedImport);
        });
    }
}
