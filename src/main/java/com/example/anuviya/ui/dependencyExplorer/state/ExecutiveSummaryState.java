package com.example.anuviya.ui.dependencyExplorer.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.GraphSnapshot;
import javafx.beans.property.*;

import java.util.*;

public class ExecutiveSummaryState {

    private final IntegerProperty entityCount = new SimpleIntegerProperty(0);
    private final IntegerProperty semanticReferenceCount = new SimpleIntegerProperty(0);
    private final IntegerProperty circularDependenciesCount = new SimpleIntegerProperty(0);
    private final DoubleProperty averageFanOut = new SimpleDoubleProperty(0.0);
    private final IntegerProperty maxDepth = new SimpleIntegerProperty(0);
    private final StringProperty healthRating = new SimpleStringProperty("Excellent");

    public void update(AnalysisContext context) {
        if (context == null) return;

        entityCount.set(context.getEntities().size());
        
        if (context.getReferenceDatabase() != null) {
            semanticReferenceCount.set(context.getReferenceDatabase().getAllReferences().size());
        } else {
            semanticReferenceCount.set(0);
        }

        if (context.getDependencyGraph() != null) {
            GraphSnapshot snapshot = context.getDependencyGraph().snapshot();
            if (snapshot != null) {
                circularDependenciesCount.set(snapshot.circularGroups().size());

                // Calculate Avg Fan-Out
                var deps = snapshot.globalDependencies();
                int total = deps.size();
                if (total > 0) {
                    double sum = deps.values().stream().mapToDouble(Set::size).sum();
                    averageFanOut.set(sum / total);
                } else {
                    averageFanOut.set(0.0);
                }

                // Calculate Max Depth
                maxDepth.set(calculateMaxDepth(deps));

                // Health rating logic
                int cycles = snapshot.circularGroups().size();
                if (cycles > 10) {
                    healthRating.set("Critical");
                } else if (cycles > 2) {
                    healthRating.set("Warning");
                } else {
                    healthRating.set("Excellent");
                }
            }
        }
    }

    private int calculateMaxDepth(Map<String, Set<String>> graph) {
        Map<String, Integer> memo = new HashMap<>();
        Set<String> visiting = new HashSet<>();
        int max = 0;
        for (String node : graph.keySet()) {
            max = Math.max(max, dfsDepth(node, graph, visiting, memo));
        }
        return max;
    }

    private int dfsDepth(String node, Map<String, Set<String>> graph, Set<String> visiting, Map<String, Integer> memo) {
        if (memo.containsKey(node)) return memo.get(node);
        if (visiting.contains(node)) return 0;

        visiting.add(node);
        int maxSubDepth = 0;
        Set<String> deps = graph.get(node);
        if (deps != null) {
            for (String dep : deps) {
                maxSubDepth = Math.max(maxSubDepth, dfsDepth(dep, graph, visiting, memo));
            }
        }
        visiting.remove(node);
        int depth = 1 + maxSubDepth;
        memo.put(node, depth);
        return depth;
    }

    public ReadOnlyIntegerProperty entityCountProperty() { return entityCount; }
    public ReadOnlyIntegerProperty semanticReferenceCountProperty() { return semanticReferenceCount; }
    public ReadOnlyIntegerProperty circularDependenciesCountProperty() { return circularDependenciesCount; }
    public ReadOnlyDoubleProperty averageFanOutProperty() { return averageFanOut; }
    public ReadOnlyIntegerProperty maxDepthProperty() { return maxDepth; }
    public ReadOnlyStringProperty healthRatingProperty() { return healthRating; }
}
