package com.example.bodhak.classification.classifier;

import com.example.bodhak.classification.Capability;
import com.example.bodhak.classification.ProjectType;
import com.example.bodhak.classification.capability.CapabilityProfile;
import com.example.bodhak.classification.detection.FrameworkDetectionResult;

import java.util.*;

import static com.example.bodhak.classification.Capability.*;
import static com.example.bodhak.classification.ProjectType.*;

/**
 * Default classifier implementation that uses declarative rules.
 *
 * Rules are prioritized — higher priority rules take precedence when
 * there are conflicts, but multiple non-conflicting types can coexist.
 */
public final class CapabilityBasedClassifier implements ProjectTypeClassifier {

    private final List<ClassificationRule> rules;

    public CapabilityBasedClassifier() {
        this.rules = buildDefaultRules();
    }

    /** Allow custom rules (for testing or specialization). */
    public CapabilityBasedClassifier(List<ClassificationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public ProjectClassificationResult classify(
            CapabilityProfile profile,
            List<FrameworkDetectionResult> detectedFrameworks) {

        Map<ProjectType, Double> typeScores = new EnumMap<>(ProjectType.class);
        ClassificationRule bestRule = null;
        double bestScore = 0.0;

        // Evaluate all rules, collect scores
        for (ClassificationRule rule : rules) {
            double score = rule.evaluate(profile);
            if (score > 0) {
                typeScores.merge(rule.projectType(), score, Math::max);
                if (score > bestScore) {
                    bestScore = score;
                    bestRule = rule;
                }
            }
        }

        // If nothing matched, it's UNKNOWN
        if (typeScores.isEmpty()) {
            typeScores.put(UNKNOWN, 1.0);
        }

        // Primary type = highest confidence
        ProjectType primary = typeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(UNKNOWN);

        return new ProjectClassificationResult(
                Collections.unmodifiableMap(typeScores),
                primary,
                profile,
                detectedFrameworks,
                List.of(),
                bestRule,
                0.0
        );
    }

    // ── Default Rule Definitions ──────────────────────────────

    private static List<ClassificationRule> buildDefaultRules() {
        List<ClassificationRule> rules = new ArrayList<>();

        // REST API: requires HTTP endpoints, no UI
        rules.add(new ClassificationRule(
                REST_API,
                Set.of(HTTP_ENDPOINT),
                Set.of(DATABASE_ACCESS, SECURITY, DEPENDENCY_INJECTION),
                Set.of(WEB_UI, DESKTOP_UI, MOBILE_UI),
                0.7, 10
        ));

        // WEB APP: requires web UI (templates, SSR, SPA)
        rules.add(new ClassificationRule(
                WEB_APP,
                Set.of(WEB_UI),
                Set.of(HTTP_ENDPOINT, TEMPLATE_RENDERING, STATIC_SITE_GENERATION),
                Set.of(),
                0.7, 10
        ));

        // Desktop App: requires desktop UI
        rules.add(new ClassificationRule(
                DESKTOP_APP,
                Set.of(DESKTOP_UI),
                Set.of(FILE_IO),
                Set.of(),
                0.8, 10
        ));

        // Mobile App: requires mobile UI
        rules.add(new ClassificationRule(
                MOBILE_APP,
                Set.of(MOBILE_UI),
                Set.of(),
                Set.of(),
                0.8, 10
        ));

        // CLI App: command-line interface
        rules.add(new ClassificationRule(
                CLI_APP,
                Set.of(CLI),
                Set.of(FILE_IO),
                Set.of(WEB_UI, HTTP_ENDPOINT, DESKTOP_UI, MOBILE_UI),
                0.6, 5
        ));

        // Microservice: HTTP endpoints + message queue
        rules.add(new ClassificationRule(
                MICROSERVICE,
                Set.of(HTTP_ENDPOINT, MESSAGE_QUEUE),
                Set.of(DATABASE_ACCESS, SECURITY, DEPENDENCY_INJECTION),
                Set.of(),
                0.6, 8
        ));

        // Serverless: serverless functions
        rules.add(new ClassificationRule(
                SERVERLESS,
                Set.of(SERVERLESS_FUNCTION),
                Set.of(HTTP_ENDPOINT),
                Set.of(),
                0.7, 9
        ));

        // Background Worker: scheduled tasks, no HTTP
        rules.add(new ClassificationRule(
                BACKGROUND_WORKER,
                Set.of(SCHEDULED_TASK),
                Set.of(MESSAGE_QUEUE, DATABASE_ACCESS),
                Set.of(HTTP_ENDPOINT, WEB_UI),
                0.6, 7
        ));

        // Library: no runtime capabilities detected (low priority fallback)
        rules.add(new ClassificationRule(
                LIBRARY,
                Set.of(),
                Set.of(TESTING),
                Set.of(HTTP_ENDPOINT, WEB_UI, DESKTOP_UI, MOBILE_UI, CLI,
                        SERVERLESS_FUNCTION, SCHEDULED_TASK),
                0.3, 1
        ));

        // Sort by priority descending
        rules.sort(Comparator.comparingInt(ClassificationRule::priority).reversed());

        return List.copyOf(rules);
    }
}
