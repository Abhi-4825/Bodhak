package com.example.anuviya.compiler.pass.relationships;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.Relationships;
import com.example.anuviya.compiler.CompilerPass;
import com.example.anuviya.compiler.PipelineContext;
import com.example.anuviya.compiler.symbol.*;
import com.example.anuviya.model.reference.*;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.compiler.extraction.*;

import java.util.*;

/**
 * Compiler pass that analyzes IR declarations and references to build the semantic ReferenceDatabase,
 * and derives traditional dependency projections for backwards compatibility.
 */
public class RelationshipBuilderPass implements CompilerPass {

    @Override
    public String getName() {
        return "RelationshipBuilderPass";
    }

    @Override
    public void execute(PipelineContext context) {
        SymbolTable symbolTable = new SymbolTable();
        ReferenceDatabase database = new ReferenceDatabase();

        Set<String> knownEntityNames = new HashSet<>();
        Map<String, SymbolKind> knownEntityKinds = new HashMap<>();
        for (CompilationUnit cu : context.getCompilationUnits()) {
            for (EntityInfo entity : cu.getEntities()) {
                knownEntityNames.add(entity.getEntityName());
                
                SymbolKind kind = SymbolKind.CLASS;
                switch (entity.getIdentity().kind()) {
                    case INTERFACE -> kind = SymbolKind.INTERFACE;
                    case STRUCT -> kind = SymbolKind.STRUCT;
                    case TRAIT -> kind = SymbolKind.TRAIT;
                    case FUNCTION -> kind = SymbolKind.FUNCTION;
                    case MODULE -> kind = SymbolKind.MODULE;
                    default -> kind = SymbolKind.CLASS;
                }
                knownEntityKinds.put(entity.getEntityName(), kind);
            }
        }

        List<ReferenceExtractor> extractors = List.of(
            new TypeReferenceExtractor(knownEntityNames, knownEntityKinds),
            new CallReferenceExtractor(knownEntityNames, knownEntityKinds),
            new DataFlowReferenceExtractor(knownEntityNames),
            new FrameworkReferenceExtractor()
        );

        // Phase 1: Run extraction on all compilation units to build the central database
        for (CompilationUnit cu : context.getCompilationUnits()) {
            if (cu.getIntermediateRepresentation() != null) {
                for (ReferenceExtractor extractor : extractors) {
                    extractor.extract(cu.getIntermediateRepresentation(), symbolTable, database, cu);
                }
            }
        }

        // Reference Database Built Event
        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.ReferenceDatabaseBuilt(database.getAllReferences().size())
        );

        // Store database and table in PipelineContext
        context.setAttribute("reference_database", database);
        context.setAttribute("symbol_table", symbolTable);



        // Phase 2: Project outgoing and incoming dependencies from the database
        Map<String, Set<String>> adjacencyList = new HashMap<>();
        Map<String, Set<String>> reverseDeps = new HashMap<>();
        for (String entityFqn : knownEntityNames) {
            reverseDeps.put(entityFqn, new HashSet<>());
        }

        for (String fqn : knownEntityNames) {
            EntitySymbol entitySym = symbolTable.getOrCreateEntity(fqn, SymbolKind.CLASS);
            Set<String> deps = new HashSet<>();

            List<SemanticReference> outgoing = database.getOutgoing(entitySym.id());
            for (SemanticReference ref : outgoing) {
                String targetFqn = ref.targetSymbol().name();
                if (knownEntityNames.contains(targetFqn)) {
                    deps.add(targetFqn);
                }
            }
            deps.remove(fqn); // self-dependency removal
            adjacencyList.put(fqn, deps);

            for (String dep : deps) {
                if (reverseDeps.containsKey(dep)) {
                    reverseDeps.get(dep).add(fqn);
                }
            }
        }

        // Dependency Graph Built Event
        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.DependencyGraphBuilt(adjacencyList.size())
        );

        // Phase 3: Detect circular dependencies globally
        Set<Set<String>> sccs = findCircularGroups(adjacencyList);

        // Graph Index Built Event
        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.GraphIndexBuilt()
        );

        // Phase 4: Enrich entity info instances with projected relationships
        for (CompilationUnit cu : context.getCompilationUnits()) {
            List<EntityInfo> enrichedEntities = new ArrayList<>();
            for (EntityInfo entity : cu.getEntities()) {
                String fqn = entity.getEntityName();
                Set<String> deps = adjacencyList.getOrDefault(fqn, Collections.emptySet());
                Set<String> uses = reverseDeps.getOrDefault(fqn, Collections.emptySet());

                // Find cycle group for this entity
                Set<Set<String>> entityCycles = new HashSet<>();
                for (Set<String> scc : sccs) {
                    if (scc.contains(fqn)) {
                        entityCycles.add(scc);
                    }
                }

                Relationships relationships = new Relationships(
                    deps,
                    uses,
                    entity.getRelationships().superTypes(),
                    entityCycles
                );

                EntityInfo enriched = new EntityInfo(
                    entity.getIdentity(),
                    entity.getSourceLocation(),
                    entity.getStructure(),
                    relationships,
                    entity.getMetrics(),
                    entity.getDocumentation(),
                    entity.getContribution(),
                    entity.getLanguageMetadata()
                );
                enrichedEntities.add(enriched);
            }
            cu.getEntities().clear();
            cu.getEntities().addAll(enrichedEntities);
        }
    }

    // Tarjan's SCC cycle finder
    private Set<Set<String>> findCircularGroups(Map<String, Set<String>> adjacencyList) {
        Tarjan solver = new Tarjan(adjacencyList);
        return solver.solve();
    }

    private static class Tarjan {
        private final Map<String, Set<String>> graph;
        private final Map<String, Integer> ids = new HashMap<>();
        private final Map<String, Integer> low = new HashMap<>();
        private final Map<String, Boolean> onStack = new HashMap<>();
        private final Deque<String> stack = new ArrayDeque<>();
        private int idCounter = 0;
        private final Set<Set<String>> sccs = new HashSet<>();

        public Tarjan(Map<String, Set<String>> graph) {
            this.graph = graph;
        }

        public Set<Set<String>> solve() {
            for (String node : graph.keySet()) {
                if (!ids.containsKey(node)) {
                    dfs(node);
                }
            }
            return sccs;
        }

        private void dfs(String node) {
            ids.put(node, idCounter);
            low.put(node, idCounter);
            idCounter++;
            stack.push(node);
            onStack.put(node, true);

            Set<String> neighbors = graph.getOrDefault(node, Collections.emptySet());
            for (String neighbor : neighbors) {
                if (graph.containsKey(neighbor)) { // resolve cycles only on valid nodes
                    if (!ids.containsKey(neighbor)) {
                        dfs(neighbor);
                        low.put(node, Math.min(low.get(node), low.get(neighbor)));
                    } else if (onStack.getOrDefault(neighbor, false)) {
                        low.put(node, Math.min(low.get(node), ids.get(neighbor)));
                    }
                }
            }

            if (ids.get(node).equals(low.get(node))) {
                Set<String> scc = new HashSet<>();
                while (true) {
                    String top = stack.pop();
                    onStack.put(top, false);
                    scc.add(top);
                    if (top.equals(node)) break;
                }
                if (scc.size() > 1) {
                    sccs.add(scc);
                }
            }
        }
    }
}
