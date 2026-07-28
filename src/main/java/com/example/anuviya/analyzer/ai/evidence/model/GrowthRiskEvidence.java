package com.example.anuviya.analyzer.ai.evidence.model;



public record GrowthRiskEvidence(

        String entityName,

        double dependencyDepth,

        double propagationDepth,

        double normalizedDepth,

        double normalizedPropagation

) {}
