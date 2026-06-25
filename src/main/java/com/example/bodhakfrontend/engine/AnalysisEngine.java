package com.example.bodhakfrontend.engine;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.analysis.builder.DefaultAnalysisContextFactory;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.project.ProjectSnapshot;
import com.example.bodhakfrontend.core.plugin.LanguagePlugin;
import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectorRegistry;
import com.example.bodhakfrontend.core.projectType.engine.ProjectTypeAnalyzer;
import com.example.bodhakfrontend.engine.analyzer.GlobalEntryPointDetector;
import com.example.bodhakfrontend.engine.builder.NamespaceBuilder;
import com.example.bodhakfrontend.engine.builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.engine.builder.ProjectSnapshotBuilder;
import com.example.bodhakfrontend.engine.incremental.EntityViewModelBuilder;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * Central analysis orchestrator.
 *
 * Analysis pipeline (on full analyze):
 *   1. Scan files
 *   2. Extract names (for dependency graph seeding)
 *   3. Build DependencyGraph
 *   4. Build EntityInfo objects (rich parsing)
 *   5. Apply graph dependencies to entities (usedBy, dependsOn, cycles)
 *   6. Build ViewModels (UI incremental layer)
 *   7. Build Namespaces
 *   8. Build ProjectInfo (pure metadata)
 *   9. Build ProjectSnapshot (structure)
 *  10. Classify project type
 *  11. Create AnalysisContext (canonical analysis state)
 *
 * After {@code analyze()} returns, consumers call {@code getAnalysisContext()} or
 * the convenience {@code getProjectInfo()} / {@code getGraphSnapshot()}.
 */
public class AnalysisEngine {

    // ── Collaborators ─────────────────────────────────────────────────────────

    private final LanguagePluginRegistry registry;
    private final ProjectScanner         scanner;
    private final DependencyGraph        dependencyGraph;
    private final NamespaceBuilder       namespaceBuilder;
    private final ProjectInfoBuilder     projectInfoBuilder;
    private final ProjectSnapshotBuilder projectSnapshotBuilder;
    private final DefaultAnalysisContextFactory contextFactory;
    private final EntityViewModelBuilder viewModelBuilder;
    private final ProjectTypeAnalyzer    projectTypeAnalyzer;

    private final com.example.bodhakfrontend.core.api.engine.EndpointDiscoveryEngine endpointDiscoveryEngine;

    // ── Mutable analysis state ────────────────────────────────────────────────

    private final Map<Path, List<EntityInfo>> entityPathMap  = new ConcurrentHashMap<>();
    private final Map<Path, Set<String>>      pathNamesMap   = new ConcurrentHashMap<>();
    private final List<EntityInfo>            allEntities    = new ArrayList<>();

    /** Canonical analysis result — populated after {@code analyze()} completes. */
    private AnalysisContext analysisContext;

    private com.example.bodhakfrontend.core.api.model.ApiSurface apiSurface;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AnalysisEngine(
            LanguagePluginRegistry registry,
            EntityViewModelBuilder viewModelBuilder,
            FrameworkDetectorRegistry detectorRegistry,
            com.example.bodhakfrontend.core.api.engine.EndpointDiscoveryEngine endpointDiscoveryEngine
    ) {
        this.registry              = registry;
        this.viewModelBuilder      = viewModelBuilder;
        this.scanner               = new ProjectScanner(registry);
        this.dependencyGraph       = new DependencyGraph(registry);
        this.namespaceBuilder      = new NamespaceBuilder();
        this.projectInfoBuilder    = new ProjectInfoBuilder(scanner, new GlobalEntryPointDetector(registry));
        this.projectSnapshotBuilder = new ProjectSnapshotBuilder();
        this.contextFactory        = new DefaultAnalysisContextFactory();
        this.projectTypeAnalyzer   = new ProjectTypeAnalyzer(detectorRegistry);
        this.endpointDiscoveryEngine = endpointDiscoveryEngine;
    }

