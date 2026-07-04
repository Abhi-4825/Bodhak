package com.example.bodhak.classification.intelligence;

public interface ScoringPolicy {
    double calculateConfidence(TechnologyDef tech, EvidenceGraph graph);
}
