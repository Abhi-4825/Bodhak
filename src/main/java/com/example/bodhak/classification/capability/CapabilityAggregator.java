package com.example.bodhak.classification.capability;

import com.example.bodhak.classification.Capability;
import com.example.bodhak.classification.detection.FrameworkDetectionResult;

import java.util.*;

/**
 * Merges multiple FrameworkDetectionResults into a single CapabilityProfile.
 *
 * When multiple frameworks report the same capability, the highest
 * confidence wins (max-merge strategy, not additive — avoids inflation).
 */
public final class CapabilityAggregator {

    /**
     * Aggregate all detected framework results into a unified capability profile.
     */
    public CapabilityProfile aggregate(List<FrameworkDetectionResult> results) {
        Map<Capability, Double> merged = new EnumMap<>(Capability.class);
        int totalEvidence = 0;

        for (FrameworkDetectionResult result : results) {
            if (!result.isDetected()) continue;

            totalEvidence += result.evidence().size();

            for (Capability cap : result.capabilities()) {
                double score = result.confidence();
                merged.merge(cap, score, Math::max);
            }
        }

        return new CapabilityProfile(merged, totalEvidence);
    }
}
