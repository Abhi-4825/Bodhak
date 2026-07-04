package com.example.bodhak.metrics;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.context.GraphSnapshot;

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
