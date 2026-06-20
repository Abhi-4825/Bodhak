package com.example.bodhakfrontend.ai.evidence.model;

public record DependencyGraphEvidence(

        int totalEntities,

        int totalEdges,

        int circularGroups,

        int largestCycleSize,

        int maxFanIn,

        int maxFanOut,

        double averageFanOut

) {}
