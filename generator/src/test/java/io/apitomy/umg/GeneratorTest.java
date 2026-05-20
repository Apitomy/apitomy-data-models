package io.apitomy.umg;

import io.apitomy.umg.io.SpecificationLoader;
import io.apitomy.umg.models.spec.SpecificationModel;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class GeneratorTest {

    @Test
    public void testGenerator_OpenApi() throws Exception {
        File outputDir;

        String outputDirPath = System.getenv("GENERATE_TEST_OUTPUT_DIR");
        if (outputDirPath != null) {
            outputDir = new File(new File(outputDirPath), "testGenerator_OpenApi");
            outputDir.mkdirs();

            System.out.println("[OpenAPI] Output directory: " + outputDir);
        } else {
            outputDir = Files.createTempDirectory(GeneratorTest.class.getSimpleName()).toFile();
        }

        File umgTestOutputDir = Files.createTempDirectory(GeneratorTest.class.getSimpleName() + "-test").toFile();
        UnifiedModelGeneratorConfig config = UnifiedModelGeneratorConfig.builder()
                .outputDirectory(outputDir)
                .testOutputDirectory(umgTestOutputDir)
                .generateTestFixtures(false)
                .rootNamespace("io.apitomy.umg.test").build();
        // Load the specs
        List<SpecificationModel> specs = List.of(
                SpecificationLoader.loadSpec(GeneratorTest.class.getResource("openapi.yaml"))
        );
        // Create a unified model generator
        UnifiedModelGenerator generator = new UnifiedModelGenerator(config, specs);
        // Generate the source code into the target output directory.
        try {
            generator.generate();
        } finally {
            if (outputDirPath == null) {
                FileUtils.deleteDirectory(outputDir);
                FileUtils.deleteDirectory(umgTestOutputDir);
            }
        }
    }

    @Test
    public void testGenerator_ParentTrait() throws Exception {
        File outputDir;

        String outputDirPath = System.getenv("GENERATE_TEST_OUTPUT_DIR");
        if (outputDirPath != null) {
            outputDir = new File(new File(outputDirPath), "testGenerator_ParentTrait");
            outputDir.mkdirs();

            System.out.println("[Parent Trait] Output directory: " + outputDir);
        } else {
            outputDir = Files.createTempDirectory(GeneratorTest.class.getSimpleName()).toFile();
        }

        File umgTestOutputDir = Files.createTempDirectory(GeneratorTest.class.getSimpleName() + "-test").toFile();
        UnifiedModelGeneratorConfig config = UnifiedModelGeneratorConfig.builder()
                .outputDirectory(outputDir)
                .testOutputDirectory(umgTestOutputDir)
                .generateTestFixtures(false)
                .rootNamespace("io.apitomy.umg.test").build();
        // Load the specs
        List<SpecificationModel> specs = List.of(
                SpecificationLoader.loadSpec(GeneratorTest.class.getResource("parent-trait-spec.yaml"))
        );
        // Create a unified model generator
        UnifiedModelGenerator generator = new UnifiedModelGenerator(config, specs);
        // Generate the source code into the target output directory.
        try {
            generator.generate();
        } finally {
            if (outputDirPath == null) {
                FileUtils.deleteDirectory(outputDir);
                FileUtils.deleteDirectory(umgTestOutputDir);
            }
        }
    }

}