package io.apitomy.umg;

import io.apitomy.umg.io.SpecificationLoader;
import io.apitomy.umg.models.spec.SpecificationModel;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Snapshot test: generates code from the synthetic spec and compares
 * every file byte-for-byte against committed expected output.
 * <p>
 * To update expected output after intentional changes:
 * <ol>
 *   <li>Delete {@code generator/src/test/resources/io/apitomy/umg/synthetic/expected/}</li>
 *   <li>Run this test — it will generate and save the expected output</li>
 *   <li>Review and commit the new expected files</li>
 * </ol>
 */
public class SyntheticSnapshotTest {

    private static final String EXPECTED_RESOURCE = "synthetic/expected";
    private static final String EXPECTED_SOURCE_DIR = "src/test/resources/io/apitomy/umg/synthetic/expected";

    @Test
    public void testSyntheticSnapshot() throws Exception {
        var outputDir = Files.createTempDirectory("umg-synthetic").toFile();
        var testOutputDir = Files.createTempDirectory("umg-synthetic-test").toFile();

        try {
            generate(outputDir, testOutputDir);

            // Use classpath to find expected dir location, then resolve in source tree
            var expectedUrl = SyntheticSnapshotTest.class.getResource(EXPECTED_RESOURCE);
            File expectedDir;
            if (expectedUrl != null) {
                expectedDir = new File(expectedUrl.toURI());
            } else {
                expectedDir = findSourceDir();
                // First run — save generated output as expected
                FileUtils.copyDirectory(outputDir, expectedDir);
                System.out.println("[Synthetic] Expected output created at " + expectedDir.getAbsolutePath());
                System.out.println("[Synthetic] Review and commit the expected files.");
                return;
            }

            // Compare all generated files against expected
            var failures = compareDirectories(expectedDir.toPath(), outputDir.toPath());
            if (!failures.isEmpty()) {
                var sb = new StringBuilder("Synthetic snapshot test failed with " + failures.size() + " difference(s):\n");
                for (var failure : failures) {
                    sb.append("  ").append(failure).append("\n");
                }
                sb.append("\nTo update, delete ").append(EXPECTED_SOURCE_DIR).append("/ and re-run.");
                fail(sb.toString());
            }

        } finally {
            FileUtils.deleteDirectory(outputDir);
            FileUtils.deleteDirectory(testOutputDir);
        }
    }

    private void generate(File outputDir, File testOutputDir) throws Exception {
        var config = UnifiedModelGeneratorConfig.builder()
                .outputDirectory(outputDir)
                .testOutputDirectory(testOutputDir)
                .generateTestFixtures(false)
                .rootNamespace("io.test.synthetic")
                .build();

        List<SpecificationModel> specs = List.of(
                SpecificationLoader.loadSpec(
                        SyntheticSnapshotTest.class.getResource("synthetic/synthetic.yaml"))
        );

        new UnifiedModelGenerator(config, specs).generate();
    }

    private List<String> compareDirectories(Path expectedDir, Path actualDir) throws IOException {
        var failures = new ArrayList<String>();

        // Check all expected files exist and match
        try (Stream<Path> walk = Files.walk(expectedDir)) {
            walk.filter(Files::isRegularFile).forEach(expectedFile -> {
                var relative = expectedDir.relativize(expectedFile);
                var actualFile = actualDir.resolve(relative);

                if (!Files.exists(actualFile)) {
                    failures.add("MISSING: " + relative);
                    return;
                }

                try {
                    var expectedContent = Files.readString(expectedFile, StandardCharsets.UTF_8);
                    var actualContent = Files.readString(actualFile, StandardCharsets.UTF_8);
                    if (!expectedContent.equals(actualContent)) {
                        failures.add("CHANGED: " + relative);
                        if (failures.size() == 1) {
                            // Print first diff for debugging
                            var expectedLines = expectedContent.lines().toList();
                            var actualLines = actualContent.lines().toList();
                            for (int i = 0; i < Math.min(expectedLines.size(), actualLines.size()); i++) {
                                if (!expectedLines.get(i).equals(actualLines.get(i))) {
                                    failures.add("  First diff at line " + (i + 1) + ":");
                                    failures.add("  Expected: " + expectedLines.get(i));
                                    failures.add("  Actual:   " + actualLines.get(i));
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    failures.add("ERROR reading " + relative + ": " + e.getMessage());
                }
            });
        }

        // Check for unexpected new files
        try (Stream<Path> walk = Files.walk(actualDir)) {
            walk.filter(Files::isRegularFile).forEach(actualFile -> {
                var relative = actualDir.relativize(actualFile);
                var expectedFile = expectedDir.resolve(relative);
                if (!Files.exists(expectedFile)) {
                    failures.add("NEW: " + relative);
                }
            });
        }

        return failures;
    }

    private File findSourceDir() {
        // Walk up from CWD to find the generator module root
        var candidates = List.of(
                new File(EXPECTED_SOURCE_DIR),
                new File("generator/" + EXPECTED_SOURCE_DIR)
        );
        for (var candidate : candidates) {
            if (candidate.getParentFile().exists()) {
                return candidate;
            }
        }
        return new File(EXPECTED_SOURCE_DIR);
    }
}
