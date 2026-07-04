package com.example.bodhak.metrics.architecture;



import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.metrics.Metric;

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
