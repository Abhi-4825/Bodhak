package com.example.anuviya.metrics.architecture;



import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.metrics.Metric;

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
