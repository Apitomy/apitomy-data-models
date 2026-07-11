package io.apitomy.umg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import io.apitomy.umg.io.SpecificationLoader;
import io.apitomy.umg.models.spec.SpecificationModel;

public class FullGeneratorTest {

    @Test
    public void testGenerator_Full() throws Exception {
        File outputDir;

        String outputDirPath = System.getenv("GENERATE_TEST_OUTPUT_DIR");
        if (outputDirPath != null) {
            outputDir = new File(new File(outputDirPath), "testGenerator_Full");
            outputDir.mkdirs();

            System.out.println("[Full] Output directory: " + outputDir);
        } else {
            outputDir = Files.createTempDirectory(FullGeneratorTest.class.getSimpleName()).toFile();
        }

        File umgTestOutputDir = Files.createTempDirectory(FullGeneratorTest.class.getSimpleName() + "-test").toFile();
        UnifiedModelGeneratorConfig config = UnifiedModelGeneratorConfig.builder()
                .outputDirectory(outputDir)
                .testOutputDirectory(umgTestOutputDir)
                .generateTestFixtures(false)
                .rootNamespace("io.apitomy.datamodels.models").build();
        // Load the specs
        List<SpecificationModel> specs = List.of(
                SpecificationLoader.loadSpec(FullGeneratorTest.class.getResource("full/openapi.yaml")),
                SpecificationLoader.loadSpec(FullGeneratorTest.class.getResource("full/asyncapi.yaml")),
                SpecificationLoader.loadSpec(FullGeneratorTest.class.getResource("full/json-schema.yaml")),
                SpecificationLoader.loadSpec(FullGeneratorTest.class.getResource("full/openrpc.yaml"))
        );
        // Create a unified model generator
        UnifiedModelGenerator generator = new UnifiedModelGenerator(config, specs);
        // Generate the source code into the target output directory.
        try {
            generator.generate();

            // Validation 1: Assert files were generated
            assertOutputNotEmpty(outputDir);

            // Validation 2: Assert expected Java source files exist
            assertExpectedFilesExist(outputDir);

            // Validation 3: Compile the generated code
            assertGeneratedCodeCompiles(outputDir);

            // Validation 4: Snapshot manifest comparison
            assertManifestMatchesExpected(outputDir);
        } finally {
            if (outputDirPath == null) {
                FileUtils.deleteDirectory(outputDir);
                FileUtils.deleteDirectory(umgTestOutputDir);
            }
        }
    }

    /**
     * Validation 1: Verify the output directory is not empty and contains .java files.
     */
    private void assertOutputNotEmpty(File outputDir) throws IOException {
        File[] topLevelFiles = outputDir.listFiles();
        assertNotNull("Output directory should exist and be listable", topLevelFiles);
        assertTrue("Output directory should not be empty", topLevelFiles.length > 0);

        List<Path> javaFiles = collectJavaFiles(outputDir);
        assertFalse("Expected generated .java files but found none", javaFiles.isEmpty());
        System.out.println("[Full] Generated " + javaFiles.size() + " .java files");
    }

    /**
     * Validation 2: Spot-check that key interfaces/classes exist for each spec.
     */
    private void assertExpectedFilesExist(File outputDir) {
        // OpenAPI
        assertFileExists(outputDir, "OpenApiDocument.java",
                "Expected OpenApiDocument.java to be generated");

        // AsyncAPI
        assertFileExists(outputDir, "AsyncApiDocument.java",
                "Expected AsyncApiDocument.java to be generated");

        // OpenRPC
        assertFileExists(outputDir, "OpenRpcDocument.java",
                "Expected OpenRpcDocument.java to be generated");
    }

