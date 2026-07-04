package com.example.bodhak.analyzer.growth.risk;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.metrics.DependencyDepthMetric;
import com.example.bodhak.metrics.PropagationMetric;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.analyzer.growth.model.GrowthQuadrant;

public class ArchitecturalGrowthRiskMetric {
    private final DependencyDepthMetric
            depthMetric =
            new DependencyDepthMetric();

    private final PropagationMetric
            propagationMetric =
            new PropagationMetric();

    public ArchitecturalGrowthRisk calculate(

            EntityInfo entity,

            AnalysisContext context,

            double maxDepth,

            double maxImpact

    ){double depth =
            depthMetric.calculate(
                    entity,
                    context
            );

        double impact =
                propagationMetric.calculate(
                        entity,
                        context
                );
        double normalizedDepth = maxDepth == 0
                        ? 0
                        : (depth / maxDepth) * 10.0;

        double normalizedImpact = maxImpact == 0
                        ? 0
                        : (Math.log1p(impact)/Math.log1p(maxImpact))* 10.0;
        double score = normalizedDepth * 0.5
                        + normalizedImpact * 0.5;

        boolean highDepth = normalizedDepth >= 5;

        boolean highImpact = normalizedImpact >= 5;
        GrowthQuadrant quadrant;

        if (!highDepth && !highImpact) {

            quadrant = GrowthQuadrant.SAFE;

        }
        else if (!highDepth) {

            quadrant = GrowthQuadrant.HUB_RISK;

        }
        else if (!highImpact) {

            quadrant = GrowthQuadrant.DEPTH_RISK;

        }
        else {

            quadrant = GrowthQuadrant.CRITICAL;

        }
        return new ArchitecturalGrowthRisk(

                quadrant,

                score,

                normalizedDepth,

                normalizedImpact

        );


    }
}
