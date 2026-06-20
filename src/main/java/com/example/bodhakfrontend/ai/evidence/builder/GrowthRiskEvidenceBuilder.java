package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.ai.evidence.model.GrowthRiskEvidence;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.DependencyDepthMetric;
import com.example.bodhakfrontend.core.Metrics.PropagationMetric;
import com.example.bodhakfrontend.engine.growth.metrics.ArchitecturalGrowthRiskMetric;

import java.util.Comparator;
import java.util.List;

public class GrowthRiskEvidenceBuilder {
    public List<GrowthRiskEvidence> buildGrowthRisks(
            AnalysisContext context
    ){

        DependencyDepthMetric depthMetric =
                new DependencyDepthMetric();

        PropagationMetric propagationMetric =
                new PropagationMetric();

        double maxDepth =
                context.getEntities()
                        .stream()
                        .mapToDouble(
                                e -> depthMetric.calculate(
                                        e,
                                        context
                                )
                        )
                        .max()
                        .orElse(1);

        double maxPropagation =
                context.getEntities()
                        .stream()
                        .mapToDouble(
                                e -> propagationMetric.calculate(
                                        e,
                                        context
                                )
                        )
                        .max()
                        .orElse(1);


        return context.getEntities()
                .stream()
                .map(entity -> {

                    double depth =
                            depthMetric.calculate(
                                    entity,
                                    context
                            );

                    double propagation =
                            propagationMetric.calculate(
                                    entity,
                                    context
                            );

                    double normalizedDepth =
                            maxDepth == 0
                                    ? 0
                                    : (depth / maxDepth) * 10.0;

                    double normalizedPropagation =
                            maxPropagation == 0
                                    ? 0
                                    : (
                                    Math.log1p(propagation)
                                    /
                                            Math.log1p(maxPropagation)
                            ) * 10.0;

                    return new GrowthRiskEvidence(

                            entity.getEntityName(),

                            depth,

                            propagation,

                            normalizedDepth,

                            normalizedPropagation
                    );
                })
                .sorted(
                        Comparator.comparingDouble(
                                        GrowthRiskEvidence::normalizedDepth
                                )
                                .thenComparingDouble(
                                        GrowthRiskEvidence::normalizedPropagation
                                )
                                .reversed()
                )
                .limit(10)
                .toList();
    }
}
