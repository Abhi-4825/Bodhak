package com.example.bodhak.classification.intelligence;

import java.util.List;
import java.util.ArrayList;

public class WeightedScoringPolicy implements ScoringPolicy {
    @Override
    public double calculateConfidence(TechnologyDef tech, EvidenceGraph graph) {
        if (tech.getEvidence().isEmpty()) return 0.0;

        double score = 0.0;
        double totalWeight = 0.0;

        for (TechnologyDef.EvidenceRule rule : tech.getEvidence()) {
            EvidenceType type;
            try {
                type = EvidenceType.valueOf(rule.getType());
            } catch (IllegalArgumentException e) {
                continue;
            }

            double weight = getWeightForType(type);
            totalWeight += weight;

            List<String> patterns = new ArrayList<>();
            if (rule.getPattern() != null && !rule.getPattern().isBlank()) {
                patterns.add(rule.getPattern());
            }
            if (rule.getAliases() != null) {
                patterns.addAll(rule.getAliases());
            }

            if (graph.hasEvidenceMatching(type, patterns)) {
                score += weight;
            }
        }

        if (totalWeight == 0.0) return 0.0;
        return Math.min(1.0, score / totalWeight);
    }

    private double getWeightForType(EvidenceType type) {
        return switch (type) {
            case BUILD_DEPENDENCY -> 1.0;
            case BUILD_PLUGIN -> 0.8;
            case ANNOTATION_REFERENCE -> 0.6;
            case METHOD_REFERENCE, METHOD_DECLARATION -> 0.4;
            case IMPORT_REFERENCE -> 0.3;
            case PROJECT_ROOT -> 0.5;
            case FILE_STRUCTURE -> 0.5;
        };
    }
}
