package com.example.bodhakfrontend.core.projectType.engine;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.projectType.capability.CapabilityAggregator;
import com.example.bodhakfrontend.core.projectType.capability.CapabilityProfile;
import com.example.bodhakfrontend.core.projectType.classification.CapabilityBasedClassifier;
import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import com.example.bodhakfrontend.core.projectType.classification.ProjectTypeClassifier;
import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectorRegistry;

import java.util.List;

/**
 * Top-level orchestrator for the project type detection pipeline.
 *
 * Pipeline stages:
 *   1. Build DetectionContext from AnalysisContext
 *   2. Run all registered FrameworkDetectors -> List<FrameworkDetectionResult>
 *   3. Aggregate capabilities from all detected frameworks -> CapabilityProfile
 *   4. Classify project types from capabilities -> ProjectClassificationResult
 *
 * This class has NO knowledge of specific frameworks.
 * Adding a new framework only requires registering a new FrameworkDetector.
 */
public final class ProjectTypeAnalyzer {

    private final FrameworkDetectorRegistry detectorRegistry;
    private final CapabilityAggregator capabilityAggregator;
    private final ProjectTypeClassifier classifier;

    /**
     * Full-control constructor.
     */
    public ProjectTypeAnalyzer(
            FrameworkDetectorRegistry detectorRegistry,
            CapabilityAggregator capabilityAggregator,
            ProjectTypeClassifier classifier) {
        this.detectorRegistry     = detectorRegistry;
        this.capabilityAggregator = capabilityAggregator;
        this.classifier           = classifier;
    }

    /**
     * Convenience constructor with default aggregator and classifier.
     */
    public ProjectTypeAnalyzer(FrameworkDetectorRegistry detectorRegistry) {
        this(detectorRegistry, new CapabilityAggregator(), new CapabilityBasedClassifier());
    }

    /**
     * Execute the full pipeline.
     */
    public ProjectClassificationResult analyze(ProjectInfo projectInfo, List<EntityInfo> entityInfos) {
        // Stage 1: Build enriched context
        DetectionContext detectionContext = new DetectionContext(entityInfos,projectInfo);
        // Stage 2: Run all framework detectors
        List<FrameworkDetectionResult> detectedFrameworks =
                detectorRegistry.detectAll(detectionContext);
        // Stage 3: Aggregate capabilities
        CapabilityProfile profile =
                capabilityAggregator.aggregate(detectedFrameworks);
        // Stage 4: Classify project types
        return classifier.classify(profile, detectedFrameworks);
    }
}
