package com.example.anuviya.context;


import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.model.project.ProjectModel;
import com.example.anuviya.quality.flag.EntityCharacteristics;
import com.example.anuviya.quality.flag.ProjectBaselines;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.namespace.NamespaceInfo;
import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.model.project.ProjectSnapshot;
import com.example.anuviya.classification.classifier.ProjectClassificationResult;


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
    private final ReferenceDatabase referenceDatabase;
    private final SymbolTable symbolTable;
    private final ProjectModel projectModel;
    private final SemanticGraphIndex semanticGraphIndex;

    public AnalysisContext(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectBaselines baselines,
            ProjectClassificationResult classificationResult,
            Collection<EntityCharacteristics> characteristics,
            List<CompilationUnit> compilationUnits,
            ReferenceDatabase referenceDatabase,
            SymbolTable symbolTable,
            ProjectModel projectModel
    ) {
        this.snapshot = snapshot;
        this.dependencyGraph = dependencyGraph;
        this.baselines = baselines;
        this.classificationResult = classificationResult;
        this.compilationUnits = List.copyOf(compilationUnits);
        this.referenceDatabase = referenceDatabase;
        this.symbolTable = symbolTable;
        this.projectModel = projectModel != null ? projectModel : ProjectModel.empty();

        this.entityMap =
                snapshot.entities()
                         .stream()
                         .collect(Collectors.toMap(
                                 EntityInfo::getEntityName,
                                 e -> e,
                                 (existing, replacement) -> existing
                         ));

        this.characteristicsMap =
                characteristics.stream()
                        .collect(Collectors.toMap(
                                c -> c.entityInfo().getEntityName(),
                                c -> c,
                                (existing, replacement) -> existing
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
    public Optional<EntityInfo> findEntity(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        EntityInfo info = entityMap.get(name);
        if (info != null) return Optional.of(info);
        return entityMap.values().stream()
                .filter(e -> name.equals(e.getEntityName()) || name.equals(e.getSimpleName()))
                .findFirst();
    }

    public List<CompilationUnit> getCompilationUnits() {
        return compilationUnits;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ProjectModel getProjectModel() {
        return projectModel;
    }
}
