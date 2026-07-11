# Step-by-Step Guide: Merging apitomy-unified-model-generator into apitomy-data-models

This guide walks through merging the `apitomy-unified-model-generator` repository into
`apitomy-data-models` as a multi-module Maven project. After the merge, the combined repo
will contain four modules: `generator`, `generator-maven-plugin`, `generator-maven-plugin-tests`,
and `data-models`.

---

## Prerequisites

- Git 2.22+ with `git-filter-repo` installed (`pip install git-filter-repo`)
- Java 17, Maven 3.8.5+, Node.js 20+
- Write access to both GitHub repositories
- Both repos should have clean working trees (no uncommitted changes)

---

## Phase 1: Pre-Merge Releases

**Goal:** Create a clean baseline so both repos have no unreleased changes.

### Step 1: Release apitomy-unified-model-generator (if needed)

If there are unreleased changes in the generator repo, do a final release (e.g. `2.0.1`) using the
existing release workflow. This is the last release from the standalone repo.

### Step 2: Release apitomy-data-models (if needed)

If there are unreleased changes in data-models, release them using the existing workflow.

### Step 3: Create tracking issues

Create a GitHub issue in both repos describing the merge plan and linking to each other.

---

## Phase 2: Import Generator History

**Goal:** Bring the generator repo's full git history into the data-models repo under a prefix
directory, then merge it.

### Step 4: Install git-filter-repo

```bash
pip install git-filter-repo
```

### Step 5: Create a filtered clone of the generator repo

This rewrites the generator's history so all files appear under `_umg_import/`:

```bash
cd /tmp
git clone https://github.com/apitomy/apitomy-unified-model-generator.git umg-for-merge
cd umg-for-merge
git filter-repo --to-subdirectory-filter _umg_import
```

### Step 6: Add the filtered clone as a remote in data-models and merge

```bash
cd /home/ewittman/git/apitomy/apitomy-data-models
git checkout main
git remote add umg-import /tmp/umg-for-merge
git fetch umg-import
git merge umg-import/main --allow-unrelated-histories -m "Import apitomy-unified-model-generator history"
git remote remove umg-import
```

### Step 7: Verify history was preserved

```bash
git log --follow -- _umg_import/generator/src/main/java/io/apitomy/umg/UnifiedModelGenerator.java
```

You should see the full commit history for that file from the generator repo.

---

## Phase 3: Restructure into Final Layout

**Goal:** Move files from the import prefix and from the data-models root into their final module
directories.

### Step 8: Move generator modules to their final locations

```bash
cd /home/ewittman/git/apitomy/apitomy-data-models

# Move the three generator modules
mv _umg_import/generator generator
mv _umg_import/maven-plugin generator-maven-plugin
mv _umg_import/maven-plugin-tests generator-maven-plugin-tests
```

### Step 9: Move data-models files into the `data-models/` subdirectory

```bash
mkdir data-models

# Move source directories
mv src data-models/
mv jsweet_extension data-models/
mv dependencies data-models/
mv jsweetconfig.json data-models/
mv build.sh data-models/
mv ASYNCAPI_RULES.md data-models/

# Move the current pom.xml (will become the data-models module POM)
mv pom.xml data-models/pom.xml
```

### Step 10: Clean up the import remnants

Remove files from `_umg_import/` that are not needed (repo-level files from the generator that are
superseded by data-models equivalents or are no longer needed):

```bash
# Remove the old generator parent pom (we'll create a new one)
rm _umg_import/pom.xml

# Remove generator repo-level files (data-models already has its own)
rm -f _umg_import/LICENSE
rm -f _umg_import/README.md
rm -f _umg_import/CONTRIBUTING.md
rm -f _umg_import/CODE_OF_CONDUCT.md
rm -f _umg_import/dco.txt
rm -f _umg_import/renovate.json
rm -f _umg_import/PROJECT_INDEX.json
rm -f _umg_import/PROJECT_INDEX.md

# Remove generator CI workflows (will be replaced)
rm -rf _umg_import/.github

# Remove any remaining empty directories
rm -rf _umg_import
```

