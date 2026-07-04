package com.example.bodhak.analyzer.policy;

import com.example.bodhak.model.reference.SemanticReference;

/**
 * Strategy interface to calculate analyzer-specific weights for semantic references.
 */
public interface ReferenceScoringPolicy {
    double calculateWeight(SemanticReference reference);
}
