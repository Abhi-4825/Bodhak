package com.example.anuviya.orchestration.incremental.engine;

import java.util.*;

public class ArtifactDependencyGraph {
    private final Map<ArtifactId, Set<ArtifactId>> adjacencyList = new HashMap<>();

    public ArtifactDependencyGraph() {
        // Initialize static dependencies: dependency -> dependents
        addDependency(ArtifactId.COMPILATION_UNIT, ArtifactId.PARSED_IR);
        addDependency(ArtifactId.SYMBOL_TABLE, ArtifactId.COMPILATION_UNIT);
        addDependency(ArtifactId.ENTITY_INDEX, ArtifactId.COMPILATION_UNIT);
        addDependency(ArtifactId.RELATIONSHIP_GRAPH, ArtifactId.ENTITY_INDEX);
        addDependency(ArtifactId.RELATIONSHIP_GRAPH, ArtifactId.SYMBOL_TABLE);
        addDependency(ArtifactId.REFERENCE_DATABASE, ArtifactId.RELATIONSHIP_GRAPH);
        addDependency(ArtifactId.NAMESPACE_INDEX, ArtifactId.ENTITY_INDEX);
        addDependency(ArtifactId.PROJECT_INFO, ArtifactId.PROJECT_ROOTS);
        addDependency(ArtifactId.PROJECT_INFO, ArtifactId.ENTITY_INDEX);
        addDependency(ArtifactId.PROJECT_SNAPSHOT, ArtifactId.PROJECT_INFO);
        addDependency(ArtifactId.PROJECT_SNAPSHOT, ArtifactId.ENTITY_INDEX);
        addDependency(ArtifactId.PROJECT_SNAPSHOT, ArtifactId.NAMESPACE_INDEX);
        addDependency(ArtifactId.CLASSIFICATION_RESULT, ArtifactId.PROJECT_INFO);
        addDependency(ArtifactId.CLASSIFICATION_RESULT, ArtifactId.PROJECT_ROOTS);
        addDependency(ArtifactId.ANALYSIS_CONTEXT, ArtifactId.PROJECT_SNAPSHOT);
        addDependency(ArtifactId.ANALYSIS_CONTEXT, ArtifactId.RELATIONSHIP_GRAPH);
        addDependency(ArtifactId.ANALYSIS_CONTEXT, ArtifactId.CLASSIFICATION_RESULT);
    }

    private void addDependency(ArtifactId dependent, ArtifactId dependency) {
        adjacencyList.computeIfAbsent(dependency, k -> new HashSet<>()).add(dependent);
    }

    public Set<ArtifactId> getDependents(ArtifactId id) {
        return adjacencyList.getOrDefault(id, Collections.emptySet());
    }
}
