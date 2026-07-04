package com.example.bodhak.analyzer.ai.evidence.model.architectur;

public record ArchitectureSummaryEvidence(

        int totalEntities,

        int healthyEntities,

        int entitiesWithWarnings,

        int godClassCount,

        int highlyCoupledCount,

        int circularEntityCount,

        int circularGroupCount

) {
}
