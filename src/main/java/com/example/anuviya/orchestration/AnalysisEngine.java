package com.example.anuviya.orchestration;
import com.example.anuviya.frontend.FrontendRegistry;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.context.DependencyGraph;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.AnalysisContextManager;
import com.example.anuviya.context.AnalysisContextFactory;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.classification.classifier.ProjectClassificationResult;
import com.example.anuviya.classification.detection.DetectionContext;
import com.example.anuviya.classification.intelligence.ProjectIntelligenceEngine;
import com.example.anuviya.orchestration.snapshot.NamespaceBuilder;
import com.example.anuviya.orchestration.snapshot.ProjectInfoBuilder;
import com.example.anuviya.orchestration.snapshot.ProjectSnapshotBuilder;
import com.example.anuviya.orchestration.incremental.EntityViewModelBuilder;

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

    private final FrontendRegistry registry;
    private final ProjectScanner         scanner;
    private final DependencyGraph        dependencyGraph;
    private final NamespaceBuilder       namespaceBuilder;
    private final ProjectInfoBuilder     projectInfoBuilder;
    private final ProjectSnapshotBuilder projectSnapshotBuilder;
    private final AnalysisContextFactory analysisContextFactory;
    private final AnalysisContextManager analysisContextManager;
    private final EntityViewModelBuilder viewModelBuilder;
    private final ProjectIntelligenceEngine projectIntelligenceEngine;

    private final com.example.anuviya.endpoint.EndpointDiscoveryEngine endpointDiscoveryEngine;

    // ── Mutable analysis state ────────────────────────────────────────────────

    private final Map<Path, List<EntityInfo>> entityPathMap  = new ConcurrentHashMap<>();
    private final Map<Path, Set<String>>      pathNamesMap   = new ConcurrentHashMap<>();
    private final List<EntityInfo>            allEntities    = new ArrayList<>();
    private Path projectPath;

    /** Canonical analysis result — populated after {@code analyze()} completes. */
    private AnalysisContext analysisContext;

    private com.example.anuviya.context.ApiSurface apiSurface;
    private FrontendRegistry frontendRegistry;
    private final com.example.anuviya.orchestration.incremental.engine.IncrementalCompilerOrchestrator orchestrator;

    public FrontendRegistry getFrontendRegistry() {
        return frontendRegistry;
    }

    public com.example.anuviya.orchestration.incremental.engine.IncrementalCompilerOrchestrator getOrchestrator() {
        return orchestrator;
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public AnalysisEngine(
            FrontendRegistry registry,
            EntityViewModelBuilder viewModelBuilder,
            com.example.anuviya.endpoint.EndpointDiscoveryEngine endpointDiscoveryEngine,
            AnalysisContextManager analysisContextManager,
            AnalysisContextFactory analysisContextFactory
    ) {
        this.registry              = registry;
        this.viewModelBuilder      = viewModelBuilder;
        this.scanner               = new ProjectScanner(registry);
        this.dependencyGraph       = new DependencyGraph();
        this.namespaceBuilder      = new NamespaceBuilder();
        this.projectInfoBuilder    = new ProjectInfoBuilder(scanner);
        this.projectSnapshotBuilder = new ProjectSnapshotBuilder();
        this.analysisContextFactory = analysisContextFactory;
        this.analysisContextManager = analysisContextManager;
        this.projectIntelligenceEngine = new ProjectIntelligenceEngine();
        this.endpointDiscoveryEngine = endpointDiscoveryEngine;

        this.orchestrator = new com.example.anuviya.orchestration.incremental.engine.IncrementalCompilerOrchestrator(
                registry,
                viewModelBuilder,
                projectIntelligenceEngine,
                analysisContextFactory,
                analysisContextManager,
                projectInfoBuilder,
                dependencyGraph
        );
    }

    // ── Full analysis ─────────────────────────────────────────────────────────

    public void analyze(Path projectPath) {
        this.projectPath = projectPath;
        allEntities.clear();
        entityPathMap.clear();
        pathNamesMap.clear();

        // Project Discovery Started
        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.ProjectDiscoveryStarted(projectPath)
        );

        // Stage 1 — Scan files
        Set<Path> files = scanner.scan(projectPath);

        // Files Discovered
        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.FilesDiscovered(new ArrayList<>(files))
        );

        // Run the new Next-Gen pipeline
        List<Path> sourceRoots = new ArrayList<>();
        Path srcMainJava = projectPath.resolve("src/main/java");
        if (java.nio.file.Files.exists(srcMainJava)) {
            sourceRoots.add(srcMainJava);
        } else {
            sourceRoots.add(projectPath);
        }

        FrontendRegistry frontendRegistry =
                new FrontendRegistry();
        frontendRegistry.register(new com.example.anuviya.frontend.java.JavaLanguageFrontend(sourceRoots));
        frontendRegistry.register(new com.example.anuviya.frontend.python.PythonLanguageFrontend(sourceRoots));
        this.frontendRegistry = frontendRegistry;

        com.example.anuviya.compiler.AnalysisPipeline pipeline =
                new com.example.anuviya.compiler.AnalysisPipeline(frontendRegistry);
        com.example.anuviya.compiler.PipelineContext pipelineContext =
                pipeline.analyze(new ArrayList<>(files));
        List<com.example.anuviya.compiler.CompilationUnit> compilationUnits =
                pipelineContext.getCompilationUnits();

        // Populate entities
        int entityCount = 0;
        for (com.example.anuviya.compiler.CompilationUnit cu : compilationUnits) {
            Path file = cu.getFilePath().toAbsolutePath().normalize();
            allEntities.addAll(cu.getEntities());
            entityPathMap.put(file, new ArrayList<>(cu.getEntities()));
            pathNamesMap.put(file, cu.getEntities().stream().map(EntityInfo::getEntityName).collect(Collectors.toSet()));
            
            // Sync dependency graph data for the compatibility layers
            Map<String, Set<String>> entityDeps = new HashMap<>();
            for (EntityInfo entity : cu.getEntities()) {
                entityCount++;
                // Entity Extracted Event
                com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
                    new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.EntityExtracted(entity.getEntityName(), entityCount)
                );
                entityDeps.put(entity.getEntityName(), entity.getRelationships().dependsOn());
            }
            this.dependencyGraph.updateDependenciesForFile(file, entityDeps);
        }

        // Stage 6 — Build ViewModels
        viewModelBuilder.initialBuild(allEntities);

        // Stage 8 — Build ProjectInfo
        com.example.anuviya.model.project.ProjectRootInfo projectRoots =
                (com.example.anuviya.model.project.ProjectRootInfo) pipelineContext.getAttribute("project_roots");
        ProjectInfo projectInfo = projectInfoBuilder.buildAll(projectPath, allEntities, projectRoots);

        // Initialize incremental compiler orchestrator
        this.orchestrator.initialBuild(projectPath, compilationUnits, pipelineContext);

        this.analysisContext = this.analysisContextManager.getCurrentContext();

        DetectionContext detectionContext = new DetectionContext(allEntities, projectInfo);
        this.apiSurface = endpointDiscoveryEngine.analyze(detectionContext, registry);

        // Analysis Completed Event
        int totalFiles = files.size();
        int totalEntities = allEntities.size();
        int totalRefs = 0;
        com.example.anuviya.context.db.ReferenceDatabase refDb =
            (com.example.anuviya.context.db.ReferenceDatabase) pipelineContext.getAttribute("reference_database");
        if (refDb != null) {
            totalRefs = refDb.getAllReferences().size();
        }
        int totalNamespaces = this.analysisContextManager.getCurrentContext().getNamespaces().size();
        int totalFrameworks = 0;
        ProjectClassificationResult classResult =
            (ProjectClassificationResult) pipelineContext.getAttribute("project_classification");
        if (classResult != null && classResult.detectedFrameworks() != null) {
            totalFrameworks = classResult.detectedFrameworks().size();
        }
        int totalMetrics = totalEntities * 5;

        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.AnalysisCompleted(
                totalFiles, totalEntities, totalRefs, totalNamespaces, totalFrameworks, totalMetrics
            )
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void applyGraphDependencies(List<EntityInfo> entities) {
        var snapshot = dependencyGraph.snapshot();
        Map<String, Set<String>> revDeps = snapshot.reverseDependencies();
        Set<Set<String>>         cycles  = snapshot.circularGroups();

        for (EntityInfo e : entities) {
            String name = e.getEntityName();
            String simpleName = e.getSimpleName();

            // Depends ON
            Map<String, Set<String>> globalDeps = snapshot.globalDependencies();
            if (globalDeps != null) {
                e.getDependsOn().addAll(globalDeps.getOrDefault(name, Set.of()));
                if (simpleName != null) {
                    e.getDependsOn().addAll(globalDeps.getOrDefault(simpleName, Set.of()));
                }
            }

            // Used BY
            e.getUsedBy().addAll(revDeps.getOrDefault(name, Set.of()));
            if (simpleName != null) {
                e.getUsedBy().addAll(revDeps.getOrDefault(simpleName, Set.of()));
            }

            // Cycles
            Set<Set<String>> myCycles = new HashSet<>();
            for (Set<String> cycle : cycles) {
                if (cycle.contains(name) || (simpleName != null && cycle.contains(simpleName))) {
                    myCycles.add(cycle);
                }
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
        return this.analysisContextManager.getCurrentContext();
    }

    public AnalysisContextManager getAnalysisContextManager() {
        return analysisContextManager;
    }

    /**
     * Convenience accessor for project metadata.
     * Equivalent to {@code getAnalysisContext().getProjectInfo()}.
     */
    public ProjectInfo getProjectInfo() {
        AnalysisContext ctx = getAnalysisContext();
        return ctx == null ? null : ctx.getProjectInfo();
    }

    /**
     * Returns an unmodifiable snapshot of entities for the given file path.
     * Returns an empty list if the path is unknown.
     */
    public List<EntityInfo> getEntitiesForFile(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        List<EntityInfo> list = orchestrator.getCache().getEntities(normalized);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public DependencyGraph getDependencyGraph() { return dependencyGraph; }

    /** Returns the latest immutable {@link GraphSnapshot}. */
    public GraphSnapshot getGraphSnapshot() {
        return dependencyGraph.snapshot();
    }

    public Map<Path, List<EntityInfo>> getEntityPathMap() {
        return orchestrator.getCache().getEntityCache();
    }

    public FrontendRegistry getPluginRegistry() {
        return registry;
    }

    /** Returns the result of project-type classification, or null if not yet analyzed. */
    public ProjectClassificationResult getClassificationResult() {
        AnalysisContext ctx = getAnalysisContext();
        return ctx == null ? null : ctx.getClassificationResult();
    }

    /** Returns the discovered API surface, or null if not yet analyzed. */
    public com.example.anuviya.context.ApiSurface getApiSurface() {
        return apiSurface;
    }

    public void onFolderCreate(Path folder) { scanner.onFolderCreated(folder); }

    public void onFolderDelete(Path folder) { scanner.onFolderDeleted(folder); }
}
