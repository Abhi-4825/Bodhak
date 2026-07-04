package com.example.bodhak.analyzer.growth.risk;

import com.example.bodhak.model.diagnostic.AnalysisSeverity;

public class ArchitecturalChokePointRiskCalculator {

    public ChokePointRisk calculate(
            double centrality,
            double maxCentrality
    ) {

        double normalizedCentrality =
                maxCentrality == 0
                        ? 0
                        : (centrality / maxCentrality) * 10.0;

        AnalysisSeverity severity;

        if (normalizedCentrality >= 8) {

            severity = AnalysisSeverity.HIGH;

        }
        else if (normalizedCentrality >= 5) {

            severity = AnalysisSeverity.MEDIUM;

        }
        else {

            severity = AnalysisSeverity.LOW;
        }

        return new ChokePointRisk(

                centrality,

                normalizedCentrality,

                severity

        );
    }
}
