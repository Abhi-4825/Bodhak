package com.example.anuviya.classification.capability;

import com.example.anuviya.classification.Capability;

import java.util.*;

/**
 * Aggregated capability profile for the entire project.
 * Maps each detected Capability to a confidence score (0.0-1.0).
 *
 * Immutable once built.
 */
public final class CapabilityProfile {

    private final Map<Capability, Double> capabilities;
    private final int totalEvidenceCount;

    public CapabilityProfile(Map<Capability, Double> capabilities, int totalEvidenceCount) {
        this.capabilities = capabilities.isEmpty() 
                ? Collections.emptyMap() 
                : Collections.unmodifiableMap(new EnumMap<>(capabilities));
        this.totalEvidenceCount = totalEvidenceCount;
    }

    /** Check if a capability was detected at any confidence level. */
    public boolean hasCapability(Capability c) {
        return capabilities.containsKey(c);
    }

    /** Get confidence for a specific capability. Returns 0.0 if absent. */
    public double getConfidence(Capability c) {
        return capabilities.getOrDefault(c, 0.0);
    }

    /** Check if confidence exceeds a threshold. */
    public boolean hasCapabilityAbove(Capability c, double threshold) {
        return getConfidence(c) >= threshold;
    }

    /** All capabilities with confidence > 0. */
    public Set<Capability> detectedCapabilities() {
        return capabilities.keySet();
    }

    /** Full map (unmodifiable). */
    public Map<Capability, Double> asMap() {
        return capabilities;
    }

    public int totalEvidenceCount() {
        return totalEvidenceCount;
    }

    @Override
    public String toString() {
        return "CapabilityProfile" + capabilities;
    }
}
