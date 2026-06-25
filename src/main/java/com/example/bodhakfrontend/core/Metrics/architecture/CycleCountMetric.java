package com.example.bodhakfrontend.core.Metrics.architecture;


import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.Metric;

public class CycleCountMetric
        implements Metric<Void> {

    @Override
    public double calculate(
            Void target,
            AnalysisContext context
    ) {

        return context.getDependencyGraph()
                .snapshot()
                .circularGroups()
                .size();
    }
}
