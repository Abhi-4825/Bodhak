package com.example.anuviya.metrics;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.context.DependencyGraph;
import com.example.anuviya.context.GraphSnapshot;

import java.util.Set;

public class FanOutMetric implements Metric<EntityInfo> {

    @Override
    public double calculate(
            EntityInfo entity,
            AnalysisContext context
    ) {

        DependencyGraph graph = context.getDependencyGraph();

        GraphSnapshot snapshot = graph.snapshot();

        Set<String> outgoing =
                snapshot.globalDependencies()
                        .getOrDefault(
                                entity.getEntityName(),
                                Set.of()
                        );

        return outgoing.size();
    }
}
