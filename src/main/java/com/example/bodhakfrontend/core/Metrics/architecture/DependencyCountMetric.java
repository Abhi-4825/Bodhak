package com.example.bodhakfrontend.core.Metrics.architecture;


import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.Metric;

import java.util.Set;

public class DependencyCountMetric
        implements Metric<Void> {

    @Override
    public double calculate(
            Void target,
            AnalysisContext context
    ) {

        return context.getDependencyGraph()
                .snapshot()
                .globalDependencies()
                .values()
                .stream()
                .mapToInt(Set::size)
                .sum();
    }
}
