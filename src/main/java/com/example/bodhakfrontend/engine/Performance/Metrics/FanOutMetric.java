package com.example.bodhakfrontend.engine.Performance.Metrics;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;
import com.example.bodhakfrontend.engine.GraphSnapshot;

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
