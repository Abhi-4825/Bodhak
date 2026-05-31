package com.example.bodhakfrontend.core.Analysis;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;

import java.util.List;
import java.util.Map;

/**This class becomes the shared container for:
project data
entities
dependency graph
future metrics
future semantic graph**/

import java.util.Optional;
import java.util.stream.Collectors;

public class AnalysisContext {

    private final ProjectInfo projectInfo;

    private final DependencyGraph dependencyGraph;

    private final List<EntityInfo> entities;

    private final Map<String, EntityInfo> entityMap;

    public AnalysisContext(
            ProjectInfo projectInfo,
            DependencyGraph dependencyGraph,
            List<EntityInfo> entities
    ) {

        this.projectInfo = projectInfo;
        this.dependencyGraph = dependencyGraph;
        this.entities = entities;

        this.entityMap = entities.stream()
                .collect(Collectors.toMap(
                        EntityInfo::getEntityName,
                        entity -> entity,
                        (a, b) -> a
                ));
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    public List<EntityInfo> getEntities() {
        return entities;
    }

    public Optional<EntityInfo> findEntity(String qualifiedName) {
        return Optional.ofNullable(entityMap.get(qualifiedName));
    }
}
