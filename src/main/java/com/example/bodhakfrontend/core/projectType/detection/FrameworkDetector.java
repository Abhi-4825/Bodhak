package com.example.bodhakfrontend.core.projectType.detection;

/**
 * Contract for all framework detector plugins.
 *
 * To add support for a new framework:
 *   1. Create a class implementing this interface (or extending AbstractFrameworkDetector)
 *   2. Register it with FrameworkDetectorRegistry
 *   3. Done — no other files need to change.
 */
public interface FrameworkDetector {

    /** Unique framework identifier, e.g. "spring-boot", "react", "fastapi". */
    String frameworkName();

    /** Language this detector applies to: "java", "python", "javascript", etc. */
    String languageId();

    /**
     * Analyze the detection context and return evidence-based results.
     * Must NEVER return null — return a result with detected=false instead.
     */
    FrameworkDetectionResult detect(DetectionContext context);
}
