package com.example.bodhak.metrics;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.context.GraphSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Calculates the dependency depth of an entity using linear-time memoized traversal
 * to prevent exponential backtracking overhead and infinite loops on cyclic graphs.
 */
public class DependencyDepthMetric implements Metric<EntityInfo> {

    @Override
    public double calculate(
            EntityInfo entity,
            AnalysisContext context
    ) {
        DependencyGraph graph = context.getDependencyGraph();
        GraphSnapshot snapshot = graph.snapshot();
        Map<String, Set<String>> globalDeps = snapshot.globalDependencies();

        Map<String, Double> memo = new HashMap<>();
        return calculateDepth(
                entity.getEntityName(),
                globalDeps,
                new HashSet<>(),
                memo
        );
    }

    private double calculateDepth(
            String current,
            Map<String, Set<String>> graph,
            Set<String> visiting,
            Map<String, Double> memo
    ) {
        if (memo.containsKey(current)) {
            return memo.get(current);
        }

        // Cycle detection: if current is already in active visiting stack, return 0 to break cycle
        if (!visiting.add(current)) {
            return 0;
        }

        Set<String> dependencies = graph.getOrDefault(current, Set.of());
        if (dependencies.isEmpty()) {
            visiting.remove(current);
            memo.put(current, 0.0);
            return 0;
        }

        double maxDepth = 0;
        for (String dependency : dependencies) {
            double depth = calculateDepth(
                    dependency,
                    graph,
                    visiting,
                    memo
            );
            maxDepth = Math.max(maxDepth, depth);
        }

        visiting.remove(current);
        double result = 1 + maxDepth;
        memo.put(current, result);
        return result;
    }
}
