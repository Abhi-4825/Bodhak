package com.example.bodhak.analyzer.growth.risk;

import com.example.bodhak.model.diagnostic.AnalysisSeverity;

public record RiskScore(double score, AnalysisSeverity severity,double Confidence) {
}
