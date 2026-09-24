package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

/**
 * Compiles this feature's new/shared source files with {@code --release 11}
 * against the project's already-built dependency classpath, to guard against
 * accidentally depending on a newer {@code java.lang}/{@code java.util} API
 * or language feature than the project targets. This is a narrow, explicit
 * check over a fixed file list; it does not change the project's own compiler
 * release, and it must be run under both JDK 17 and JDK 21 (a JDK newer than
 * the CI baseline can accept source that {@code --release 11} would reject
 * when actually enforced against an older release's API signatures -- see the
 * project's JSweet transpilation notes on JDK-version-dependent failures for
 * the same general reason).
 */
class OpenApiJava11SourceTest {

    private static final String[] SOURCE_FILES = {
        "src/main/java/io/apitomy/datamodels/jsonschema/compat/containment/ExactDecimal.java",
        "src/main/java/io/apitomy/datamodels/jsonschema/compat/containment/NumericProvenance.java",
        "src/main/java/io/apitomy/datamodels/jsonschema/compat/containment/PortableSchemaUtil.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/CheckCancellation.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/CheckOptions.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/CheckSession.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ReferenceEdge.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ReferenceGraph.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ReferenceKind.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ReferenceTarget.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ResourceDiscovery.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ResourceDocument.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ResourceIndex.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ResourceProblem.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ResourceRequest.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/ResourceSet.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/AsyncResourceLoader.java",
        "src/main/java/io/apitomy/datamodels/openapi/compat/resource/AsyncResourceAcquirer.java",
    };

    @Test
    void newSourcesCompileUnderJava11Release() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "A JDK (not just a JRE) is required to run this test");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        List<File> sourceFiles = new ArrayList<File>();
        Path moduleRoot = Paths.get("").toAbsolutePath();
        for (int i = 0; i < SOURCE_FILES.length; i++) {
            File file = moduleRoot.resolve(SOURCE_FILES[i]).toFile();
            assertTrue(file.exists(), "Expected source file to exist: " + file);
            sourceFiles.add(file);
        }

        String classpath = System.getProperty("java.class.path");
        String outputDir = System.getProperty("java.io.tmpdir") + File.separator + "openapi-compat-java11-check";
        new File(outputDir).mkdirs();
        List<String> options = Arrays.asList(
                "--release", "11",
                "-classpath", classpath,
                "-d", outputDir,
                "-proc:none");

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
        JavaCompiler.CompilationTask task = compiler.getTask(new StringWriter(), fileManager, diagnostics, options, null,
                compilationUnits);
        boolean success = task.call().booleanValue();

        StringBuilder report = new StringBuilder();
        List<Diagnostic<? extends JavaFileObject>> allDiagnostics = diagnostics.getDiagnostics();
        for (int i = 0; i < allDiagnostics.size(); i++) {
            report.append(allDiagnostics.get(i).toString()).append('\n');
        }
        fileManager.close();
        assertTrue(success, "Compilation with --release 11 failed:\n" + report);
    }
}
