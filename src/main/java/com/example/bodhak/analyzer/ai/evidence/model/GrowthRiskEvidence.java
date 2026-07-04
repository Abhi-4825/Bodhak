package com.example.bodhak.analyzer.ai.evidence.model;



public record GrowthRiskEvidence(

        String entityName,

        double dependencyDepth,

        double propagationDepth,

        double normalizedDepth,

        double normalizedPropagation

) {}
