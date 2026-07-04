
package com.example.bodhak.metrics.architecture;
import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.metrics.Metric;

public class ClassCountMetric
        implements Metric<Void> {

    @Override
    public double calculate(
            Void target,
            AnalysisContext context
    ) {

        var projectInfo = context.getProjectInfo();
        return projectInfo.totalEntities();
    }
}
