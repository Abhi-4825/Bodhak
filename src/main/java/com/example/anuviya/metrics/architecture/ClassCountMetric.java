
package com.example.anuviya.metrics.architecture;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.metrics.Metric;

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
