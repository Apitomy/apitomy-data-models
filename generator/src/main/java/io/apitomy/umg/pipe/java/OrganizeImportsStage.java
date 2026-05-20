package io.apitomy.umg.pipe.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.pipe.AbstractStage;

/**
 * Removes any imports that are not needed.  This works around an issue where Roaster
 * will sometimes add imports for classes or interfaces used but in the same package.
 *
 * @author eric.wittmann@gmail.com
 */
public class OrganizeImportsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getJavaIndex().getClasses().values().forEach(javaClass -> {
            organizeImports(javaClass);
        });
        getState().getJavaIndex().getInterfaces().values().forEach(javaInterface -> {
            organizeImports(javaInterface);
        });
    }

    private void organizeImports(JavaSource<?> javaClass) {
        List<Import> imports = new ArrayList<>(javaClass.getImports());
        imports.sort(new Comparator<Import>() {
            @Override
            public int compare(Import o1, Import o2) {
                return o1.getQualifiedName().compareTo(o2.getQualifiedName());
            }
        });
        imports.forEach(_import -> {
            javaClass.removeImport(_import);
        });
        imports.forEach(_import -> {
            javaClass.addImport(_import);
        });
    }
}