### Step 11: Commit the restructuring

```bash
git add -A
git commit -m "Restructure into multi-module layout"
```

---

## Phase 4: Create the Parent POM

**Goal:** Create a new root `pom.xml` that serves as the multi-module parent.

### Step 12: Create the root parent POM

Create a new file `pom.xml` at the repo root with this content:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.apitomy</groupId>
    <artifactId>apitomy-data-models-parent</artifactId>
    <version>3.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>apitomy-data-models-parent</name>

    <url>https://www.apicur.io/</url>
    <description>Apitomy Data Models - Parent</description>

    <organization>
        <name>Red Hat</name>
        <url>https://www.redhat.com</url>
    </organization>

    <licenses>
        <license>
            <name>The Apache Software License, Version 2.0</name>
            <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>

    <issueManagement>
        <system>GitHub</system>
        <url>https://github.com/apitomy/apitomy-data-models/issues</url>
    </issueManagement>

    <scm>
        <connection>scm:git:git@github.com:apitomy/apitomy-data-models.git</connection>
        <developerConnection>scm:git:git@github.com:apitomy/apitomy-data-models.git</developerConnection>
        <url>scm:git:git@github.com:apitomy/apitomy-data-models.git</url>
    </scm>

    <developers>
        <developer>
            <name>Eric Wittmann</name>
            <id>EricWittmann</id>
            <email>eric.wittmann@redhat.com</email>
            <organization>Red Hat</organization>
            <roles>
                <role>Project Lead</role>
                <role>Developer</role>
            </roles>
            <timezone>-5</timezone>
        </developer>
    </developers>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.build.timestamp.format>yyyy-MM-dd HH:mm:ss</maven.build.timestamp.format>
        <timestamp>${maven.build.timestamp}</timestamp>

        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.release>17</maven.compiler.release>

        <!-- Node Versions -->
        <version.node-js>20.18.1</version.node-js>

        <!-- Dependency Versions (shared) -->
        <version.com.fasterxml.jackson>2.21.2</version.com.fasterxml.jackson>
        <version.com.fasterxml.jackson.dataformat>2.10.0.pr1</version.com.fasterxml.jackson.dataformat>
        <version.commons-lang3>3.20.0</version.commons-lang3>
        <version.commons-io>2.21.0</version.commons-io>
        <version.junit>4.13.2</version.junit>

        <!-- Dependency Versions (generator-specific) -->
        <version.commons-beanutils>1.8.3</version.commons-beanutils>
        <version.commons-collections4>4.5.0</version.commons-collections4>
        <version.roaster>2.28.0.Final</version.roaster>
        <version.modeshape-common>5.4.1.Final</version.modeshape-common>
        <version.lombok>1.18.44</version.lombok>
        <version.rgxgen>3.1</version.rgxgen>
        <version.apitomy-codegen-maven-plugin>1.3.0</version.apitomy-codegen-maven-plugin>

        <!-- Dependency Versions (data-models-specific) -->
        <version.org.jsweet-core>6.3.0</version.org.jsweet-core>
        <version.org.jsweet-transpiler>3.1.0</version.org.jsweet-transpiler>
        <version.org.skyscreamer>1.5.3</version.org.skyscreamer>

        <!-- Plugin Versions -->
        <version.org.eclipse.m2e.lifecycle-mapping>1.0.0</version.org.eclipse.m2e.lifecycle-mapping>
        <version.org.apache.felix.maven-bundle-plugin>6.0.2</version.org.apache.felix.maven-bundle-plugin>
        <version.maven-compiler-plugin>3.15.0</version.maven-compiler-plugin>
        <version.maven-source-plugin>3.4.0</version.maven-source-plugin>
        <version.maven-javadoc-plugin>3.12.0</version.maven-javadoc-plugin>
        <version.maven-resources-plugin>3.5.0</version.maven-resources-plugin>
        <version.maven-gpg-plugin>3.2.8</version.maven-gpg-plugin>
        <version.org.codehaus.mojo.exec-maven-plugin>3.6.3</version.org.codehaus.mojo.exec-maven-plugin>
        <version.com.github.eirslett.frontend-maven-plugin>2.0.0</version.com.github.eirslett.frontend-maven-plugin>
        <version.org.sonatype.central-publishing-maven-plugin>0.10.0</version.org.sonatype.central-publishing-maven-plugin>
        <version.jsweet-maven-plugin>3.1.1.RedHat</version.jsweet-maven-plugin>
    </properties>

    <modules>
        <module>generator</module>
        <module>generator-maven-plugin</module>
        <module>data-models</module>
    </modules>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${version.maven-compiler-plugin}</version>
                <configuration>
                    <release>17</release>
                    <fork>true</fork>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>${version.maven-source-plugin}</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>${version.maven-javadoc-plugin}</version>
                <executions>
                    <execution>
                        <id>attach-javadocs</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                        <configuration>
                            <source>17</source>
                            <doclint>none</doclint>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>plugin-tests</id>
            <modules>
                <module>generator-maven-plugin-tests</module>
            </modules>
        </profile>
        <profile>
            <id>release</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.sonatype.central</groupId>
                        <artifactId>central-publishing-maven-plugin</artifactId>
                        <version>${version.org.sonatype.central-publishing-maven-plugin}</version>
                        <extensions>true</extensions>
                        <configuration>
                            <publishingServerId>central</publishingServerId>
                            <waitUntil>published</waitUntil>
                            <autoPublish>true</autoPublish>
                        </configuration>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>${version.maven-gpg-plugin}</version>
                        <executions>
                            <execution>
                                <id>sign-artifacts</id>
                                <phase>verify</phase>
                                <goals>
                                    <goal>sign</goal>
                                </goals>
                                <configuration>
                                    <passphraseEnvName>MAVEN_GPG_PASSPHRASE</passphraseEnvName>
                                    <gpgArguments>
                                        <arg>--pinentry-mode</arg>
                                        <arg>loopback</arg>
                                    </gpgArguments>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

