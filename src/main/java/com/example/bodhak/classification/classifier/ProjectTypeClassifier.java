package com.example.bodhak.classification.classifier;

import com.example.bodhak.classification.capability.CapabilityProfile;
import com.example.bodhak.classification.detection.FrameworkDetectionResult;

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
