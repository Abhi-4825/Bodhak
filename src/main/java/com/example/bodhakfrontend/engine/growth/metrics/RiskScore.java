package com.example.bodhakfrontend.engine.growth.metrics;

import com.example.bodhakfrontend.core.Analysis.AnalysisSeverity;

public record RiskScore(double score, AnalysisSeverity severity,double Confidence) {
}
