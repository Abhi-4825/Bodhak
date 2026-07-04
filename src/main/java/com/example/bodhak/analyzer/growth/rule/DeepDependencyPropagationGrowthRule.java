package com.example.bodhak.analyzer.growth.rule;

import com.example.bodhak.model.diagnostic.AnalysisCategory;
import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.diagnostic.AnalysisIssue;
import com.example.bodhak.model.diagnostic.AnalysisSeverity;
import com.example.bodhak.metrics.DependencyDepthMetric;
import com.example.bodhak.metrics.PropagationMetric;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.analyzer.growth.algorithm.ArticulationPointAnalyzer;
import com.example.bodhak.analyzer.growth.algorithm.SccAnalyzer;
import com.example.bodhak.analyzer.growth.risk.ArchitecturalGrowthRisk;
import com.example.bodhak.analyzer.growth.risk.ArchitecturalGrowthRiskMetric;
import com.example.bodhak.analyzer.growth.model.BridgeNode;
import com.example.bodhak.analyzer.growth.model.GrowthQuadrant;
import com.example.bodhak.analyzer.growth.model.SccCluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DeepDependencyPropagationGrowthRule implements GrowthRule {
    private final DependencyDepthMetric depthMetric = new DependencyDepthMetric();

    private final PropagationMetric propagationMetric = new PropagationMetric();

    private final ArchitecturalGrowthRiskMetric riskMetric = new ArchitecturalGrowthRiskMetric();
    @Override
    public List<AnalysisIssue> analyze(AnalysisContext context) {
        SccAnalyzer analyzer =
                new SccAnalyzer();

        List<SccCluster> clusters =
                analyzer.analyze(context);

        System.out.println(
                "\n========== SCC ANALYSIS =========="
        );

        for (SccCluster cluster :
                clusters.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        SccCluster::size
                                ).reversed()
                        )
                        .toList()) {
            if(cluster.size()>1){

            System.out.println(
                    "\nCluster: "
                            + cluster.clusterId()
            );

            System.out.println(
                    "Size: "
                            + cluster.size()
            );

            System.out.println(
                    "Members:"
            );

            cluster.nodes()
                    .stream()
                    .sorted()
                    .forEach(
                            node ->
                                    System.out.println(
                                            "  - " + node
                                    )
                    );
        }}
        ArticulationPointAnalyzer articulationPointAnalyzer =
                new ArticulationPointAnalyzer();

        List<BridgeNode> bridges =
                articulationPointAnalyzer.analyze(context);

        System.out.println(
                "\n========== BRIDGE ANALYSIS =========="
        );

        bridges.stream()
                .sorted(
                        Comparator.comparingInt(
                                BridgeNode::impactScore
                        ).reversed()
                )
                .forEach(node -> {

                    System.out.println(
                            node.entity()
                                    + " impact="
                                    + node.impactScore()
                    );
                });
        List<AnalysisIssue> issues=new ArrayList<>();
        double maxDepth=context.getEntities().stream().mapToDouble(entity->depthMetric.calculate(entity,context)).max().orElse(1);
        double maxImpact=context.getEntities().stream().mapToDouble(entity->propagationMetric.calculate(entity,context)).max().orElse(1);
        for(EntityInfo entity:context.getEntities()){
            ArchitecturalGrowthRisk risk =
                    riskMetric.calculate(
                            entity,
                            context,
                            maxDepth,
                            maxImpact
                    );
            if (risk.quadrant()
                    == GrowthQuadrant.SAFE
                    ||
                    risk.quadrant()
                            == GrowthQuadrant.HUB_RISK) {

                continue;
            }
            double depth=depthMetric.calculate(entity,context);
            double impact=propagationMetric.calculate(entity,context);
            issues.add(
                   createIssue(entity,depth,impact,risk));
        }
        return issues;
    }
    private AnalysisIssue createIssue(

            EntityInfo entity,

            double depth,

            double propagation,

            ArchitecturalGrowthRisk risk

    ) {
        return new AnalysisIssue("Deep Dependency Growth Risk",entity.getEntityName()
                +
                " participates in a deep dependency structure. "
                +
                "As the project grows, changes may propagate "
                +
                "through multiple layers of the system.",determineSeverity(risk.quadrant()), AnalysisCategory.SCALABILITY,
                List.of(entity.getEntityName()), Map.of(
                "riskScore",
                risk.score(),

                "depth",
              depth,

                "propagation",
              propagation
        ),   List.of(
                "Reduce dependency chain depth.",
                "Introduce clearer architectural boundaries.",
                "Break large dependency flows into smaller modules.",
                "Minimize change propagation paths."
        ), Map.of("depth", depth,
                        "propagation", propagation,

                "riskScore",
                risk.score(),

                "normalizedDepth",
                risk.normalizedDepth(),

                "normalizedImpact",
                risk.normalizedImpact()
        ));
    }




        private AnalysisSeverity determineSeverity(
            GrowthQuadrant quadrant
    ) {

        return switch (quadrant) {
            case DEPTH_RISK ->
                    AnalysisSeverity.MEDIUM;
            case CRITICAL ->
                    AnalysisSeverity.HIGH;
            default ->
                    AnalysisSeverity.LOW;
        };
    }
}
