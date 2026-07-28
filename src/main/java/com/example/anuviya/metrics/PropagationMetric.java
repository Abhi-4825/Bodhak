package com.example.anuviya.metrics;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.context.DependencyGraph;
import com.example.anuviya.context.GraphSnapshot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PropagationMetric implements Metric<EntityInfo> {

    @Override
    public double calculate(
            EntityInfo entity,
            AnalysisContext context
    ) {

        DependencyGraph graph =
                context.getDependencyGraph();

        GraphSnapshot snapshot =
                graph.snapshot();

        Map<String, Set<String>> reverseDeps =
                snapshot.reverseDependencies();

        Set<String> visited =
                new HashSet<>();

        calculatePropagation(
                entity.getEntityName(),
                reverseDeps,
                visited
        );

        // remove self
        visited.remove(
                entity.getEntityName()
        );

        return visited.size();
    }

    private void calculatePropagation(
            String current,
            Map<String, Set<String>> reverseDeps,
            Set<String> visited
    ) {

        if (!visited.add(current)) {
            return;
        }

        for (String dependent :
                reverseDeps.getOrDefault(
                        current,
                        Set.of()
                )) {

            calculatePropagation(
                    dependent,
                    reverseDeps,
                    visited
            );
        }
    }
}
