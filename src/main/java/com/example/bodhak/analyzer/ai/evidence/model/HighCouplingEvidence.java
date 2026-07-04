package com.example.bodhak.analyzer.ai.evidence.model;

public record HighCouplingEvidence(

        String entityName,

        int fanIn,

        int fanOut,

        int couplingScore

) {
}
