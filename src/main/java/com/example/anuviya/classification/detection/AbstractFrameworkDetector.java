package com.example.anuviya.classification.detection;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.classification.EvidenceCategory;

import java.util.List;

/**
 * Base class for framework detectors.
 * Provides convenience methods for common evidence-gathering patterns.
 *
 * Subclasses implement {@link #doDetect(DetectionContext)} and use
 * the helper methods to build evidence incrementally.
 */
public abstract class AbstractFrameworkDetector implements FrameworkDetector {

    @Override
    public final FrameworkDetectionResult detect(DetectionContext context) {
        return doDetect(context);
    }

    /**
     * Subclasses implement detection logic here.
     * Use the builder pattern: {@code FrameworkDetectionResult.builder(frameworkName())}
     */
    protected abstract FrameworkDetectionResult doDetect(DetectionContext context);

    // ── Convenience: entity filtering ─────────────────────────

    protected List<EntityInfo> entities(DetectionContext ctx) {
        return ctx.entitiesForLanguage(languageId());
    }

    protected List<EntityInfo> entitiesWithDecorator(DetectionContext ctx, String decorator) {
        return ctx.entitiesWithDecorator(languageId(), decorator);
    }

    protected List<EntityInfo> entitiesWithTag(DetectionContext ctx, String tag) {
        return ctx.entitiesWithTag(languageId(), tag);
    }

    // ── Convenience: evidence creation ────────────────────────

    protected FrameworkEvidence dependencyEvidence(String desc, double weight) {
        return new FrameworkEvidence(EvidenceCategory.DEPENDENCY, desc, weight);
    }

    protected FrameworkEvidence buildFileEvidence(String desc, double weight) {
        return new FrameworkEvidence(EvidenceCategory.BUILD_FILE, desc, weight);
    }

    protected FrameworkEvidence astAnnotationEvidence(String desc, double weight, String source) {
        return new FrameworkEvidence(EvidenceCategory.AST_ANNOTATION, desc, weight, source);
    }

    protected FrameworkEvidence astPatternEvidence(String desc, double weight) {
        return new FrameworkEvidence(EvidenceCategory.AST_PATTERN, desc, weight);
    }

    protected FrameworkEvidence configEvidence(String desc, double weight) {
        return new FrameworkEvidence(EvidenceCategory.CONFIGURATION, desc, weight);
    }

    protected FrameworkEvidence fileStructureEvidence(String desc, double weight) {
        return new FrameworkEvidence(EvidenceCategory.FILE_STRUCTURE, desc, weight);
    }

    protected FrameworkEvidence entityTagEvidence(String desc, double weight) {
        return new FrameworkEvidence(EvidenceCategory.ENTITY_TAG, desc, weight);
    }

    // ── Convenience: common build file checks ─────────────────

    protected boolean pomContains(DetectionContext ctx, String text) {
        return ctx.fileContains("pom.xml", text);
    }
    protected boolean buildGradleContains(DetectionContext ctx, String text) {
        return ctx.fileContains("build.gradle", text)
                || ctx.fileContains("build.gradle.kts", text);
    }
    protected boolean packageJsonContains(DetectionContext ctx, String text) {
        return ctx.fileContains("package.json", text);
    }

    protected boolean requirementsContains(DetectionContext ctx, String text) {
        return ctx.fileContains("requirements.txt", text)
                || ctx.fileContains("pyproject.toml", text)
                || ctx.fileContains("setup.py", text)
                || ctx.fileContains("Pipfile", text);
    }
}
