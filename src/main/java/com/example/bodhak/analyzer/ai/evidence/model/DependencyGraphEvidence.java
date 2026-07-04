package com.example.bodhak.analyzer.ai.evidence.model;

public record DependencyGraphEvidence(

        int totalEntities,

        int totalEdges,

        int circularGroups,

        int largestCycleSize,

        int maxFanIn,

        int maxFanOut,

        double averageFanOut

) {}
