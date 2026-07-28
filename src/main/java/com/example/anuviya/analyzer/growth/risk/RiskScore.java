package com.example.anuviya.analyzer.growth.risk;

import com.example.anuviya.model.diagnostic.AnalysisSeverity;

public record RiskScore(double score, AnalysisSeverity severity,double Confidence) {
}
