package com.example.bodhakfrontend.engine.growth.metrics;

import com.example.bodhakfrontend.core.Analysis.AnalysisSeverity;

public record ChokePointRisk(

        double rawCentrality,

        double normalizedCentrality,

        AnalysisSeverity severity

) {
}