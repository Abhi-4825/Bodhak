package com.example.bodhak.classification;
import com.example.bodhak.frontend.FrontendRegistry;
import com.example.bodhak.model.project.ProjectSnapshot;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.*;
import com.example.bodhak.model.project.ProjectRootInfo;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.classification.detection.DetectionContext;
import com.example.bodhak.context.DependencyGraph;

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
                new Identity("com.example." + name, name, "com.example", EntityKind.CLASS, language, Set.of(ModifierKind.PUBLIC)),
                new SourceLocation(tempDir.resolve(name + ".java").toFile(), 1, 0, 50, 0, 50),
                new Structure(Set.of(), List.of(), List.of(), List.of(), List.of()),
                new Relationships(Set.of(), Set.of(), Set.of(), Set.of()),
                new Metrics(50, 1, 1, 0, 0, 0, 0),
                new Documentation("", List.of(), Set.copyOf(decorators), List.of()),
                new Contribution(Set.copyOf(tags), new EntityContribution(Set.copyOf(tags))),
                new LanguageMetadata(Map.of())
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
                new ProjectRootInfo(Collections.emptySet(), Collections.emptyList()),            // projectRootInfo
                entities.size()                     // totalEntities
        );

        FrontendRegistry emptyRegistry = new FrontendRegistry();
        DependencyGraph dependencyGraph = new DependencyGraph(emptyRegistry);

        ProjectSnapshot snapshot =
                new ProjectSnapshot(projectInfo, entities, Map.of());
        com.example.bodhak.classification.classifier.ProjectClassificationResult classificationResult =
                new com.example.bodhak.classification.classifier.ProjectClassificationResult(
                        Map.of(),
                        ProjectType.LIBRARY,
                        new com.example.bodhak.classification.capability.CapabilityProfile(Map.of(), 0),
                        List.of()
                );
        return new com.example.bodhak.context.DefaultAnalysisContextFactory().create(
                snapshot, dependencyGraph, classificationResult, List.of());
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
