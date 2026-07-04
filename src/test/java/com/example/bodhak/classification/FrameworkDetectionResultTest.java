package com.example.bodhak.classification;

import com.example.bodhak.classification.detection.FrameworkDetectionResult;
import com.example.bodhak.classification.detection.FrameworkEvidence;
import org.junit.jupiter.api.Test;

import static com.example.bodhak.classification.EvidenceCategory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FrameworkDetectionResult's builder and scoring logic.
 * Verifies: score accumulation, confidence from evidence diversity,
 * detection threshold, and capability immutability.
 */
class FrameworkDetectionResultTest {

    @Test
    void emptyBuilder_notDetected() {
        var result = FrameworkDetectionResult.builder("test-framework").build();
        assertFalse(result.isDetected(), "No evidence → score=0 → not detected");
        assertEquals(0.0, result.score());
        assertEquals(0.0, result.confidence());
        assertTrue(result.capabilities().isEmpty());
        assertTrue(result.evidence().isEmpty());
    }

    @Test
    void scoreIsAccumulatedWeightSum() {
        var result = FrameworkDetectionResult.builder("framework")
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "dep 1", 0.35))
                .addEvidence(new FrameworkEvidence(AST_ANNOTATION, "annotation", 0.20))
                .build();

        assertEquals(0.55, result.score(), 0.001);
    }

    @Test
    void scoreAboveThreshold_isDetected() {
        var result = FrameworkDetectionResult.builder("framework")
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "strong dep", 0.40))
                .build();

        assertTrue(result.isDetected(), "Score 0.40 >= default threshold 0.30");
    }

    @Test
    void scoreBelowThreshold_notDetected() {
        var result = FrameworkDetectionResult.builder("framework")
                .addEvidence(new FrameworkEvidence(FILE_STRUCTURE, "vague hint", 0.05))
                .build();

        assertFalse(result.isDetected(), "Score 0.05 < default threshold 0.30");
    }

    @Test
    void customThreshold_respected() {
        var result = FrameworkDetectionResult.builder("framework")
                .threshold(0.6)
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "dep", 0.50))
                .build();

        assertFalse(result.isDetected(), "Score 0.50 < custom threshold 0.60");
    }

    @Test
    void confidenceIncreasesWithEvidenceDiversity() {
        // 1 category → confidence = 1/4 = 0.25
        var oneCategory = FrameworkDetectionResult.builder("fw")
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "dep1", 0.4))
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "dep2", 0.1))
                .build();

        // 4 categories → confidence = 4/4 = 1.0
        var fourCategories = FrameworkDetectionResult.builder("fw")
                .addEvidence(new FrameworkEvidence(DEPENDENCY,     "dep",    0.4))
                .addEvidence(new FrameworkEvidence(AST_ANNOTATION, "ann",    0.2))
                .addEvidence(new FrameworkEvidence(CONFIGURATION,  "config", 0.1))
                .addEvidence(new FrameworkEvidence(FILE_STRUCTURE, "file",   0.05))
                .build();

        assertTrue(fourCategories.confidence() > oneCategory.confidence(),
                "More diverse evidence → higher confidence");
        assertEquals(0.25, oneCategory.confidence(), 0.001);
        assertEquals(1.00, fourCategories.confidence(), 0.001);
    }

    @Test
    void confidenceNeverExceedsOne() {
        // 10 different categories would give 10/4 = 2.5 → should be capped at 1.0
        var result = FrameworkDetectionResult.builder("fw")
                .addEvidence(new FrameworkEvidence(DEPENDENCY,     "d", 0.1))
                .addEvidence(new FrameworkEvidence(AST_ANNOTATION, "a", 0.1))
                .addEvidence(new FrameworkEvidence(CONFIGURATION,  "c", 0.1))
                .addEvidence(new FrameworkEvidence(FILE_STRUCTURE, "f", 0.1))
                .addEvidence(new FrameworkEvidence(ENTITY_TAG,     "e", 0.1))
                .addEvidence(new FrameworkEvidence(BUILD_FILE,     "b", 0.1))
                .addEvidence(new FrameworkEvidence(AST_PATTERN,    "p", 0.1))
                .build();

        assertTrue(result.confidence() <= 1.0, "Confidence must be capped at 1.0");
    }

    @Test
    void frameworkName_returnedCorrectly() {
        var result = FrameworkDetectionResult.builder("spring-boot").build();
        assertEquals("spring-boot", result.frameworkName());
    }

    @Test
    void capabilities_areImmutable() {
        var result = FrameworkDetectionResult.builder("fw")
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "dep", 0.5))
                .addCapability(Capability.HTTP_ENDPOINT)
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> result.capabilities().add(Capability.WEB_UI),
                "capabilities() must return an unmodifiable set");
    }

    @Test
    void evidence_isImmutable() {
        var result = FrameworkDetectionResult.builder("fw")
                .addEvidence(new FrameworkEvidence(DEPENDENCY, "dep", 0.5))
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> result.evidence().add(new FrameworkEvidence(DEPENDENCY, "x", 0.1)),
                "evidence() must return an unmodifiable list");
    }
}
