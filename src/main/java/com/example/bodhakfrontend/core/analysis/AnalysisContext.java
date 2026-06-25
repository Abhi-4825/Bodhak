package com.example.bodhakfrontend.core.analysis;

import com.example.bodhakfrontend.core.analysis.entityflag.EntityCharacteristics;
import com.example.bodhakfrontend.core.analysis.entityflag.ProjectBaselines;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.project.ProjectSnapshot;
import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import com.example.bodhakfrontend.engine.DependencyGraph;

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

    public AnalysisContext(

            ProjectSnapshot snapshot,

            DependencyGraph dependencyGraph,

            ProjectBaselines baselines,

            ProjectClassificationResult classificationResult,

            Collection<EntityCharacteristics> characteristics

    ) {
        this.snapshot = snapshot;
        this.dependencyGraph = dependencyGraph;
        this.baselines = baselines;
        this.classificationResult = classificationResult;

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
}
