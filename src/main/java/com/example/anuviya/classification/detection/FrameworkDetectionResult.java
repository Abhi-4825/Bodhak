package com.example.anuviya.classification.detection;

import com.example.anuviya.classification.Capability;

import java.util.*;

/**
 * Output of a single FrameworkDetector.
 * Immutable — built via the nested Builder.
 */
public final class FrameworkDetectionResult {

    private final String frameworkName;
    private final double score;
    private final double confidence;
    private final Set<Capability> capabilities;
    private final List<FrameworkEvidence> evidence;
    private final boolean detected;

    private FrameworkDetectionResult(Builder b) {
        this.frameworkName = b.frameworkName;
        this.evidence      = List.copyOf(b.evidence);
        this.capabilities  = b.capabilities.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(b.capabilities));
        this.score         = b.evidence.stream()
                .mapToDouble(FrameworkEvidence::weight)
                .sum();
        this.confidence    = computeConfidence(b.evidence);
        this.detected      = this.score >= b.detectionThreshold;
    }

    private static double computeConfidence(List<FrameworkEvidence> evidence) {
        if (evidence.isEmpty()) return 0.0;
        long categories = evidence.stream()
                .map(FrameworkEvidence::category)
                .distinct().count();
        return Math.min(1.0, categories / 4.0);
    }

    public String frameworkName()            { return frameworkName; }
    public double score()                    { return score;         }
    public double confidence()               { return confidence;    }
    public Set<Capability> capabilities()    { return capabilities;  }
    public List<FrameworkEvidence> evidence() { return evidence;     }
    public boolean isDetected()              { return detected;      }

    public static Builder builder(String frameworkName) {
        return new Builder(frameworkName);
    }

    public static final class Builder {
        private final String frameworkName;
        private final List<FrameworkEvidence> evidence = new ArrayList<>();
        private final Set<Capability> capabilities = EnumSet.noneOf(Capability.class);
        private double detectionThreshold = 0.3;

        private Builder(String frameworkName) {
            this.frameworkName = frameworkName;
        }

        public Builder addEvidence(FrameworkEvidence e) {
            evidence.add(e);
            return this;
        }

        public Builder addCapability(Capability c) {
            capabilities.add(c);
            return this;
        }

        public Builder addCapabilities(Capability... caps) {
            capabilities.addAll(Arrays.asList(caps));
            return this;
        }

        public Builder threshold(double t) {
            this.detectionThreshold = t;
            return this;
        }

        public FrameworkDetectionResult build() {
            return new FrameworkDetectionResult(this);
        }
    }
}
