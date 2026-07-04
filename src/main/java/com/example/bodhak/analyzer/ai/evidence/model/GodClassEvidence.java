package com.example.bodhak.analyzer.ai.evidence.model;

public record GodClassEvidence(

        String entityName,

        int linesOfCode,

        int methodCount,

        int fieldCount,

        int dependencyCount

) {
}
