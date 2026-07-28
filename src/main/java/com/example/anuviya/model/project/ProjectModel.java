package com.example.anuviya.model.project;

import java.util.List;

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
