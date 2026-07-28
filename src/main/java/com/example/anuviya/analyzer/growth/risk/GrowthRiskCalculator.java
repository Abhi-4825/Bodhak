package com.example.anuviya.analyzer.growth.risk;

import com.example.anuviya.model.diagnostic.AnalysisSeverity;

/** we are considering 20+ fanIn as a risky in any project , above that we will fully trust the score*/

public class GrowthRiskCalculator {

    public RiskScore calculateDependencyHubRisk(
            double dependencyShareScore,
            double normalizedFanIn,
            double fanIn
    ) {

        double confidence =
                Math.min(
                        fanIn / 20.0,
                        1.0
                );

        double baseScore =
                dependencyShareScore * 0.4
                        + normalizedFanIn * 0.6;

//        System.out.println(
//                "\n[GrowthRiskCalculator]"
//        );
//
//        System.out.println(
//                "dependencyShareScore = "
//                        + dependencyShareScore
//        );
//
//        System.out.println(
//                "normalizedFanIn = "
//                        + normalizedFanIn
//        );
//
//        System.out.println(
//                "fanIn = "
//                        + fanIn
//        );
//
//        System.out.println(
//                "confidence = "
//                        + confidence
//        );


//
//        System.out.println(
//                "finalScore = "
//                        + baseScore
//        );

        return new RiskScore(
                baseScore,
                determineSeverity(baseScore),
                confidence
        );
    }

    private AnalysisSeverity determineSeverity(
            double score
    ) {

        if (score >= 8) {
            return AnalysisSeverity.CRITICAL;
        }

        if (score >= 6) {
            return AnalysisSeverity.HIGH;
        }

        if (score >= 3) {
            return AnalysisSeverity.MEDIUM;
        }

        return AnalysisSeverity.LOW;
    }
}
