package com.example.bodhakfrontend.core.Metrics;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
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
