package com.example.bodhakfrontend.core.projectType.detection;

import com.example.bodhakfrontend.core.projectType.EvidenceCategory;

/**
 * A single piece of evidence supporting framework detection.
 *
 * @param category    The type of evidence source
 * @param description Human-readable description
 * @param weight      Contribution to the overall score (0.0-1.0)
 * @param source      Optional: file path or entity name where found
 */
public record FrameworkEvidence(
        EvidenceCategory category,
        String description,
        double weight,
        String source
) {
    public FrameworkEvidence(EvidenceCategory category, String description, double weight) {
        this(category, description, weight, null);
    }
}
