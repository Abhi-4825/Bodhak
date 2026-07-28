package com.example.anuviya.analyzer.policy;

import com.example.anuviya.model.reference.SemanticReference;

/**
 * Strategy interface to calculate analyzer-specific weights for semantic references.
 */
public interface ReferenceScoringPolicy {
    double calculateWeight(SemanticReference reference);
}
