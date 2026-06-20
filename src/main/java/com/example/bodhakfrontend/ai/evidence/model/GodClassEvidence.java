package com.example.bodhakfrontend.ai.evidence.model;

public record GodClassEvidence(

        String entityName,

        int linesOfCode,

        int methodCount,

        int fieldCount,

        int dependencyCount

) {
}
