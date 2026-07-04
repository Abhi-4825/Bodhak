package com.example.bodhak.orchestration;
import com.example.bodhak.context.ApiSurface;
import com.example.bodhak.compiler.PipelineContext;
import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.frontend.FrontendRegistry;
import com.example.bodhak.frontend.LanguageFrontend;
import com.example.bodhak.model.diagnostic.WarningRule;
import com.example.bodhak.model.project.ProjectRootInfo;
import com.example.bodhak.model.entity.EntityViewModel;
import com.example.bodhak.context.GraphSnapshot;
import com.example.bodhak.context.DependencyGraph;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.AnalysisContextManager;
import com.example.bodhak.context.AnalysisContextFactory;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.SymbolTable;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.namespace.NamespaceInfo;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.model.project.ProjectSnapshot;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.classification.detection.DetectionContext;
import com.example.bodhak.classification.intelligence.ProjectIntelligenceEngine;
import com.example.bodhak.orchestration.snapshot.NamespaceBuilder;
import com.example.bodhak.orchestration.snapshot.ProjectInfoBuilder;
import com.example.bodhak.orchestration.snapshot.ProjectSnapshotBuilder;
import com.example.bodhak.orchestration.incremental.EntityViewModelBuilder;

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

    private final com.example.bodhak.endpoint.EndpointDiscoveryEngine endpointDiscoveryEngine;

    // ── Mutable analysis state ────────────────────────────────────────────────

    private final Map<Path, List<EntityInfo>> entityPathMap  = new ConcurrentHashMap<>();
    private final Map<Path, Set<String>>      pathNamesMap   = new ConcurrentHashMap<>();
    private final List<EntityInfo>            allEntities    = new ArrayList<>();
    private Path projectPath;

    /** Canonical analysis result — populated after {@code analyze()} completes. */
    private AnalysisContext analysisContext;

    private ApiSurface apiSurface;
    private FrontendRegistry frontendRegistry;
    private final com.example.bodhak.orchestration.incremental.engine.IncrementalCompilerOrchestrator orchestrator;

    public FrontendRegistry getFrontendRegistry() {
        return frontendRegistry;
    }

    public com.example.bodhak.orchestration.incremental.engine.IncrementalCompilerOrchestrator getOrchestrator() {
        return orchestrator;
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public AnalysisEngine(
            FrontendRegistry registry,
            EntityViewModelBuilder viewModelBuilder,
            com.example.bodhak.endpoint.EndpointDiscoveryEngine endpointDiscoveryEngine,
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

        this.orchestrator = new com.example.bodhak.orchestration.incremental.engine.IncrementalCompilerOrchestrator(
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

        // Stage 1 — Scan files
        Set<Path> files = scanner.scan(projectPath);

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
        frontendRegistry.register(new com.example.bodhak.frontend.java.JavaLanguageFrontend(sourceRoots));
        frontendRegistry.register(new com.example.bodhak.frontend.python.PythonLanguageFrontend());
        this.frontendRegistry = frontendRegistry;

        com.example.bodhak.compiler.AnalysisPipeline pipeline =
                new com.example.bodhak.compiler.AnalysisPipeline(frontendRegistry);
        PipelineContext pipelineContext =
                pipeline.analyze(new ArrayList<>(files));
        List<CompilationUnit> compilationUnits =
                pipelineContext.getCompilationUnits();

        // Populate entities
        for (CompilationUnit cu : compilationUnits) {
            Path file = cu.getFilePath().toAbsolutePath().normalize();
            allEntities.addAll(cu.getEntities());
            entityPathMap.put(file, new ArrayList<>(cu.getEntities()));
            pathNamesMap.put(file, cu.getEntities().stream().map(EntityInfo::getEntityName).collect(Collectors.toSet()));
            
            // Sync dependency graph data for the compatibility layers
            Map<String, Set<String>> entityDeps = new HashMap<>();
            for (EntityInfo entity : cu.getEntities()) {
                entityDeps.put(entity.getEntityName(), entity.getRelationships().dependsOn());
            }
            this.dependencyGraph.updateDependenciesForFile(file, entityDeps);
        }

        // Stage 6 — Build ViewModels
        viewModelBuilder.initialBuild(allEntities);

        // Stage 8 — Build ProjectInfo
        ProjectRootInfo projectRoots =
                (ProjectRootInfo) pipelineContext.getAttribute("project_roots");
        ProjectInfo projectInfo = projectInfoBuilder.buildAll(projectPath, allEntities, projectRoots);

        // Initialize incremental compiler orchestrator
        this.orchestrator.initialBuild(projectPath, compilationUnits, pipelineContext);

        this.analysisContext = this.analysisContextManager.getCurrentContext();

        DetectionContext detectionContext = new DetectionContext(allEntities, projectInfo);
        this.apiSurface = endpointDiscoveryEngine.analyze(detectionContext, registry);
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
    public ApiSurface getApiSurface() {
        return apiSurface;
    }

    public void onFolderCreate(Path folder) { scanner.onFolderCreated(folder); }

    public void onFolderDelete(Path folder) { scanner.onFolderDeleted(folder); }
}
