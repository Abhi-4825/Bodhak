package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.ai.evidence.model.DependencyGraphEvidence;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.GraphSnapshot;

import java.util.Set;

public class DependencyGraphEvidenceBuilder {
    public DependencyGraphEvidence buildGraphEvidence(
           AnalysisContext context
    ) {
        ProjectInfo projectInfo=context.getProjectInfo();
        GraphSnapshot graph=context.getDependencyGraph().snapshot();
        int totalEdges =
                graph.globalDependencies()
                        .values()
                        .stream()
                        .mapToInt(Set::size)
                        .sum();

        int largestCycle =
                graph.circularGroups()
                        .stream()
                        .mapToInt(Set::size)
                        .max()
                        .orElse(0);

        int maxFanIn =
                projectInfo.getEntities()
                        .stream()
                        .mapToInt(e -> e.getUsedBy().size())
                        .max()
                        .orElse(0);

        int maxFanOut =
                projectInfo.getEntities()
                        .stream()
                        .mapToInt(e -> e.getDependsOn().size())
                        .max()
                        .orElse(0);

        double avgFanOut =
                projectInfo.getEntities()
                        .stream()
                        .mapToInt(e -> e.getDependsOn().size())
                        .average()
                        .orElse(0);

        return new DependencyGraphEvidence(

                projectInfo.getTotalEntities(),

                totalEdges,

                graph.circularGroups().size(),

                largestCycle,

                maxFanIn,

                maxFanOut,

                avgFanOut
        );
    }
}
