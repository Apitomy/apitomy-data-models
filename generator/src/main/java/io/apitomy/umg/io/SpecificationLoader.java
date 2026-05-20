package io.apitomy.umg.io;

import java.io.File;
import java.net.URL;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.apitomy.umg.beans.Specification;
import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.logging.Logger;
import io.apitomy.umg.models.spec.SpecificationModel;

public class SpecificationLoader {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Loads a single specification file, parses it into an object, and
     * returns that object.
     * @param specPath
     */
    public static SpecificationModel loadSpec(String specPath) {
        File file = new File(specPath);
        return loadSpec(file);
    }

    /**
     * Loads a single specification file/resource, parses it into an object, and
     * returns that object.
     * @param specPath
     */
    public static SpecificationModel loadSpec(String specPath, ClassLoader cl) {
        URL resource = cl.getResource(specPath);
        if (resource == null) {
            throw new NullPointerException("Specification not found: " + specPath);
        }
        return loadSpec(resource);
    }

    /**
     * Loads a single specification resource, parses it into an object, and
     * returns that object.
     * @param specFile
     */
    public static SpecificationModel loadSpec(File specFile) {
        try {
            if (!specFile.isFile()) {
                throw new RuntimeException("Invalid specification file (not a file): " + specFile);
            }
            return loadSpec(specFile.toURI().toURL());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Loads a single specification resource, parses it into an object, and
     * returns that object.
     * @param specURL
     */
    public static SpecificationModel loadSpec(URL specURL) {
        Logger.info("Loading specification from: %s", specURL);
        try {
            Specification spec = mapper.readValue(specURL, Specification.class);
            SpecificationModel model = SpecificationModel.from(spec);
            loadVersions(specURL, spec, model);
            return model;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Loads the versions referenced from the specification file.
     * @param specURL
     * @param spec
     * @param model
     */
    private static void loadVersions(URL specURL, Specification spec, SpecificationModel model) {
        model.getVersions().addAll(
                spec.getVersions().stream().map(versionRef -> {
                    try {
                        String ref = versionRef.get$ref();
                        URL specVersionUrl;
                        if (ref.startsWith(".")) {
                            specVersionUrl = specURL.toURI().resolve(ref).toURL();
                        } else {
                            specVersionUrl = new URL(ref);
                        }
                        return loadSpecVersion(specVersionUrl);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList())
                );
    }

    private static SpecificationVersion loadSpecVersion(URL specVersionURL) {
        Logger.info("Loading specification version from: %s", specVersionURL);
        try {
            return mapper.readValue(specVersionURL, SpecificationVersion.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
