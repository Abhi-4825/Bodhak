package com.example.bodhak.ui.dashboard.architecture.analysis;



import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.metrics.architecture.ArchitectureMetrics;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureDashboardData;

public class ArchitectureDashboardDataBuilder {

    public ArchitectureDashboardData build(
            AnalysisContext context
    ) {

        return new ArchitectureDashboardData(

                ArchitectureMetrics.HEALTH_SCORE
                        .calculate(null, context),

                (int) ArchitectureMetrics.CYCLE_COUNT
                        .calculate(null, context),

                (int) ArchitectureMetrics.DEPENDENCY_COUNT
                        .calculate(null, context),

                (int) ArchitectureMetrics.NAMESPACE_COUNT
                        .calculate(null, context),

                (int) ArchitectureMetrics.CLASS_COUNT
                        .calculate(null, context)
        );
    }
}
