package com.example.anuviya.orchestration.incremental.engine;

import java.nio.file.Path;
import java.util.Set;

public record InvalidationPlan(
    Set<ArtifactId> staleArtifacts,
    Set<Path> filesToProcess,
    Set<Path> filesToRemove
) {}
