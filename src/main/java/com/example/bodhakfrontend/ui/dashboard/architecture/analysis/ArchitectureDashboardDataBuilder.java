package com.example.bodhakfrontend.ui.dashboard.architecture.analysis;



import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.architecture.ArchitectureMetrics;
import com.example.bodhakfrontend.ui.dashboard.architecture.model.ArchitectureDashboardData;

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