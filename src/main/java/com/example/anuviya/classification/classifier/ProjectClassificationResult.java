package com.example.anuviya.classification.classifier;

import com.example.anuviya.classification.ProjectType;
import com.example.anuviya.classification.capability.CapabilityProfile;
import com.example.anuviya.classification.detection.FrameworkDetectionResult;

import com.example.anuviya.classification.intelligence.DetectedTechnology;

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
        List<FrameworkDetectionResult> detectedFrameworks,
        
        /** All detected technologies including their provenance and metadata. */
        List<DetectedTechnology> detectedTechnologies,
        
        /** The primary classification rule that was matched. */
        ClassificationRule primaryRule,
        
        /** The total time spent in inference. */
        double inferenceTimeMs
) {
    public ProjectClassificationResult(Map<ProjectType, Double> projectTypes, ProjectType primaryType, CapabilityProfile capabilityProfile, List<FrameworkDetectionResult> detectedFrameworks) {
        this(projectTypes, primaryType, capabilityProfile, detectedFrameworks, Collections.emptyList(), null, 0.0);
    }
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
