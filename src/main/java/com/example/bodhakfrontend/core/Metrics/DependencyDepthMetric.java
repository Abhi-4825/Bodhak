package com.example.bodhakfrontend.core.Metrics;



import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;
import com.example.bodhakfrontend.engine.GraphSnapshot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyDepthMetric implements Metric<EntityInfo> {

    @Override
    public double calculate(
            EntityInfo entity,
            AnalysisContext context
    ) {

        DependencyGraph graph = context.getDependencyGraph();

        GraphSnapshot snapshot = graph.snapshot();

        Map<String, Set<String>> globalDeps =
                snapshot.globalDependencies();

        return calculateDepth(
                entity.getEntityName(),
                globalDeps,
                new HashSet<>()
        );
    }

    private double calculateDepth(
            String current,
            Map<String, Set<String>> graph,
            Set<String> visited
    ) {

        if (!visited.add(current)) {
            return 0;
        }

        Set<String> dependencies =
                graph.getOrDefault(current, Set.of());

        if (dependencies.isEmpty()) {
            return 0;
        }

        double maxDepth = 0;

        for (String dependency : dependencies) {

            double depth = calculateDepth(
                    dependency,
                    graph,
                    new HashSet<>(visited)
            );

            maxDepth = Math.max(maxDepth, depth);
        }

        return 1 + maxDepth;
    }
}
