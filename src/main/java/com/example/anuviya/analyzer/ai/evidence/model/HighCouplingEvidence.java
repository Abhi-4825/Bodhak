package com.example.anuviya.analyzer.ai.evidence.model;

public record HighCouplingEvidence(

        String entityName,

        int fanIn,

        int fanOut,

        int couplingScore

) {
}
