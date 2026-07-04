package com.example.bodhak.compiler.root;

import com.example.bodhak.model.project.ProjectSurface;
import com.example.bodhak.model.project.ProjectRootInfo;
import com.example.bodhak.model.project.Evidence;
import com.example.bodhak.model.project.RootCapability;
import java.util.*;

/**
 * Resolves raw candidate evidence profiles into finalized, capability-tagged ProjectSurface records.
 */
public class RootResolver {
    private static final double CONFIDENCE_THRESHOLD = 0.5;

    public ProjectRootInfo resolve(EvidenceAccumulator accumulator) {
        List<ProjectSurface> resolved = new ArrayList<>();
        Set<String> archetypes = new HashSet<>();

        accumulator.getCandidates().forEach((symbol, candidate) -> {
            double totalScore = candidate.evidenceList.stream()
                .mapToDouble(Evidence::score)
                .sum();

            if (totalScore >= CONFIDENCE_THRESHOLD) {
                resolved.add(new ProjectSurface(
                    symbol,
                    EnumSet.copyOf(candidate.capabilities),
                    Set.copyOf(candidate.evidenceList),
                    Math.min(1.0, totalScore)
                ));
                archetypes.addAll(candidate.archetypeEvidence);
            }
        });

        return new ProjectRootInfo(archetypes, resolved);
    }
}
