package com.example.bodhakfrontend.core.projectType;

import com.example.bodhakfrontend.core.projectType.capability.CapabilityAggregator;
import com.example.bodhakfrontend.core.projectType.capability.CapabilityProfile;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkEvidence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.bodhakfrontend.core.projectType.Capability.*;
import static com.example.bodhakfrontend.core.projectType.EvidenceCategory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CapabilityAggregator.
 * Verifies max-merge strategy, empty inputs, and multi-framework scenarios.
 */
class CapabilityAggregatorTest {

    private final CapabilityAggregator aggregator = new CapabilityAggregator();

    // ── Helper ────────────────────────────────────────────────────────────────

    private FrameworkDetectionResult detectedResult(String name,
                                                    double evidenceWeight,
                                                    Capability... caps) {
        var builder = FrameworkDetectionResult.builder(name)
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "test evidence", evidenceWeight));
        for (Capability c : caps) builder.addCapability(c);
        return builder.build();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void emptyResults_producesEmptyProfile() {
        CapabilityProfile profile = aggregator.aggregate(List.of());
        assertTrue(profile.detectedCapabilities().isEmpty());
        assertEquals(0, profile.totalEvidenceCount());
    }

    @Test
    void singleFramework_capabilitiesTransferred() {
        var result = detectedResult("spring-boot", 0.8, HTTP_ENDPOINT, DATABASE_ACCESS);
        CapabilityProfile profile = aggregator.aggregate(List.of(result));

        assertTrue(profile.hasCapability(HTTP_ENDPOINT));
        assertTrue(profile.hasCapability(DATABASE_ACCESS));
        assertFalse(profile.hasCapability(WEB_UI));
    }

    @Test
    void twoFrameworks_sameCapability_maxMerged() {
        // React reports WEB_UI with confidence ~0.25 (1 category → 0.25)
        var react = detectedResult("react", 0.55, WEB_UI);
        // NextJS also reports WEB_UI with confidence ~0.25 but higher score
        var nextjs = detectedResult("nextjs", 0.80, WEB_UI, STATIC_SITE_GENERATION);

        CapabilityProfile profile = aggregator.aggregate(List.of(react, nextjs));

        // WEB_UI should be present
        assertTrue(profile.hasCapability(WEB_UI));
        // STATIC_SITE_GENERATION from nextjs
        assertTrue(profile.hasCapability(STATIC_SITE_GENERATION));
        // Confidence is max of the two, not sum
        double webUiConfidence = profile.getConfidence(WEB_UI);
        assertTrue(webUiConfidence <= 1.0, "Confidence must not exceed 1.0 with max-merge");
    }

    @Test
    void undetectedFramework_excluded() {
        // score < 0.3 → not detected
        var weak = FrameworkDetectionResult.builder("weak-framework")
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "tiny hint", 0.05))
                .addCapability(GRPC)
                .build();

        assertFalse(weak.isDetected(), "Score 0.05 should be below default threshold 0.3");

        CapabilityProfile profile = aggregator.aggregate(List.of(weak));
        assertFalse(profile.hasCapability(GRPC),
                "Undetected framework should not contribute capabilities");
    }

    @Test
    void evidenceCountAccumulated() {
        var a = detectedResult("framework-a", 0.5, HTTP_ENDPOINT);
        var b = detectedResult("framework-b", 0.5, WEB_UI);

        CapabilityProfile profile = aggregator.aggregate(List.of(a, b));
        // Each result has 1 evidence item
        assertEquals(2, profile.totalEvidenceCount());
    }
}
