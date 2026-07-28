package com.example.anuviya.analyzer.growth.risk;

import com.example.anuviya.model.diagnostic.AnalysisSeverity;

public record ChokePointRisk(

        double rawCentrality,

        double normalizedCentrality,

        AnalysisSeverity severity

) {
}
