package com.example.bodhakfrontend.engine.growth.rules;

import com.example.bodhakfrontend.core.Analysis.AnalysisCategory;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisIssue;
import com.example.bodhakfrontend.engine.growth.analysis.BetweennessCentralityAnalyzer;
import com.example.bodhakfrontend.engine.growth.metrics.ArchitecturalChokePointRiskCalculator;
import com.example.bodhakfrontend.engine.growth.metrics.ChokePointRisk;
import com.example.bodhakfrontend.engine.growth.model.CentralityNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArchitecturalChokePointGrowthRule
        implements GrowthRule {

    private final BetweennessCentralityAnalyzer analyzer =
            new BetweennessCentralityAnalyzer();

    private final ArchitecturalChokePointRiskCalculator riskCalculator =
            new ArchitecturalChokePointRiskCalculator();

    @Override
    public List<AnalysisIssue> analyze(
            AnalysisContext context
    ) {

        List<AnalysisIssue> issues =
                new ArrayList<>();

        List<CentralityNode> nodes =
                analyzer.analyze(context);

        if (nodes.isEmpty()) {
            return issues;
        }

        double maxCentrality =
                nodes.stream()
                        .mapToDouble(
                                CentralityNode::score
                        )
                        .max()
                        .orElse(1);

        int rank = 1;

        for (CentralityNode node : nodes) {

            ChokePointRisk risk =
                    riskCalculator.calculate(
                            node.score(),
                            maxCentrality
                    );

            if (risk.normalizedCentrality() < 5) {
                continue;
            }

            issues.add(
                    createIssue(
                            node,
                            risk,
                            rank
                    )
            );

            rank++;
        }

        return issues;
    }

    private AnalysisIssue createIssue(

            CentralityNode node,

            ChokePointRisk risk,

            int rank

    ) {

        return new AnalysisIssue(

                "Architectural Choke Point",

                node.entity()
                        + " sits on a large number of architectural "
                        + "communication paths. As the project grows, "
                        + "this component may become a coordination "
                        + "bottleneck and increase change propagation cost.",

                risk.severity(),

                AnalysisCategory.SCALABILITY,

                List.of(
                        node.entity()
                ),

                Map.of(

                        "betweennessCentrality",
                        risk.rawCentrality(),

                        "normalizedCentrality",
                        risk.normalizedCentrality()

                ),

                List.of(

                        "Reduce architectural responsibilities.",

                        "Split functionality into smaller services.",

                        "Introduce clearer subsystem boundaries.",

                        "Avoid routing multiple workflows through a single component."

                ),

                Map.of(

                        "riskType",
                        "ARCHITECTURAL_CHOKE_POINT",

                        "riskLevel",
                        risk.severity().name(),

                        "growthFactor",
                        "ARCHITECTURAL_TRAFFIC",

                        "centralityRank",
                        rank,

                        "normalizedCentrality",
                        risk.normalizedCentrality(),

                        "rawCentrality",
                        risk.rawCentrality()

                )
        );
    }


}