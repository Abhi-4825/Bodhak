package com.example.bodhakfrontend.core.projectType.classification;

import com.example.bodhakfrontend.core.projectType.ProjectType;
import com.example.bodhakfrontend.core.projectType.capability.CapabilityProfile;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult;

import java.util.*;

/**
 * Final output of the project type detection pipeline.
 *
 * Supports MULTIPLE project types simultaneously.
 * Example: Spring Boot + React -> { REST_API, WEB_APP }
 */
public record ProjectClassificationResult(
        /** Detected project types with their confidence scores. */
        Map<ProjectType, Double> projectTypes,

        /** The primary (highest confidence) project type. */
        ProjectType primaryType,

        /** Full capability profile that drove the classification. */
        CapabilityProfile capabilityProfile,

        /** All detected frameworks with their evidence. */
        List<FrameworkDetectionResult> detectedFrameworks
) {
    /** Convenience: all project types detected above a minimum confidence. */
    public Set<ProjectType> types() {
        return projectTypes.keySet();
    }

    /** Convenience: check if a specific type was detected. */
    public boolean hasType(ProjectType type) {
        return projectTypes.containsKey(type);
    }

    /** Convenience: get confidence for a specific type. */
    public double confidenceFor(ProjectType type) {
        return projectTypes.getOrDefault(type, 0.0);
    }
}
