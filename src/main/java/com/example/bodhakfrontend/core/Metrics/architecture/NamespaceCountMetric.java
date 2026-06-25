package com.example.bodhakfrontend.core.Metrics.architecture;



import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.Metric;

public class NamespaceCountMetric
        implements Metric<Void> {

    @Override
    public double calculate(
            Void target,
            AnalysisContext context
    ) {

        return context.getNamespaces()
                .size();
    }
}