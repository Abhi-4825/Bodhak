package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.classification.ProjectType;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.classification.detection.FrameworkEvidence;
import com.example.bodhak.classification.intelligence.DetectedTechnology;
import com.example.bodhak.classification.intelligence.Evidence;
import com.example.bodhak.context.AnalysisContext;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.Map;

public class ProjectClassificationState implements AnalysisReportSection {

    private final StringProperty primaryClassification = new SimpleStringProperty("Unknown");
    private final DoubleProperty primaryConfidence = new SimpleDoubleProperty(0.0);
    private final DoubleProperty inferenceTimeMs = new SimpleDoubleProperty(0.0);
    private final IntegerProperty totalCapabilityEvidenceCount = new SimpleIntegerProperty(0);

    public record ClassificationEntry(String name, double confidence) {}
    public record CapabilityEntry(String name, String category, double confidence) {}
    public record MissingRequirementEntry(String name, String category) {}
    public record EvidenceEntry(String category, String description, double weight) {}

    public record TechnologyEntry(
            String name,
            String family,         // e.g. "framework", "library", "database", "messaging"
            String category,
            String version,        // nullable
            double confidence,
            java.util.List<TechnologyEvidenceEntry> matchedEvidence,
            Map<String, String> metadata,
            java.util.List<String> providedCapabilities
    ) {}

    public record TechnologyEvidenceEntry(
            String type,           // EvidenceType name, e.g. "BUILD_DEPENDENCY"
            String pattern,        // rule pattern that matched, e.g. "spring-boot-starter-web"
            String value,          // actual value found, e.g. "2.7.1"
            String sourceFile,     // nullable
            String description
    ) {}

    private final ObservableList<ClassificationEntry> detectedTypes = FXCollections.observableArrayList();
    private final ObservableList<CapabilityEntry> capabilities = FXCollections.observableArrayList();
    private final ObservableList<MissingRequirementEntry> missingRequirements = FXCollections.observableArrayList();
    private final ObservableList<EvidenceEntry> evidence = FXCollections.observableArrayList();
    private final ObservableList<TechnologyEntry> detectedTechnologies = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        if (context == null) return;

        ProjectClassificationResult result = context.getClassificationResult();
        if (result == null) return;

        inferenceTimeMs.set(result.inferenceTimeMs());

        if (result.primaryType() != null) {
            primaryClassification.set(formatEnumName(result.primaryType().name()));
            double conf = result.confidenceFor(result.primaryType());
            primaryConfidence.set(conf);
        }

        // Full type confidence distribution — every ProjectType the classifier scored.
        detectedTypes.clear();
        if (result.projectTypes() != null) {
            result.projectTypes().entrySet().stream()
                    .sorted(Map.Entry.<ProjectType, Double>comparingByValue().reversed())
                    .forEach(entry -> detectedTypes.add(new ClassificationEntry(
                            formatEnumName(entry.getKey().name()),
                            entry.getValue()
                    )));
        }

        // Capabilities, sorted by confidence, plus the total evidence count backing them.
        capabilities.clear();
        missingRequirements.clear();
        totalCapabilityEvidenceCount.set(0);
        if (result.capabilityProfile() != null) {
            var profile = result.capabilityProfile();
            profile.detectedCapabilities().stream()
                    .sorted(Comparator.comparingDouble(profile::getConfidence).reversed())
                    .forEach(cap -> capabilities.add(new CapabilityEntry(
                            formatEnumName(cap.name()),
                            formatEnumName(cap.getCategory().name()),
                            profile.getConfidence(cap)
                    )));
            totalCapabilityEvidenceCount.set(profile.totalEvidenceCount());

            // Compute missing boosting capabilities for the primary rule
            if (result.primaryRule() != null) {
                result.primaryRule().boostingCapabilities().stream()
                        .filter(cap -> !profile.hasCapability(cap))
                        .forEach(cap -> missingRequirements.add(new MissingRequirementEntry(
                                formatEnumName(cap.name()),
                                formatEnumName(cap.getCategory().name())
                        )));
                
                // Also list required capabilities if they were somehow missed (e.g. forced archetype)
                result.primaryRule().requiredCapabilities().stream()
                        .filter(cap -> !profile.hasCapability(cap))
                        .forEach(cap -> missingRequirements.add(new MissingRequirementEntry(
                                formatEnumName(cap.name()) + " (Required)",
                                formatEnumName(cap.getCategory().name())
                        )));
            }
        }

        // Classification-level evidence ledger, sourced from every framework's evidence trail.
        evidence.clear();
        if (result.detectedFrameworks() != null) {
            result.detectedFrameworks().stream()
                    .filter(fw -> fw.evidence() != null)
                    .flatMap(fw -> fw.evidence().stream())
                    .sorted(Comparator.comparingDouble(FrameworkEvidence::weight).reversed())
                    .forEach(fe -> evidence.add(new EvidenceEntry(
                            formatEnumName(fe.category().name()),
                            fe.description(),
                            fe.weight()
                    )));
        }

        // Technology intelligence — full provenance: family/category/version/metadata
        // and the exact matched-evidence trail (pattern -> value -> source file).
        detectedTechnologies.clear();
        if (result.detectedTechnologies() != null) {
            result.detectedTechnologies().stream()
                    .sorted(Comparator.comparingDouble(DetectedTechnology::confidence).reversed())
                    .forEach(tech -> {
                        var matchedEvidence = tech.matchedEvidence() == null
                                ? java.util.List.<TechnologyEvidenceEntry>of()
                                : tech.matchedEvidence().stream()
                                .map(this::toTechnologyEvidenceEntry)
                                .toList();

                        var metadata = tech.metadata() == null ? Map.<String, String>of() : tech.metadata();
                        var providedCapabilities = tech.capabilities() == null ? java.util.List.<String>of() : tech.capabilities();

                        detectedTechnologies.add(new TechnologyEntry(
                                tech.displayName(),
                                tech.family(),
                                tech.category(),
                                tech.version(),
                                tech.confidence(),
                                matchedEvidence,
                                metadata,
                                providedCapabilities
                        ));
                    });
        }
    }

    private TechnologyEvidenceEntry toTechnologyEvidenceEntry(Evidence e) {
        return new TechnologyEvidenceEntry(
                e.type() != null ? e.type().name() : null,
                e.pattern(),
                e.value(),
                e.sourceFile(),
                e.description()
        );
    }

    private String formatEnumName(String name) {
        if (name == null) return "";
        String[] words = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public StringProperty primaryClassificationProperty() {
        return primaryClassification;
    }

    public DoubleProperty primaryConfidenceProperty() {
        return primaryConfidence;
    }

    public DoubleProperty inferenceTimeMsProperty() {
        return inferenceTimeMs;
    }

    public IntegerProperty totalCapabilityEvidenceCountProperty() {
        return totalCapabilityEvidenceCount;
    }

    public ObservableList<ClassificationEntry> getDetectedTypes() {
        return detectedTypes;
    }

    public ObservableList<CapabilityEntry> getCapabilities() {
        return capabilities;
    }

    public ObservableList<EvidenceEntry> getEvidence() {
        return evidence;
    }

    public ObservableList<TechnologyEntry> getDetectedTechnologies() {
        return detectedTechnologies;
    }

    public ObservableList<MissingRequirementEntry> getMissingRequirements() {
        return missingRequirements;
    }
}