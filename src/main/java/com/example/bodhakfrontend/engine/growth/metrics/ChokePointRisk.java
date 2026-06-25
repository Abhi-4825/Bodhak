package com.example.bodhakfrontend.engine.growth.metrics;

import com.example.bodhakfrontend.core.analysis.AnalysisSeverity;

public record ChokePointRisk(

        double rawCentrality,

        double normalizedCentrality,

        AnalysisSeverity severity

) {
}