</project>
```

---

## Phase 5: Update Module POMs

**Goal:** Point each module's POM at the new parent and clean up duplicated configuration.

### Step 13: Update `generator/pom.xml`

Replace the `<parent>` section to point to the new parent:

```xml
<parent>
    <groupId>io.apitomy</groupId>
    <artifactId>apitomy-data-models-parent</artifactId>
    <version>3.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Remove any properties that are now defined in the parent (Jackson, Commons, JUnit, Lombok, Roaster,
etc. versions). The `<dependencies>` and `<build>` sections can keep using the `${version.*}`
property references -- they'll resolve from the parent.

### Step 14: Update `generator-maven-plugin/pom.xml`

Same `<parent>` change as Step 13.

Remove duplicated properties. The `maven.version` and `maven-annotations.version` properties
can stay in this module POM since they're specific to the plugin module.

### Step 15: Update `generator-maven-plugin-tests/pom.xml`

Same `<parent>` change as Step 13. Keep the `maven-deploy-plugin` skip configuration.

### Step 16: Update `data-models/pom.xml`

This is the most involved change. Edit `data-models/pom.xml` (the former root POM):

1. **Add a `<parent>` section** (it didn't have one before):
   ```xml
   <parent>
       <groupId>io.apitomy</groupId>
       <artifactId>apitomy-data-models-parent</artifactId>
       <version>3.0.0-SNAPSHOT</version>
       <relativePath>../pom.xml</relativePath>
   </parent>
   ```

2. **Remove the `<groupId>` element** (inherited from parent). Keep `<artifactId>` as
   `apitomy-data-models` and `<version>` only if you want to override (usually remove it to
   inherit).

3. **Remove properties now in parent** -- delete all the shared properties
   (`version.com.fasterxml.jackson`, `version.commons-lang3`, `version.maven-compiler-plugin`,
   etc.). Keep only properties unique to this module that are NOT in the parent.

4. **Remove the `<organization>`, `<licenses>`, `<issueManagement>`, `<scm>`, `<developers>`
   sections** -- inherited from parent.

5. **Change the UMG plugin version** from a hardcoded `2.0.0` to `${project.version}`:
   ```xml
   <plugin>
       <groupId>io.apitomy</groupId>
       <artifactId>apitomy-unified-model-generator-maven-plugin</artifactId>
       <version>${project.version}</version>
       ...
   </plugin>
   ```

6. **Remove the `maven-compiler-plugin`, `maven-source-plugin`, and `maven-javadoc-plugin`
   declarations** -- they're now in the parent. Keep module-specific plugins (bundle, build-helper,
   UMG plugin).

