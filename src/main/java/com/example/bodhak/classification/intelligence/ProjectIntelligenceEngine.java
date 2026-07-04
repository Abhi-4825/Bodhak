package com.example.bodhak.classification.intelligence;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.project.ProjectModel;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.classification.ProjectType;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.classification.classifier.CapabilityBasedClassifier;
import com.example.bodhak.classification.capability.CapabilityProfile;
import com.example.bodhak.classification.detection.FrameworkDetectionResult;
import com.example.bodhak.classification.detection.FrameworkEvidence;
import com.example.bodhak.classification.EvidenceCategory;

import java.util.*;

public class ProjectIntelligenceEngine {
    private final RegistryManager registryManager = new RegistryManager();
    private final EvidenceCollector collector = new EvidenceCollector();
    private final TechnologyResolver technologyResolver = new TechnologyResolver();
    private final CapabilityResolver capabilityResolver = new CapabilityResolver();
    private final ProjectArchetypeResolver archetypeResolver = new ProjectArchetypeResolver();
    private final CapabilityBasedClassifier classifier = new CapabilityBasedClassifier();

    public ProjectClassificationResult analyze(AnalysisContext context, ProjectModel projectModel) {
        long start = System.nanoTime();
        List<TechnologyDef> registry = registryManager.getTechnologies();
        EvidenceGraph graph = collector.collect(context, projectModel, registry);
        TechnologyCatalog catalog = technologyResolver.resolve(registry, graph, projectModel);
        CapabilityProfile profile = capabilityResolver.resolve(catalog);
        Set<String> archetypes = archetypeResolver.resolve(context.getProjectInfo());

        List<FrameworkDetectionResult> compatibilityFrameworks = new ArrayList<>();
        for (DetectedTechnology tech : catalog.detectedTechnologies()) {
            compatibilityFrameworks.add(toFrameworkDetectionResult(tech));
        }

        ProjectClassificationResult baseResult = classifier.classify(profile, compatibilityFrameworks);
        long end = System.nanoTime();
        double durationMs = (end - start) / 1_000_000.0;

        return new ProjectClassificationResult(
            baseResult.projectTypes(),
            baseResult.primaryType(),
            profile,
            compatibilityFrameworks,
            catalog.detectedTechnologies(),
            baseResult.primaryRule(),
            durationMs
        );
    }

    private FrameworkDetectionResult toFrameworkDetectionResult(DetectedTechnology tech) {
        var builder = FrameworkDetectionResult.builder(tech.displayName());
        builder.threshold(0.25);
        for (Evidence e : tech.matchedEvidence()) {
            builder.addEvidence(new FrameworkEvidence(
                mapEvidenceTypeToCategory(e.type()),
                e.description(),
                0.2, // standard weight
                e.sourceFile()
            ));
        }
        for (String cap : tech.capabilities()) {
            try {
                builder.addCapability(com.example.bodhak.classification.Capability.valueOf(cap));
            } catch (Exception ignored) {}
        }
        return builder.build();
    }

    private EvidenceCategory mapEvidenceTypeToCategory(EvidenceType type) {
        return switch (type) {
            case BUILD_DEPENDENCY -> EvidenceCategory.DEPENDENCY;
            case BUILD_PLUGIN -> EvidenceCategory.BUILD_FILE;
            case ANNOTATION_REFERENCE -> EvidenceCategory.AST_ANNOTATION;
            case METHOD_REFERENCE, IMPORT_REFERENCE, METHOD_DECLARATION -> EvidenceCategory.AST_PATTERN;
            case FILE_STRUCTURE, PROJECT_ROOT -> EvidenceCategory.FILE_STRUCTURE;
        };
    }
}
