package com.example.bodhak.analyzer.growth.risk;

import com.example.bodhak.model.diagnostic.AnalysisSeverity;

public record ChokePointRisk(

        double rawCentrality,

        double normalizedCentrality,

        AnalysisSeverity severity

) {
}