7. **Remove the `release` profile** -- it's now in the parent. Keep the `transpilation` profile in
   this module POM.

8. **Keep the `<repositories>` and `<pluginRepositories>` sections** -- the `file:${basedir}/dependencies`
   URL will resolve correctly because `${basedir}` points to the `data-models/` directory.

### Step 17: Commit the POM changes

```bash
git add -A
git commit -m "Update POMs for multi-module structure"
```

---

## Phase 6: Update CI/CD Workflows

**Goal:** Consolidate workflows from both repos into a single set.

### Step 18: Update `verify.yaml`

The existing `.github/workflows/verify.yaml` needs only one change: since the project is now
multi-module, `mvn clean install -Ptranspilation` from the root will build everything. The existing
workflow already does this, so **no changes are needed** unless you want to adjust branch names or
paths-ignore patterns.

Verify the `MAVEN_OPTS` environment variable is set (it already is in the current workflow).

### Step 19: Update `maven-publish.yaml`

Update the Maven deploy command to skip the plugin-tests module (it shouldn't be deployed). Change:

```yaml
command: mvn deploy -Prelease --batch-mode --settings /home/runner/.m2/settings.xml -DskipTests
```

This should work as-is because `generator-maven-plugin-tests` is only included when the
`plugin-tests` profile is active, and it has `maven-deploy-plugin` skip configured anyway. No
changes needed.

### Step 20: Update `npm-publish.yaml`

Update the npm publish path. Change:

```yaml
- name: Publish to NPM
  run: npm publish ./target/ts/
```

To:

```yaml
- name: Publish to NPM
  run: npm publish ./data-models/target/ts/
```

### Step 21: Update `release.yaml`

The `versions:set` command already uses `-DprocessAllModules=true`, which will update all module
POMs. This should work correctly with the new multi-module structure. **No changes needed** to
the release workflow itself.

### Step 22: Review `update-website.yaml`

This workflow references the GitHub release and the repo name. **No changes needed** -- it operates
on GitHub releases, not on the source tree.

### Step 23: Commit CI changes

```bash
git add -A
git commit -m "Update CI workflows for multi-module structure"
```

---

## Phase 7: Update Documentation

### Step 24: Update README.md

Update the root `README.md` to mention that this is now a multi-module project containing both
the data models library and the code generator. Add a brief description of the module structure.

### Step 25: Update renovate.json

If both repos had `renovate.json`, review and merge the configurations. If they're both default
configs, no changes are needed.

### Step 26: Commit documentation changes

```bash
git add -A
git commit -m "Update documentation for multi-module structure"
```

---

## Phase 8: Verify the Build

### Step 27: Build without transpilation

```bash
cd /home/ewittman/git/apitomy/apitomy-data-models
mvn clean install
```

Expected build order:
1. `apitomy-data-models-parent` (pom)
2. `apitomy-unified-model-generator` (jar)
3. `apitomy-unified-model-generator-maven-plugin` (maven-plugin)
4. `apitomy-data-models` (bundle)

All four should build successfully.

### Step 28: Build with transpilation

```bash
mvn clean install -Ptranspilation
```

This additionally runs JSweet transpilation, npm install, npm test, and npm package in the
`data-models` module. Verify that `data-models/target/ts/dist/` is created.

### Step 29: Build with plugin-tests

```bash
mvn clean install -Pplugin-tests
```

This includes the `generator-maven-plugin-tests` module with its integration tests.

### Step 30: Verify version management

```bash
mvn versions:set -DnewVersion=3.0.0-test -DgenerateBackupPoms=false -DprocessAllModules=true
```

Verify all four POMs were updated. Then revert:

```bash
mvn versions:set -DnewVersion=3.0.0-SNAPSHOT -DgenerateBackupPoms=false -DprocessAllModules=true
```

### Step 31: Verify git history preservation

```bash
git log --follow -- generator/src/main/java/io/apitomy/umg/UnifiedModelGenerator.java
```

You should see commits from the original generator repo.

---

## Phase 9: Push and Finalize

### Step 32: Push to a feature branch first

```bash
git checkout -b merge-generator
git push -u origin merge-generator
```

Create a PR and let CI verify the build.

### Step 33: Merge the PR

Once CI passes and you've reviewed, merge the PR to `main`.

### Step 34: Archive the generator repo

1. Go to https://github.com/apitomy/apitomy-unified-model-generator/settings
2. Update the README to say:
   > **This repository has been archived.** The code generator has been merged into
   > [apitomy-data-models](https://github.com/apitomy/apitomy-data-models).
3. Archive the repository (Settings > Danger Zone > Archive this repository)

### Step 35: Update external references

- Update any wiki pages or documentation that link to the generator repo
- Update the Apitomy website if it references the generator repo separately
- Close the tracking issues created in Step 3

---

## Quick Reference: Final Directory Layout

```
apitomy-data-models/
├── pom.xml                              (parent: apitomy-data-models-parent)
├── generator/
│   ├── pom.xml                          (artifactId: apitomy-unified-model-generator)
│   └── src/main/java/io/apitomy/umg/
├── generator-maven-plugin/
│   ├── pom.xml                          (artifactId: apitomy-unified-model-generator-maven-plugin)
│   └── src/main/java/io/apitomy/umg/maven/
├── generator-maven-plugin-tests/
│   ├── pom.xml                          (artifactId: ...-maven-plugin-tests)
│   └── src/
├── data-models/
│   ├── pom.xml                          (artifactId: apitomy-data-models)
│   ├── src/main/java/io/apitomy/datamodels/
│   ├── src/main/resources/specs/
│   ├── src/main/ts/
│   ├── src/test/
│   ├── jsweet_extension/
│   └── dependencies/
├── .github/workflows/
│   ├── verify.yaml
│   ├── release.yaml
│   ├── maven-publish.yaml
│   ├── npm-publish.yaml
│   └── update-website.yaml
├── README.md
├── LICENSE
├── renovate.json
└── ...
```

## Quick Reference: Published Artifacts (unchanged)

| Artifact | Coordinates | Registry |
|---|---|---|
| Generator library | `io.apitomy:apitomy-unified-model-generator` | Maven Central |
| Generator plugin | `io.apitomy:apitomy-unified-model-generator-maven-plugin` | Maven Central |
| Data models | `io.apitomy:apitomy-data-models` | Maven Central |
| Data models (TS) | `@apitomy/data-models` | npm |
| Parent POM (new) | `io.apitomy:apitomy-data-models-parent` | Maven Central |

## Troubleshooting

**Q: Maven can't find the UMG plugin during the data-models build**
A: Ensure the `<modules>` order in the parent POM lists `generator` and `generator-maven-plugin`
before `data-models`. Maven builds modules in the order listed and resolves plugins from the
reactor.

**Q: JSweet transpilation fails with path errors**
A: `${basedir}` resolves to the module directory (`data-models/`), not the repo root. The
`dependencies/` folder and `jsweet_extension/` must be inside `data-models/`.

**Q: `mvn versions:set` doesn't update all POMs**
A: Make sure you pass `-DprocessAllModules=true`. Also verify all module POMs use
`${project.version}` for the parent version (or inherit it).

**Q: Plugin-tests fail after the merge**
A: The integration tests in `generator-maven-plugin-tests` reference the plugin by version.
Check that `src/it/settings.xml` and `invoker.properties` files use `${project.version}`.