    /**
     * Validation 3: Compile all generated .java files using the system Java compiler.
     */
    private void assertGeneratedCodeCompiles(File outputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("Java compiler not available (requires JDK, not JRE)", compiler);

        List<Path> javaFiles = collectJavaFiles(outputDir);
        assertFalse("No .java files to compile", javaFiles.isEmpty());

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null,
                StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(javaFiles);

            List<String> options = List.of(
                    "-d", Files.createTempDirectory("umg-compiled").toString(),
                    "--release", "17"
            );

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null,
                    compilationUnits);
            boolean success = task.call();

            if (!success) {
                StringBuilder errors = new StringBuilder("Generated code failed to compile:\n");
                diagnostics.getDiagnostics().forEach(d -> {
                    errors.append("  ").append(d.getKind()).append(": ")
                            .append(d.getMessage(null));
                    if (d.getSource() != null) {
                        errors.append(" (").append(d.getSource().getName())
                                .append(":").append(d.getLineNumber()).append(")");
                    }
                    errors.append("\n");
                });
                fail(errors.toString());
            }
        }
        System.out.println("[Full] All " + javaFiles.size() + " generated files compiled successfully");
    }

    /**
     * Validation 4: Compare the sorted list of generated file paths against a golden manifest.
     * If no manifest file exists yet, it is created automatically (the test still passes
     * on first run so the manifest can be committed).
     */
    private void assertManifestMatchesExpected(File outputDir) throws IOException {
        List<String> actualManifest = buildManifest(outputDir);

        File manifestFile = new File("src/test/resources/io/apitomy/umg/full/expected-manifest.txt");
        if (!manifestFile.exists()) {
            // First run — write the manifest so it can be reviewed and committed.
            manifestFile.getParentFile().mkdirs();
            Files.write(manifestFile.toPath(), actualManifest, StandardCharsets.UTF_8);
            System.out.println("[Full] Manifest file created at " + manifestFile.getAbsolutePath()
                    + " — review and commit it.");
            return;
        }

        List<String> expectedManifest = Files.readAllLines(manifestFile.toPath(), StandardCharsets.UTF_8);
        List<String> missing = new ArrayList<>(expectedManifest);
        missing.removeAll(actualManifest);

        List<String> unexpected = new ArrayList<>(actualManifest);
        unexpected.removeAll(expectedManifest);

        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            StringBuilder sb = new StringBuilder("Generated file manifest does not match expected:\n");
            if (!missing.isEmpty()) {
                sb.append("  Missing files (expected but not generated):\n");
                missing.forEach(f -> sb.append("    - ").append(f).append("\n"));
            }
            if (!unexpected.isEmpty()) {
                sb.append("  Unexpected files (generated but not in manifest):\n");
                unexpected.forEach(f -> sb.append("    + ").append(f).append("\n"));
            }
            sb.append("If these changes are intentional, delete the manifest file and re-run the test.");
            fail(sb.toString());
        }

        System.out.println("[Full] Manifest matches: " + actualManifest.size() + " files verified");
    }

    /**
     * Collects all .java file paths under the given directory.
     */
    private List<Path> collectJavaFiles(File dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir.toPath())) {
            return walk.filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Recursively searches for a file with the given name under the directory.
     */
    private Optional<Path> findFile(File dir, String fileName) {
        try (Stream<Path> walk = Files.walk(dir.toPath())) {
            return walk.filter(p -> p.getFileName().toString().equals(fileName))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Asserts that a file with the given name exists somewhere under the directory.
     */
    private void assertFileExists(File dir, String fileName, String message) {
        Optional<Path> found = findFile(dir, fileName);
        assertTrue(message + " (looked for " + fileName + ")", found.isPresent());
    }

    /**
     * Builds a sorted manifest of relative file paths from the output directory.
     */
    private List<String> buildManifest(File outputDir) throws IOException {
        Path root = outputDir.toPath();
        try (Stream<Path> walk = Files.walk(root)) {
            List<String> manifest = walk
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(p -> root.relativize(p).toString().replace(File.separatorChar, '/'))
                    .sorted()
                    .collect(Collectors.toList());
            return manifest;
        }
    }

}
