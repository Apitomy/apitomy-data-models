package io.apitomy.umg.pipe.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.pipe.AbstractStage;

public class JavaWriteStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getJavaIndex().getClasses().values().forEach(_class -> {
            writeToFile(_class, getState().getConfig().getOutputDirectory());
        });
        getState().getJavaIndex().getInterfaces().values().forEach(_interface -> {
            writeToFile(_interface, getState().getConfig().getOutputDirectory());
        });
        getState().getJavaIndex().getEnums().values().forEach(_enum -> {
            writeToFile(_enum, getState().getConfig().getOutputDirectory());
        });
    }

    /**
     * Writes the given class out to a file.
     *
     * @param javaSource
     * @param outputDirectory
     */
    private void writeToFile(JavaSource<?> javaSource, File outputDirectory) {
        String pkg = javaSource.getPackage();
        String fpath = pkg.replace(".", "/");
        File dir = new File(outputDirectory, fpath);
        dir.mkdirs();
        File file = new File(dir, javaSource.getName() + ".java");
        try (PrintWriter writer = new PrintWriter(file)) {
            // TODO use Roaster's 'Formatter' with the option of custom formatting options to be more controlled here
            writer.write(javaSource.toString());
            writer.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
