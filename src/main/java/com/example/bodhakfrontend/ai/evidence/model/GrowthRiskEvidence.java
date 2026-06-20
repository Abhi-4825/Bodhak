package com.example.bodhakfrontend.ai.evidence.model;



public record GrowthRiskEvidence(

        String entityName,

        double dependencyDepth,

        double propagationDepth,

        double normalizedDepth,

        double normalizedPropagation

) {}
