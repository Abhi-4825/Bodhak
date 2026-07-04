package com.example.bodhak.orchestration.incremental.engine;

import java.nio.file.Path;
import java.util.*;

public class InvalidationPlanner {
    private final DependencyImpactAnalyzer analyzer;

    public InvalidationPlanner(DependencyImpactAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public InvalidationPlan plan(Set<ChangeEvent> changes) {
        Set<ArtifactId> directlyStale = new HashSet<>();
        Set<Path> filesToProcess = new HashSet<>();
        Set<Path> filesToRemove = new HashSet<>();

        for (ChangeEvent change : changes) {
            Path file = change.filePath();
            switch (change.changeType()) {
                case CONTENT_CHANGE, NEW_FILE -> {
                    directlyStale.add(ArtifactId.PARSED_IR);
                    filesToProcess.add(file);
                }
                case DELETED_FILE -> {
                    directlyStale.add(ArtifactId.COMPILATION_UNIT); // Trigger downstream dependency updates
                    filesToRemove.add(file);
                }
                case BUILD_CONFIG_CHANGE -> {
                    directlyStale.add(ArtifactId.PROJECT_ROOTS);
                }
                case RESOURCE_CHANGE -> {
                    directlyStale.add(ArtifactId.CLASSIFICATION_RESULT);
                }
                default -> {}
            }
        }

        Set<ArtifactId> fullyStale = analyzer.analyze(directlyStale);

        return new InvalidationPlan(fullyStale, filesToProcess, filesToRemove);
    }
}
