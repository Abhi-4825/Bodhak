
package com.example.bodhakfrontend.core.Metrics.architecture;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.Metric;

public class ClassCountMetric
        implements Metric<Void> {

    @Override
    public double calculate(
            Void target,
            AnalysisContext context
    ) {

        return context.getProjectInfo()
                .getTotalEntities();
    }
}