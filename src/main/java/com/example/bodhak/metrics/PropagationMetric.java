package com.example.bodhak.metrics;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.context.GraphSnapshot;

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
