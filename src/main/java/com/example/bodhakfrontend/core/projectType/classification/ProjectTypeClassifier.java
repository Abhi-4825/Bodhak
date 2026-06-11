package com.example.bodhakfrontend.core.projectType.classification;

import com.example.bodhakfrontend.core.projectType.capability.CapabilityProfile;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult;

import java.util.List;

/**
 * Strategy interface for project type classification.
 * Operates purely on capabilities — never on framework names.
 */
public interface ProjectTypeClassifier {

    ProjectClassificationResult classify(
            CapabilityProfile profile,
            List<FrameworkDetectionResult> detectedFrameworks
    );
}
