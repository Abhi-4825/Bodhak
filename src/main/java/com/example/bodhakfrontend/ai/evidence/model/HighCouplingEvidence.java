package com.example.bodhakfrontend.ai.evidence.model;

public record HighCouplingEvidence(

        String entityName,

        int fanIn,

        int fanOut,

        int couplingScore

) {
}
