package com.example.bodhak.model.project;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ProjectModel(
    BuildModel buildModel,
    DeploymentModel deploymentModel,
    PackagingModel packagingModel,
    List<RepositoryInfo> repositories,
    List<ModuleInfo> modules
) {
    public static ProjectModel empty() {
        return new ProjectModel(BuildModel.empty(), DeploymentModel.empty(), PackagingModel.empty(), List.of(), List.of());
    }
}
