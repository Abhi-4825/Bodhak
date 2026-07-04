package com.example.bodhak.orchestration.incremental.engine;

import java.util.*;

public class DependencyImpactAnalyzer {
    private final ArtifactDependencyGraph graph;

    public DependencyImpactAnalyzer(ArtifactDependencyGraph graph) {
        this.graph = graph;
    }

    public Set<ArtifactId> analyze(Set<ArtifactId> directlyStale) {
        Set<ArtifactId> transitiveClosure = new HashSet<>();
        Queue<ArtifactId> queue = new LinkedList<>(directlyStale);

        while (!queue.isEmpty()) {
            ArtifactId current = queue.poll();
            if (transitiveClosure.add(current)) {
                queue.addAll(graph.getDependents(current));
            }
        }

        return transitiveClosure;
    }
}
