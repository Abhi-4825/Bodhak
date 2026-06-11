package com.example.bodhakfrontend.core.projectType.detectors.java;

import com.example.bodhakfrontend.core.projectType.detection.*;

import static com.example.bodhakfrontend.core.projectType.Capability.*;

/**
 * Detects standard Java CLI applications.
 */
public final class JavaCliDetector extends AbstractFrameworkDetector {

    @Override
    public String frameworkName() { return "java-cli"; }

    @Override
    public String languageId() { return "java"; }

    @Override
    protected FrameworkDetectionResult doDetect(DetectionContext ctx) {
        var builder = FrameworkDetectionResult.builder(frameworkName());

        // 1. Dependency Evidence
        if (pomContains(ctx, "commons-cli") || buildGradleContains(ctx, "commons-cli")) {
            builder.addEvidence(dependencyEvidence("Apache Commons CLI found", 0.40));
            builder.addCapability(CLI);
        }
        
        if (pomContains(ctx, "picocli") || buildGradleContains(ctx, "picocli")) {
            builder.addEvidence(dependencyEvidence("Picocli found", 0.40));
            builder.addCapability(CLI);
        }

        if (pomContains(ctx, "jcommander") || buildGradleContains(ctx, "jcommander")) {
            builder.addEvidence(dependencyEvidence("JCommander found", 0.40));
            builder.addCapability(CLI);
        }

        // 2. Member Evidence (main method)
        boolean hasMainMethod = ctx.anyMemberNameContains("java", "main");
        if (hasMainMethod) {
            builder.addEvidence(new FrameworkEvidence(
                    com.example.bodhakfrontend.core.projectType.EvidenceCategory.AST_PATTERN,
                    "main method found",
                    0.30));
            builder.addCapability(CLI);
        }

        // 3. Fallback configuration evidence (e.g. jar plugin mainClass)
        if (pomContains(ctx, "<mainClass>")) {
            builder.addEvidence(dependencyEvidence("Maven <mainClass> configuration found", 0.30));
            builder.addCapability(CLI);
        }

        return builder.build();
    }
}
