package com.example.bodhakfrontend.core.projectType;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityContribution;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.EntityKind;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;
import com.example.bodhakfrontend.engine.DependencyGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Utility that builds minimal, in-memory test fixtures for the
 * Project Type Detection pipeline — no real filesystem scanning needed.
 *
 * Usage pattern:
 * <pre>
 *   DetectionContext ctx = new TestContextBuilder()
 *       .withPomXml("spring-boot-starter-web")
 *       .withJavaEntity("MyApp", Set.of("SpringBootApplication"), Set.of("spring_boot"))
 *       .buildDetectionContext();
 * </pre>
 */
public class TestContextBuilder {

    // ── Temporary directory (cleaned up after each test) ──────────────────────
    private final Path tempDir;

    // ── Entities to include ───────────────────────────────────────────────────
    private final List<EntityInfo> entities = new ArrayList<>();

    // ── Files to write into the temp dir ─────────────────────────────────────
    private final Map<String, String> files = new LinkedHashMap<>();

    // ── Folders to register ───────────────────────────────────────────────────
    private final Set<Path> extraFolders = new HashSet<>();

    public TestContextBuilder() {
        try {
            this.tempDir = Files.createTempDirectory("bodhak-test-");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ── Fluent file-content helpers ───────────────────────────────────────────

    /** Write a pom.xml containing the given content snippet. */
    public TestContextBuilder withPomXml(String content) {
        files.put("pom.xml", "<project>" + content + "</project>");
        return this;
    }

    /** Write a package.json containing the given content. */
    public TestContextBuilder withPackageJson(String content) {
        files.put("package.json", content);
        return this;
    }

    /** Write a requirements.txt containing the given content. */
    public TestContextBuilder withRequirementsTxt(String content) {
        files.put("requirements.txt", content);
        return this;
    }

    /** Write a pyproject.toml containing the given content. */
    public TestContextBuilder withPyprojectToml(String content) {
        files.put("pyproject.toml", content);
        return this;
    }

    /** Write application.properties. */
    public TestContextBuilder withApplicationProperties() {
        files.put("application.properties", "server.port=8080");
        return this;
    }

    /** Write application.yml. */
    public TestContextBuilder withApplicationYml() {
        files.put("application.yml", "server:\n  port: 8080");
        return this;
    }

    /** Write a custom file with arbitrary content. */
    public TestContextBuilder withFile(String name, String content) {
        files.put(name, content);
        return this;
    }

    /** Register a sub-directory name (e.g. "components", "routers"). */
    public TestContextBuilder withDirectory(String name) {
        extraFolders.add(tempDir.resolve(name));
        return this;
    }

    // ── Fluent entity helpers ─────────────────────────────────────────────────

    /**
     * Add a Java entity with specific decorators and EntityContribution tags.
     *
     * @param simpleName  simple class name, e.g. "MyController"
     * @param decorators  annotation names without '@', e.g. Set.of("RestController")
     * @param tags        EntityContribution tags, e.g. Set.of("rest_controller")
     */
    public TestContextBuilder withJavaEntity(String simpleName,
                                             Set<String> decorators,
                                             Set<String> tags) {
        return withEntity(simpleName, "java", decorators, tags);
    }

    /** Add a Python entity. */
    public TestContextBuilder withPythonEntity(String simpleName,
                                               Set<String> decorators,
                                               Set<String> tags) {
        return withEntity(simpleName, "python", decorators, tags);
    }

    /** Add a JavaScript entity. */
    public TestContextBuilder withJsEntity(String simpleName,
                                           Set<String> decorators,
                                           Set<String> tags) {
        return withEntity(simpleName, "javascript", decorators, tags);
    }

    private TestContextBuilder withEntity(String name, String language,
                                          Set<String> decorators, Set<String> tags) {
        entities.add(new EntityInfo(
                "com.example." + name,     // entityName
                "com.example",             // namespaceName
                tempDir.resolve(name + ".java").toFile(), // sourceFile (synthetic)
                EntityKind.CLASS,          // kind
                language,
                Set.of(),                  // fields
                List.of(),                 // members
                Set.copyOf(decorators),    // decorators
                Set.of(),                  // dependsOn
                Set.of(),                  // usedBy
                Set.of(),                  // circularGroups
                false,                     // isAbstract
                false,                     // isFinal
                true,                      // isPublic
                50,                        // linesOfCode
                1,                         // beginLine
                0,                         // beginColumn
                new EntityContribution(Set.copyOf(tags))
        ));
        return this;
    }

    // ── Build methods ─────────────────────────────────────────────────────────

    /** Build a DetectionContext ready for use by framework detectors. */
    public DetectionContext buildDetectionContext() {
        return new DetectionContext(buildAnalysisContext());
    }

    /** Build the underlying AnalysisContext. */
    public AnalysisContext buildAnalysisContext() {
        // Write files to temp directory
        Set<Path> knownFiles = new HashSet<>();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            Path p = tempDir.resolve(entry.getKey());
            try {
                Files.writeString(p, entry.getValue());
                knownFiles.add(p);
            } catch (IOException e) {
                throw new RuntimeException("Could not write test file: " + entry.getKey(), e);
            }
        }

        // Register extra folders
        Set<Path> knownFolders = new HashSet<>(extraFolders);
        knownFolders.add(tempDir);

        ProjectInfo projectInfo = new ProjectInfo(
                tempDir.getFileName() != null ? tempDir.getFileName().toString() : "test-project",
                tempDir,
                Map.of(),                           // languageCountMap
                List.of(),                          // largestFiles
                knownFolders,                       // knownFolders
                knownFiles,                         // knownFiles
                EntryPointInfo.unknown(),            // entryPointInfo
                entities.size()                     // totalEntities
        );

        LanguagePluginRegistry emptyRegistry = new LanguagePluginRegistry();
        DependencyGraph dependencyGraph = new DependencyGraph(emptyRegistry);

        com.example.bodhakfrontend.core.model.project.ProjectSnapshot snapshot =
                new com.example.bodhakfrontend.core.model.project.ProjectSnapshot(projectInfo, entities, Map.of());
        com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult classificationResult =
                new com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult(
                        Map.of(),
                        ProjectType.LIBRARY,
                        new com.example.bodhakfrontend.core.projectType.capability.CapabilityProfile(Map.of(), 0),
                        List.of()
                );
        return new com.example.bodhakfrontend.core.analysis.builder.DefaultAnalysisContextFactory().create(
                snapshot, dependencyGraph, classificationResult);
    }

    /**
     * Delete temp files. Call in @AfterEach.
     * Failures are silently swallowed — temp cleanup is best-effort.
     */
    public void cleanup() {
        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {}
    }
}
