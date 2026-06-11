package com.example.bodhakfrontend.engine.Performance.Rules;



import com.example.bodhakfrontend.core.Analysis.AnalysisCategory;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisIssue;
import com.example.bodhakfrontend.core.Analysis.AnalysisSeverity;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.Metrics.DependencyDepthMetric;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeepDependencyChainRule implements PerformanceRule {

    private static final int DEFAULT_THRESHOLD = 5;

    private final DependencyDepthMetric depthMetric =
            new DependencyDepthMetric();

    @Override
    public List<AnalysisIssue> analyze(
            AnalysisContext context
    ) {

        List<AnalysisIssue> issues = new ArrayList<>();

        for (EntityInfo entity : context.getEntities()) {

            double depth =
                    depthMetric.calculate(entity, context);

            if (depth >= DEFAULT_THRESHOLD) {

                issues.add(
                        buildIssue(entity, depth)
                );
            }
        }

        return issues;
    }

    private AnalysisIssue buildIssue(
            EntityInfo entity,
            double depth
    ) {

        return new AnalysisIssue(
                "Deep Dependency Chain",
                buildDescription(entity, depth),
                determineSeverity(depth),
                AnalysisCategory.PERFORMANCE,
                List.of(entity.getEntityName()),
                Map.of(
                        "dependencyDepth", depth
                ),
                buildSuggestions(),Map.of()
        );
    }

    private String buildDescription(
            EntityInfo entity,
            double depth
    ) {

        return entity.getEntityName()
                + " has a dependency chain depth of "
                + depth
                + ", which may indicate excessive abstraction layers, "
                + "architectural fragility, and propagation overhead.";
    }

    private AnalysisSeverity determineSeverity(
            double depth
    ) {

        if (depth >= 10) {
            return AnalysisSeverity.CRITICAL;
        }

        if (depth >= 8) {
            return AnalysisSeverity.HIGH;
        }

        if (depth >= 6) {
            return AnalysisSeverity.MEDIUM;
        }

        return AnalysisSeverity.LOW;
    }

    private List<String> buildSuggestions() {

        return List.of(
                "Reduce unnecessary abstraction layers",
                "Flatten deep dependency chains",
                "Consider modular boundary simplification",
                "Reduce transitive dependency complexity"
        );
    }
}
