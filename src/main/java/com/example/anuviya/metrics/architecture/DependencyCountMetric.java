package com.example.anuviya.metrics.architecture;


import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.metrics.Metric;

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