    // ── Full analysis ─────────────────────────────────────────────────────────

    public void analyze(Path projectPath) {

        // Stage 1 — Scan files
        Set<Path> files = scanner.scan(projectPath);

        // Stage 2 — Extract names per file
        for (Path file : files) {
            LanguagePlugin plugin = registry.forFile(file).orElse(null);
            if (plugin != null) {
                Set<String> names = plugin.getNameExtractor().extractNames(file);
                pathNamesMap.put(file, names);
            }
        }

        Set<String> allKnownNames = pathNamesMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        // Stage 3 — Build Dependency Graph
        dependencyGraph.buildInitialGraph(pathNamesMap, allKnownNames);

        // Stage 4 — Build EntityInfo
        for (Path file : files) {
            LanguagePlugin plugin = registry.forFile(file).orElse(null);
            if (plugin != null) {
                List<EntityInfo> entities = plugin.getEntityBuilder().build(file);
                allEntities.addAll(entities);
                entityPathMap.put(file, new ArrayList<>(entities));
            }
        }

        // Stage 5 — Apply graph dependencies to entities
        applyGraphDependencies(allEntities);

        // Stage 6 — Build ViewModels
        viewModelBuilder.initialBuild(allEntities);

        // Stage 7 — Build Namespaces
        Map<String, NamespaceInfo> namespaces = namespaceBuilder.build(allEntities);

        // Stage 8 — Build ProjectInfo (pure metadata)
        ProjectInfo projectInfo = projectInfoBuilder.buildAll(projectPath, allEntities);

        // Stage 9 — Build ProjectSnapshot (structural view)
        ProjectSnapshot snapshot = projectSnapshotBuilder.build(projectInfo, allEntities, namespaces);

        // Stage 10 — Classify project type
        ProjectClassificationResult classificationResult =
                projectTypeAnalyzer.analyze(projectInfo, allEntities);

        // Stage 11 — Create AnalysisContext (canonical analysis state)
        this.analysisContext = contextFactory.create(snapshot, dependencyGraph, classificationResult);
        //
        DetectionContext detectionContext=new DetectionContext(allEntities,projectInfo);
        this.apiSurface=endpointDiscoveryEngine.analyze(detectionContext,registry);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void applyGraphDependencies(List<EntityInfo> entities) {
        Map<String, Set<String>> revDeps = dependencyGraph.getReverseDependencies();
        Set<Set<String>>         cycles  = dependencyGraph.getCircularGroups();

        for (EntityInfo e : entities) {
            String name = e.getEntityName();
            Path file   = e.getSourceFile().toPath().toAbsolutePath().normalize();

            // Depends ON
            Map<String, Set<String>> fileDeps = dependencyGraph.getFileDependencies().get(file);
            if (fileDeps != null) {
                e.getDependsOn().addAll(fileDeps.getOrDefault(name, Set.of()));
            }

            // Used BY
            e.getUsedBy().addAll(revDeps.getOrDefault(name, Set.of()));

            // Cycles
            Set<Set<String>> myCycles = new HashSet<>();
            for (Set<String> cycle : cycles) {
                if (cycle.contains(name)) myCycles.add(cycle);
            }
            e.getCircularGroups().addAll(myCycles);
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the canonical analysis state.
     * Returns {@code null} if {@link #analyze(Path)} has not been called yet.
     */
    public AnalysisContext getAnalysisContext() {
        return analysisContext;
    }

    /**
     * Convenience accessor for project metadata.
     * Equivalent to {@code getAnalysisContext().getProjectInfo()}.
     */
    public ProjectInfo getProjectInfo() {
        return analysisContext == null ? null : analysisContext.getProjectInfo();
    }

    /**
     * Returns an unmodifiable snapshot of entities for the given file path.
     * Returns an empty list if the path is unknown.
     */
    public List<EntityInfo> getEntitiesForFile(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        List<EntityInfo> list = entityPathMap.get(normalized);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public DependencyGraph getDependencyGraph() { return dependencyGraph; }

    /** Returns the latest immutable {@link GraphSnapshot}. */
    public GraphSnapshot getGraphSnapshot() {
        return dependencyGraph.snapshot();
    }

    public Map<Path, List<EntityInfo>> getEntityPathMap() {
        return entityPathMap;
    }

    public LanguagePluginRegistry getPluginRegistry() {
        return registry;
    }

    /** Returns the result of project-type classification, or null if not yet analyzed. */
    public ProjectClassificationResult getClassificationResult() {
        return analysisContext == null ? null : analysisContext.getClassificationResult();
    }

    /** Returns the discovered API surface, or null if not yet analyzed. */
    public com.example.bodhakfrontend.core.api.model.ApiSurface getApiSurface() {
        return apiSurface;
    }

    // ── Incremental update handlers ───────────────────────────────────────────

    public void onFileCreate(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        scanner.onFileCreated(normalized);

        LanguagePlugin plugin = registry.forFile(normalized).orElse(null);
        if (plugin == null) return;

        // Name extraction
        Set<String> names = plugin.getNameExtractor().extractNames(normalized);
        pathNamesMap.put(normalized, names);
        Set<String> allKnownNames = pathNamesMap.values().stream()
                .flatMap(Set::stream).collect(Collectors.toSet());

        // Graph update
        dependencyGraph.updateForFile(normalized, allKnownNames);

        // Entity creation
        List<EntityInfo> newEntities = plugin.getEntityBuilder().build(normalized);
        allEntities.addAll(newEntities);
        entityPathMap.put(normalized, new ArrayList<>(newEntities));

        applyGraphDependencies(allEntities);

        viewModelBuilder.onFileCreate(newEntities);
        projectInfoBuilder.onFileCreated(file);
    }

    public void onFileDelete(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        scanner.onFileDeleted(normalized);

        LanguagePlugin plugin = registry.forFile(normalized).orElse(null);
        if (plugin != null) plugin.getEntityBuilder().invalidate(normalized);

        pathNamesMap.remove(normalized);
        dependencyGraph.removeFile(normalized);

        List<EntityInfo> removed = entityPathMap.remove(normalized);
        if (removed != null) {
            allEntities.removeAll(removed);
            viewModelBuilder.onFileDelete(removed);
        }

        applyGraphDependencies(allEntities);
        projectInfoBuilder.onFileDeleted(file);
    }

    public void onFileModify(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        LanguagePlugin plugin = registry.forFile(normalized).orElse(null);
        if (plugin == null) return;

        List<EntityInfo> oldEntities = new ArrayList<>(
                entityPathMap.getOrDefault(normalized, new ArrayList<>()));
        plugin.getEntityBuilder().invalidate(normalized);

        // Re-extract
        Set<String> names = plugin.getNameExtractor().extractNames(normalized);
        pathNamesMap.put(normalized, names);
        Set<String> allKnownNames = pathNamesMap.values().stream()
                .flatMap(Set::stream).collect(Collectors.toSet());

        dependencyGraph.updateForFile(normalized, allKnownNames);

        allEntities.removeAll(oldEntities);

        List<EntityInfo> newEntities = plugin.getEntityBuilder().build(normalized);
        allEntities.addAll(newEntities);
        entityPathMap.put(normalized, new ArrayList<>(newEntities));

        applyGraphDependencies(allEntities);

        viewModelBuilder.onFileModify(oldEntities, newEntities);
        projectInfoBuilder.onFileDeleted(file);
        projectInfoBuilder.onFileCreated(file);
    }

    public void onFolderCreate(Path folder) { scanner.onFolderCreated(folder); }

    public void onFolderDelete(Path folder) { scanner.onFolderDeleted(folder); }
}
