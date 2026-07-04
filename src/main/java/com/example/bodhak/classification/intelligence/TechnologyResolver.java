package com.example.bodhak.classification.intelligence;

import com.example.bodhak.model.project.ProjectModel;
import java.util.*;

public class TechnologyResolver {
    private final ScoringPolicy policy;

    public TechnologyResolver(ScoringPolicy policy) {
        this.policy = policy;
    }

    public TechnologyResolver() {
        this(new WeightedScoringPolicy());
    }

    public TechnologyCatalog resolve(List<TechnologyDef> registry, EvidenceGraph graph, ProjectModel projectModel) {
        List<DetectedTechnology> detected = new ArrayList<>();

        for (TechnologyDef tech : registry) {
            double confidence = policy.calculateConfidence(tech, graph);

            if (confidence >= 0.25) { // threshold
                List<Evidence> matchedEvidence = collectMatchedEvidence(tech, graph);
                String version = resolveVersion(tech, projectModel);

                detected.add(new DetectedTechnology(
                    tech.getId(),
                    tech.getDisplayName(),
                    tech.getFamily(),
                    tech.getCategory(),
                    matchedEvidence,
                    tech.getCapabilities(),
                    version,
                    confidence,
                    Map.of()
                ));
            }
        }

        return new TechnologyCatalog(detected);
    }

    private List<Evidence> collectMatchedEvidence(TechnologyDef tech, EvidenceGraph graph) {
        List<Evidence> matched = new ArrayList<>();
        for (TechnologyDef.EvidenceRule rule : tech.getEvidence()) {
            EvidenceType type;
            try {
                type = EvidenceType.valueOf(rule.getType());
            } catch (IllegalArgumentException e) {
                continue;
            }

            List<String> patterns = new ArrayList<>();
            if (rule.getPattern() != null && !rule.getPattern().isBlank()) {
                patterns.add(rule.getPattern());
            }
            if (rule.getAliases() != null) {
                patterns.addAll(rule.getAliases());
            }

            for (String pattern : patterns) {
                graph.findEvidence(type, pattern).ifPresent(matched::add);
            }
        }
        return matched;
    }

    private String resolveVersion(TechnologyDef tech, ProjectModel projectModel) {
        if (tech.getVersionDetection() == null) return "unknown";
        String buildDep = tech.getVersionDetection().getBuildDependency();
        if (buildDep == null || buildDep.isBlank()) return "unknown";

        var deps = projectModel.buildModel().dependencies();
        if (deps.containsKey(buildDep)) {
            return deps.get(buildDep);
        }
        for (var entry : deps.entrySet()) {
            if (entry.getKey().contains(buildDep)) {
                return entry.getValue();
            }
        }
        return "unknown";
    }
}
