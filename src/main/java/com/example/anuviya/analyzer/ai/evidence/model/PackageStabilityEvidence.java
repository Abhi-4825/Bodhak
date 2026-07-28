package com.example.anuviya.analyzer.ai.evidence.model;

public record PackageStabilityEvidence(
        String packageName,
        int fanIn,
        int fanOut,
        double instability,
        double abstractness,
        double distance
) {}
