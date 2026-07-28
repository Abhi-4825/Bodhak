package com.example.anuviya.ui.analysisReport.state;

import com.example.anuviya.classification.detection.FrameworkDetectionResult;
import com.example.anuviya.context.AnalysisContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.List;

public class FrameworkDetectionState implements AnalysisReportSection {

    public record FrameworkEntry(
            String name,
            String icon,
            double score,          // raw sum of FrameworkEvidence weights — unbounded
            double confidence,     // distinct evidence categories / 4, capped at 1.0
            boolean detected,      // score >= detectionThreshold
            List<String> capabilities,
            List<FrameworkEvidenceEntry> evidence
    ) {}

    public record FrameworkEvidenceEntry(
            String category,       // EvidenceCategory name, e.g. "DEPENDENCY"
            String description,
            double weight,
            String source          // nullable — file path or entity name
    ) {}

    private final ObservableList<FrameworkEntry> detectedFrameworks = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        detectedFrameworks.clear();
        if (context == null) return;

        var classificationResult = context.getClassificationResult();
        if (classificationResult == null || classificationResult.detectedFrameworks() == null) return;

        // NOTE: no longer filtering by isDetected() — the inspector now shows both
        // detected and below-threshold frameworks (with a status badge distinguishing
        // them), since "we looked at this and it didn't clear the bar" is itself
        // useful information for the user to see.
        classificationResult.detectedFrameworks().stream()
                .sorted(Comparator.comparingDouble(FrameworkDetectionResult::confidence).reversed())
                .forEach(fw -> {
                    List<String> capabilityNames = fw.capabilities().stream()
                            .map(Enum::name)
                            .sorted()
                            .toList();

                    List<FrameworkEvidenceEntry> evidenceEntries = fw.evidence().stream()
                            .map(e -> new FrameworkEvidenceEntry(
                                    e.category().name(),
                                    e.description(),
                                    e.weight(),
                                    e.source()))
                            .toList();

                    detectedFrameworks.add(new FrameworkEntry(
                            fw.frameworkName(),
                            getIconForFramework(fw.frameworkName()),
                            fw.score(),
                            fw.confidence(),
                            fw.isDetected(),
                            capabilityNames,
                            evidenceEntries
                    ));
                });
    }

    private String getIconForFramework(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("spring")) return "🍃";
        if (lower.contains("react")) return "⚛️";
        if (lower.contains("hibernate")) return "🗃️";
        if (lower.contains("lombok")) return "🌶️";
        if (lower.contains("junit") || lower.contains("test")) return "✅";
        if (lower.contains("fastapi")) return "⚡";
        return "📦";
    }

    public ObservableList<FrameworkEntry> getDetectedFrameworks() {
        return detectedFrameworks;
    }
}