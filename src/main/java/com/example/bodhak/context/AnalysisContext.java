package com.example.bodhak.context;
import com.example.bodhak.compiler.CompilationUnit;

import com.example.bodhak.quality.flag.EntityCharacteristics;
import com.example.bodhak.quality.flag.ProjectBaselines;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.namespace.NamespaceInfo;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.model.project.ProjectSnapshot;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.context.DependencyGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Canonical analysis state for a loaded project.
 *
 * Contains everything consumers need for AI evidence building,
 * metric calculation, growth analysis, and UI rendering:
 *   - ProjectSnapshot              (project metadata + entities + namespaces)
 *   - DependencyGraph              (inter-entity dependency data)
 *   - ProjectBaselines             (p90 thresholds calculated from entities)
 *   - ProjectClassificationResult  (project type: REST_API, WEB_APP, …)
 *
 * All data is accessed via helper methods — callers never need to unwrap
 * the snapshot manually.
 */
public final class AnalysisContext {

    private final ProjectSnapshot snapshot;
    private final DependencyGraph dependencyGraph;
    private final ProjectBaselines baselines;
    private final ProjectClassificationResult classificationResult;
    private final Map<String, EntityInfo> entityMap;
    private final Map<String, EntityCharacteristics> characteristicsMap;
    private final List<CompilationUnit> compilationUnits;
    private final com.example.bodhak.context.db.ReferenceDatabase referenceDatabase;
    private final com.example.bodhak.compiler.symbol.SymbolTable symbolTable;
    private final com.example.bodhak.model.project.ProjectModel projectModel;
    private final SemanticGraphIndex semanticGraphIndex;

    public AnalysisContext(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectBaselines baselines,
            ProjectClassificationResult classificationResult,
            Collection<EntityCharacteristics> characteristics,
            List<CompilationUnit> compilationUnits,
            com.example.bodhak.context.db.ReferenceDatabase referenceDatabase,
            com.example.bodhak.compiler.symbol.SymbolTable symbolTable,
            com.example.bodhak.model.project.ProjectModel projectModel
    ) {
        this.snapshot = snapshot;
        this.dependencyGraph = dependencyGraph;
        this.baselines = baselines;
        this.classificationResult = classificationResult;
        this.compilationUnits = List.copyOf(compilationUnits);
        this.referenceDatabase = referenceDatabase;
        this.symbolTable = symbolTable;
        this.projectModel = projectModel != null ? projectModel : com.example.bodhak.model.project.ProjectModel.empty();

        this.entityMap =
                snapshot.entities()
                         .stream()
                         .collect(Collectors.toMap(
                                 EntityInfo::getEntityName,
                                 e -> e
                         ));

        this.characteristicsMap =
                characteristics.stream()
                        .collect(Collectors.toMap(
                                c -> c.entityInfo().getEntityName(),
                                c -> c
                        ));

        this.semanticGraphIndex = new SemanticGraphIndex(this);
    }

    // ── Snapshot delegation ────────────────────────────────────────────────────

    /** Full structural snapshot (entities + namespaces). */
    public ProjectSnapshot getSnapshot()                        { return snapshot;                        }

    /** Convenience: project metadata (name, root, language map, entry point, …). */
    public ProjectInfo getProjectInfo()                         { return snapshot.projectInfo();           }

    /** Convenience: all parsed entities. */
    public List<EntityInfo> getEntities()                       { return snapshot.entities();              }

    /** Convenience: namespace key → NamespaceInfo map. */
    public Map<String, NamespaceInfo> getNamespaces()           { return snapshot.namespaces();            }

    // ── Graph ──────────────────────────────────────────────────────────────────

    public DependencyGraph getDependencyGraph()                  { return dependencyGraph;                 }

    public SemanticGraphIndex getSemanticGraphIndex()             { return semanticGraphIndex;              }

    // ── Baselines ──────────────────────────────────────────────────────────────

    /** p90 metric baselines derived from the entity population. */
    public ProjectBaselines getBaselines()                       { return baselines;                       }

    // ── Classification ────────────────────────────────────────────────────────

    /** Project-type classification (REST_API, WEB_APP, LIBRARY, …). */
    public ProjectClassificationResult getClassificationResult() { return classificationResult;            }


    public Collection<EntityCharacteristics>
    getCharacteristics() {

        return characteristicsMap.values();
    }
    public Optional<EntityCharacteristics>
    findCharacteristics(
            String entityName
    ) {

        return Optional.ofNullable(
                characteristicsMap.get(entityName)
        );
    }
    // ── Entity lookup ─────────────────────────────────────────────────────────

    /** Find an entity by its qualified name. Returns empty if not found. */
    public Optional<EntityInfo> findEntity(String qualifiedName) {
        return Optional.ofNullable(entityMap.get(qualifiedName));
    }

    public List<CompilationUnit> getCompilationUnits() {
        return compilationUnits;
    }

    public com.example.bodhak.context.db.ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public com.example.bodhak.compiler.symbol.SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public com.example.bodhak.model.project.ProjectModel getProjectModel() {
        return projectModel;
    }
}
