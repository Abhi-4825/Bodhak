package com.example.anuviya.classification.detection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Central registry for all framework detector plugins.
 * Follows the same pattern as FrontendRegistry.
 *
 * Thread-safe via CopyOnWriteArrayList (registrations are rare,
 * reads are frequent during analysis).
 */
public final class FrameworkDetectorRegistry {

    private final List<FrameworkDetector> detectors = new CopyOnWriteArrayList<>();

    /** Register a single detector. */
    public void register(FrameworkDetector detector) {
        Objects.requireNonNull(detector, "detector");
        detectors.add(detector);
    }

    /** Register multiple detectors at once. */
    public void registerAll(FrameworkDetector... detectors) {
        for (FrameworkDetector d : detectors) register(d);
    }

    /** All registered detectors (unmodifiable snapshot). */
    public List<FrameworkDetector> allDetectors() {
        return List.copyOf(detectors);
    }

    /** Detectors for a specific language. */
    public List<FrameworkDetector> detectorsForLanguage(String languageId) {
        return detectors.stream()
                .filter(d -> d.languageId().equalsIgnoreCase(languageId))
                .toList();
    }

    /** All registered framework names. */
    public Set<String> registeredFrameworks() {
        return detectors.stream()
                .map(FrameworkDetector::frameworkName)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Run all detectors against the context, return only detected results. */
    public List<FrameworkDetectionResult> detectAll(DetectionContext context) {
        return detectors.stream()
                .map(d -> d.detect(context))
                .filter(FrameworkDetectionResult::isDetected)
                .toList();
    }
}
