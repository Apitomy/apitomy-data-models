/*
 * Copyright 2022 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.datamodels._build;

import java.io.File;
import java.io.FileWriter;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author eric.wittmann@gmail.com
 */
public class GenerateCoreTs {

    private static String rootDir;

    public static void main(String[] args) throws Exception {
        File srcDir = null;
        if (args.length < 1) {
            throw new RuntimeException("Missing argument: srcDir");
        }
        srcDir = new File(args[0]);
        if (!srcDir.isDirectory()) {
            throw new RuntimeException("Invalid argument: srcDir must be a directory");
        }

        rootDir = srcDir.getAbsolutePath();
        Set<String> tsPaths = findTsPaths(srcDir);

        File outputFile = new File(srcDir, "core.ts");
        if (outputFile.exists() && outputFile.isFile()) {
            outputFile.delete();
        }

        StringBuilder builder = new StringBuilder();
        tsPaths.forEach(path -> {
            builder.append("export * from \"");
            builder.append(path);
            builder.append("\";");
            builder.append("\n");
        });

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(builder.toString());
        }

        System.out.println("Successfully created core.ts file with " + tsPaths.size() + " ts files.");
    }

    /**
     * @param srcDir
     */
    private static Set<String> findTsPaths(File srcDir) {
        Set<String> paths = new TreeSet<String>();
        findTsPaths(srcDir, paths);
        return paths;
    }

    /**
     * @param srcDir
     * @param paths
     */
    private static void findTsPaths(File srcDir, Set<String> paths) {
        for (File file : srcDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".ts")) {
                String fullPath = file.getAbsolutePath();
                if (fullPath.startsWith(rootDir)) {
                    String relativePath = "." + fullPath.substring(rootDir.length(), fullPath.length() - 3).replace('\\', '/');
                    paths.add(relativePath);
                }
            } else if (file.isDirectory()) {
                findTsPaths(file, paths);
            }
        }
    }

}
