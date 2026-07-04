package com.example.bodhak.model.project;

import java.nio.file.Path;
import java.util.Set;

public record DeploymentModel(
    boolean hasDockerfile,
    Set<Path> dockerfiles,
    boolean hasKubernetesManifests,
    Set<Path> kubernetesManifests
) {
    public static DeploymentModel empty() {
        return new DeploymentModel(false, Set.of(), false, Set.of());
    }
}
