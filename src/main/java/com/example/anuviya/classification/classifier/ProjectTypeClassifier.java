package com.example.anuviya.classification.classifier;

import com.example.anuviya.classification.capability.CapabilityProfile;
import com.example.anuviya.classification.detection.FrameworkDetectionResult;

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
