package com.example.anuviya.classification.detector.java;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.classification.detection.*;

import static com.example.anuviya.classification.Capability.*;

import java.util.List;

/**
 * Detects Java Desktop applications (JavaFX, Swing).
 */
public final class JavaDesktopDetector extends AbstractFrameworkDetector {

    @Override
    public String frameworkName() { return "java-desktop"; }

    @Override
    public String languageId() { return "java"; }

    @Override
    protected FrameworkDetectionResult doDetect(DetectionContext ctx) {
        var builder = FrameworkDetectionResult.builder(frameworkName());

        // 1. Dependency Evidence
        if (pomContains(ctx, "org.openjfx") || pomContains(ctx, "javafx-controls")) {
            builder.addEvidence(dependencyEvidence("JavaFX dependency found in pom.xml", 0.40));
            builder.addCapability(DESKTOP_UI);
        }
        
        if (buildGradleContains(ctx, "org.openjfx") || buildGradleContains(ctx, "javafx")) {
            builder.addEvidence(dependencyEvidence("JavaFX dependency found in build.gradle", 0.40));
            builder.addCapability(DESKTOP_UI);
        }
        
        if (pomContains(ctx, "javafx-maven-plugin")) {
            builder.addEvidence(dependencyEvidence("javafx-maven-plugin found in pom.xml", 0.20));
            builder.addCapability(DESKTOP_UI);
        }

        // 2. AST Decorator Evidence
        List<EntityInfo> fxmlEntities = entitiesWithDecorator(ctx, "FXML");
        if (!fxmlEntities.isEmpty()) {
            builder.addEvidence(astAnnotationEvidence(
                    "@FXML found on " + fxmlEntities.size() + " entities",
                    0.30,
                    fxmlEntities.getFirst().getEntityName()));
            builder.addCapability(DESKTOP_UI);
        }
        
        List<EntityInfo> appEntities = entitiesWithDecorator(ctx, "Application");
        if (!appEntities.isEmpty()) {
            // Note: 'Application' is a common name, so this is weak evidence alone unless combined with JavaFX
            builder.addEvidence(astAnnotationEvidence(
                    "Class extending/annotated with Application found",
                    0.10,
                    appEntities.getFirst().getEntityName()));
            builder.addCapability(DESKTOP_UI);
        }

        return builder.build();
    }
}
