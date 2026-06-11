package com.example.bodhakfrontend.engine.growth.rules;

import com.example.bodhakfrontend.core.Analysis.AnalysisCategory;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisIssue;
import com.example.bodhakfrontend.core.Analysis.AnalysisSeverity;
import com.example.bodhakfrontend.core.Metrics.FanInMetric;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.engine.growth.analysis.BetweennessCentralityAnalyzer;
import com.example.bodhakfrontend.engine.growth.metrics.GrowthRiskCalculator;
import com.example.bodhakfrontend.engine.growth.metrics.RiskScore;
import com.example.bodhakfrontend.engine.growth.model.CentralityNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A Dependency Hub defect is when one module knows too much and does too much
 *  — fine today, but as the codebase grows,
 * it becomes the single most fragile and costly piece of your entire system.
 * for this module i will use the fan-In factor -(used by how many modules)
 *
 * **/

public class DependencyHubGrowthRule
        implements GrowthRule {



    private final FanInMetric fanInMetric = new FanInMetric();
    private final GrowthRiskCalculator riskCalculator = new GrowthRiskCalculator();

    @Override
    public List<AnalysisIssue> analyze(
            AnalysisContext context
    ) {
        double maxFanIn =
                context.getEntities()
                        .stream()
                        .mapToDouble(
                                entity ->
                                        fanInMetric.calculate(
                                                entity,
                                                context
                                        )
                        )
                        .max()
                        .orElse(1);

        List<AnalysisIssue> issues =
                new ArrayList<>();

        int totalEntities =
                context.getEntities().size();

        if (totalEntities == 0) {
            return issues;
        }


     // test
        BetweennessCentralityAnalyzer analyzer =
                new BetweennessCentralityAnalyzer();

        List<CentralityNode> nodes =
                analyzer.analyze(context);

        System.out.println(
                "\n========== BETWEENNESS CENTRALITY =========="
        );

        nodes.stream()
                .limit(20)
                .forEach(node -> {

                    System.out.println(
                            node.entity()
                                    + " score="
                                    + node.score()
                    );
                });


        for (EntityInfo entity :
                context.getEntities()) {

            double fanIn =
                    fanInMetric.calculate(
                            entity,
                            context
                    );

            double dependencyShare =
                    fanIn / totalEntities;
            double dependencyShareScore =
                    dependencyShare * 10.0;

            double normalizedFanIn =
                    (fanIn / maxFanIn) * 10.0;
            RiskScore riskScore =
                    riskCalculator
                            .calculateDependencyHubRisk(
                                    dependencyShareScore,
                                    normalizedFanIn,fanIn
                            );

  // test



//            ArchitecturalGrowthRisk risk=architecturalGrowthRiskMetric.calculate(entity,context,maxDepth,maxImpact);
//            System.out.println(
//                    entity.getEntityName()
//                            + " -> "
//                            + risk.quadrant()
//                            + " score="
//                            + risk.score()
//            );




//            System.out.println(
//                    "\n========== GROWTH DEBUG =========="
//            );
//
//            System.out.println(
//                    "Entity: " + entity.getEntityName()
//            );
//
//            System.out.println(
//                    "FanIn: " + fanIn
//            );
//
//            System.out.println(
//                    "Dependency Share: "
//                            + String.format(
//                            "%.2f%%",
//                            dependencyShare * 100
//                    )
//            );
//
//            System.out.println(
//                    "Dependency Share Score: "
//                            + String.format(
//                            "%.2f",
//                            dependencyShareScore
//                    )
//            );
//
//            System.out.println(
//                    "Normalized FanIn: "
//                            + String.format(
//                            "%.2f",
//                            normalizedFanIn
//                    )
//            );
//
//            System.out.println(
//                    "Scale Factor: "
//                            + String.format(
//                            "%.2f",
//                            Math.min(fanIn / 20.0, 1.0)
//                    )
//            );
//
//            System.out.println(
//                    "Risk Score: "
//                            + String.format(
//                            "%.2f",
//                            riskScore.score()
//                    )
//            );
//
//            System.out.println(
//                    "Severity: "
//                            + riskScore.severity()
//            );
//
//            System.out.println(
//                    "=================================="
//            );
            if (riskScore.severity()
                    == AnalysisSeverity.LOW) {

                continue;
            }


                issues.add(
                        createIssue(
                                entity,
                                fanIn,
                                dependencyShare,
                                riskScore
                        )
                );
            }


        return issues;
    }

    private AnalysisIssue createIssue(
            EntityInfo entity,
            double fanIn,
            double dependencyShare,
            RiskScore score
    ) {

        return new AnalysisIssue(
                "Dependency Hub Growth Risk",

                entity.getEntityName()
                        + " is depended upon by "
                        + (int) fanIn
                        + " entities. As the project grows, "
                        + "this component may become a "
                        + "coordination bottleneck.",

                score.severity(),

                AnalysisCategory.SCALABILITY,

                List.of(
                        entity.getEntityName()
                ),

                Map.of(
                        "fanIn", fanIn,
                        "dependencyShare", dependencyShare * 100,
                        "growthRiskScore", score.score(),
                        "confidence", score.Confidence()
                ),
                List.of(
                        "Split responsibilities",
                        "Reduce centralization",
                        "Introduce clearer module boundaries"
                ),

                Map.of(
                        "growthFactor", "PROJECT_SIZE",
                        "riskType", "DEPENDENCY_HUB",
                        "riskLevel", score.severity().name(),
                        "confidenceLevel",
                        getConfidenceLevel(
                                score.Confidence()
                        )
                )


        );
    }
    private String getConfidenceLevel(
            double confidence
    ) {

        if (confidence >= 0.8)
            return "HIGH";

        if (confidence >= 0.4)
            return "MEDIUM";

        return "LOW";
    }
}
