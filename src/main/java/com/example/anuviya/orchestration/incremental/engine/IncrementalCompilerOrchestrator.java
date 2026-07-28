package com.example.anuviya.orchestration.incremental.engine;

import com.example.anuviya.compiler.AnalysisPipeline;
import com.example.anuviya.compiler.PipelineContext;
import com.example.anuviya.compiler.symbol.Symbol;
import com.example.anuviya.compiler.symbol.EntitySymbol;
import com.example.anuviya.compiler.symbol.NamespaceSymbol;
import com.example.anuviya.compiler.symbol.ModuleSymbol;
import com.example.anuviya.compiler.symbol.MemberSymbol;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.AnalysisContextFactory;
import com.example.anuviya.context.AnalysisContextManager;
import com.example.anuviya.context.DependencyGraph;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.frontend.FrontendRegistry;
import com.example.anuviya.model.diagnostic.WarningRule;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.EntityViewModel;
import com.example.anuviya.model.reference.SemanticReference;
import com.example.anuviya.classification.intelligence.ProjectIntelligenceEngine;
import com.example.anuviya.orchestration.snapshot.ProjectInfoBuilder;
import com.example.anuviya.orchestration.incremental.EntityViewModelBuilder;
import com.example.anuviya.quality.warning.WarningBuilder;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class IncrementalCompilerOrchestrator {

    private final FrontendRegistry frontendRegistry;
    private final EntityViewModelBuilder viewModelBuilder;
    private final ProjectIntelligenceEngine projectIntelligenceEngine;
    private final AnalysisContextFactory analysisContextFactory;
    private final AnalysisContextManager analysisContextManager;
    private final ProjectInfoBuilder projectInfoBuilder;
    private final DependencyGraph dependencyGraph;

    private final ArtifactCache cache = new ArtifactCache();
    private final FileContentHasher hasher = new FileContentHasher();
    private final ChangeClassifier classifier = new ChangeClassifier(hasher);
    private final ArtifactDependencyGraph dependencyGraphArtifacts = new ArtifactDependencyGraph();
    private final DependencyImpactAnalyzer impactAnalyzer = new DependencyImpactAnalyzer(dependencyGraphArtifacts);
    private final InvalidationPlanner planner = new InvalidationPlanner(impactAnalyzer);
    private final IncrementalContextAssembler assembler = new IncrementalContextAssembler();

    private final AtomicLong versionCounter = new AtomicLong(1);
    private volatile Path projectPath;

    public IncrementalCompilerOrchestrator(
            FrontendRegistry frontendRegistry,
            EntityViewModelBuilder viewModelBuilder,
            ProjectIntelligenceEngine projectIntelligenceEngine,
            AnalysisContextFactory analysisContextFactory,
            AnalysisContextManager analysisContextManager,
            ProjectInfoBuilder projectInfoBuilder,
            DependencyGraph dependencyGraph
    ) {
        this.frontendRegistry = frontendRegistry;
        this.viewModelBuilder = viewModelBuilder;
        this.projectIntelligenceEngine = projectIntelligenceEngine;
        this.analysisContextFactory = analysisContextFactory;
        this.analysisContextManager = analysisContextManager;
        this.projectInfoBuilder = projectInfoBuilder;
        this.dependencyGraph = dependencyGraph;
    }

    public FileContentHasher getHasher() {
        return hasher;
    }

    public ChangeClassifier getClassifier() {
        return classifier;
    }

    public synchronized void initialBuild(Path projectPath, List<com.example.anuviya.compiler.CompilationUnit> initialCus, PipelineContext initialContext) {
        this.projectPath = projectPath;
        cache.clear();
        hasher.clear();

        // 1. Seed caches for initial compilation units
        for (com.example.anuviya.compiler.CompilationUnit cu : initialCus) {
            Path file = cu.getFilePath().toAbsolutePath().normalize();
            hasher.computeHash(file);
            cache.putIR(file, cu.getIntermediateRepresentation());
            cache.putCU(file, cu);
            cache.putEntities(file, new ArrayList<>(cu.getEntities()));
        }

        // 2. Populate SymbolTable and ReferenceDatabase in the Cache
        SymbolTable initialTable = (SymbolTable) initialContext.getAttribute("symbol_table");
        if (initialTable != null) {
            mergeSymbols(initialTable);
        }

        ReferenceDatabase initialRefs = (ReferenceDatabase) initialContext.getAttribute("reference_database");
        if (initialRefs != null) {
            for (SemanticReference ref : initialRefs.getAllReferences()) {
                cache.getReferenceDatabase().addReference(ref);
            }
        }

        // 3. Assemble and publish context
        Set<ArtifactId> allStale = EnumSet.allOf(ArtifactId.class);
        AnalysisContext context = assembler.assemble(
                null,
                cache,
                dependencyGraph,
                analysisContextFactory,
                projectInfoBuilder,
                projectIntelligenceEngine,
                projectPath,
                allStale
        );

        analysisContextManager.replace(context);
    }

    public synchronized void processChanges(Set<ChangeEvent> changes) {
        if (projectPath == null || changes.isEmpty()) return;

        System.out.println("[IncrementalCompiler] Processing " + changes.size() + " change events...");
        InvalidationPlan plan = planner.plan(changes);

        // Evict removed files from caches and graph
        for (Path file : plan.filesToRemove()) {
            Path normalized = file.toAbsolutePath().normalize();
            cache.evict(normalized);
            hasher.remove(normalized);
            dependencyGraph.removeFile(normalized);
            ReferencePatch.patchRemoveFile(cache.getReferenceDatabase(), normalized);
        }

        // Analyze and compile new / modified files
        List<Path> filesToAnalyze = new ArrayList<>(plan.filesToProcess());
        if (!filesToAnalyze.isEmpty()) {
            AnalysisPipeline pipeline = new AnalysisPipeline(frontendRegistry);
            PipelineContext pipelineContext = pipeline.analyze(filesToAnalyze);

            for (com.example.anuviya.compiler.CompilationUnit cu : pipelineContext.getCompilationUnits()) {
                Path file = cu.getFilePath().toAbsolutePath().normalize();
                cache.putIR(file, cu.getIntermediateRepresentation());
                cache.putCU(file, cu);
                cache.putEntities(file, new ArrayList<>(cu.getEntities()));

                // Update dependency graph data
                Map<String, Set<String>> entityDeps = new HashMap<>();
                for (EntityInfo entity : cu.getEntities()) {
                    entityDeps.put(entity.getEntityName(), entity.getRelationships().dependsOn());
                }
                dependencyGraph.updateDependenciesForFile(file, entityDeps);

                // Surgical removal of old references for this file
                ReferencePatch.patchRemoveFile(cache.getReferenceDatabase(), file);

                // Surgical addition of new references
                ReferenceDatabase localRefDb = (ReferenceDatabase) pipelineContext.getAttribute("reference_database");
                if (localRefDb != null) {
                    for (SemanticReference ref : localRefDb.getAllReferences()) {
                        cache.getReferenceDatabase().addReference(ref);
                    }
                }

                // Surgical symbol table merge
                SymbolTable localTable = (SymbolTable) pipelineContext.getAttribute("symbol_table");
                if (localTable != null) {
                    mergeSymbols(localTable);
                }
            }
        }

        // Apply graph dependencies (usedBy, dependsOn, cycles) to updated entities
        applyGraphDependencies();

        // Assemble new context with structural sharing
        AnalysisContext previousContext = analysisContextManager.getCurrentContext();
        AnalysisContext newContext = assembler.assemble(
                previousContext,
                cache,
                dependencyGraph,
                analysisContextFactory,
                projectInfoBuilder,
                projectIntelligenceEngine,
                projectPath,
                plan.staleArtifacts()
        );

        // Update warnings on viewmodels
        WarningBuilder warningBuilder = new WarningBuilder();
        for (EntityViewModel vm : viewModelBuilder.getViewModelMap().values()) {
            List<WarningRule> warnings = warningBuilder.buildWarnings(vm.toEntityInfo(), newContext);
            javafx.application.Platform.runLater(() -> vm.getWarnings().setAll(warnings));
        }

        // Trigger updates in UI State
        analysisContextManager.replace(newContext);
        System.out.println("[IncrementalCompiler] Incremental build complete. New version: " + versionCounter.incrementAndGet());
    }

    private void mergeSymbols(SymbolTable sourceTable) {
        for (Symbol sym : sourceTable.getAllSymbols()) {
            if (sym instanceof EntitySymbol ent) {
                cache.getSymbolTable().getOrCreateEntity(ent.name(), ent.kind());
            } else if (sym instanceof NamespaceSymbol ns) {
                cache.getSymbolTable().getOrCreateNamespace(ns.name());
            } else if (sym instanceof ModuleSymbol mod) {
                cache.getSymbolTable().getOrCreateModule(mod.name());
            } else if (sym instanceof MemberSymbol mem) {
                EntitySymbol parent = (EntitySymbol) cache.getSymbolTable().getOrCreateEntity(mem.parent().name(), mem.parent().kind());
                cache.getSymbolTable().getOrCreateMember(parent, mem.name(), mem.kind());
            }
        }
    }

    private void applyGraphDependencies() {
        var snapshot = dependencyGraph.snapshot();
        Map<String, Set<String>> revDeps = snapshot.reverseDependencies();
        Set<Set<String>> cycles = snapshot.circularGroups();

        for (List<EntityInfo> entities : cache.getEntityCache().values()) {
            for (EntityInfo e : entities) {
                String name = e.getEntityName();
                String simpleName = e.getSimpleName();

                e.getDependsOn().clear();
                e.getUsedBy().clear();
                e.getCircularGroups().clear();

                Map<String, Set<String>> globalDeps = snapshot.globalDependencies();
                if (globalDeps != null) {
                    e.getDependsOn().addAll(globalDeps.getOrDefault(name, Set.of()));
                    if (simpleName != null) {
                        e.getDependsOn().addAll(globalDeps.getOrDefault(simpleName, Set.of()));
                    }
                }

                e.getUsedBy().addAll(revDeps.getOrDefault(name, Set.of()));
                if (simpleName != null) {
                    e.getUsedBy().addAll(revDeps.getOrDefault(simpleName, Set.of()));
                }

                Set<Set<String>> myCycles = new HashSet<>();
                for (Set<String> cycle : cycles) {
                    if (cycle.contains(name) || (simpleName != null && cycle.contains(simpleName))) {
                        myCycles.add(cycle);
                    }
                }
                e.getCircularGroups().addAll(myCycles);
            }
        }
    }

    public ArtifactCache getCache() {
        return cache;
    }
}
