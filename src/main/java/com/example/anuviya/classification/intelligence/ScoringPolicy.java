package com.example.anuviya.classification.intelligence;

public interface ScoringPolicy {
    double calculateConfidence(TechnologyDef tech, EvidenceGraph graph);
}
