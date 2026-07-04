package com.example.bodhak.orchestration.incremental.engine;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.AnalysisContextFactory;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.namespace.NamespaceInfo;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.model.project.ProjectSnapshot;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.classification.intelligence.ProjectIntelligenceEngine;
import com.example.bodhak.orchestration.snapshot.NamespaceBuilder;
import com.example.bodhak.orchestration.snapshot.ProjectInfoBuilder;
import com.example.bodhak.orchestration.snapshot.ProjectSnapshotBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IncrementalContextAssembler {

    private final NamespaceBuilder namespaceBuilder = new NamespaceBuilder();
    private final ProjectSnapshotBuilder projectSnapshotBuilder = new ProjectSnapshotBuilder();

    public AnalysisContext assemble(
            AnalysisContext previous,
            ArtifactCache cache,
            DependencyGraph dependencyGraph,
            AnalysisContextFactory factory,
            ProjectInfoBuilder projectInfoBuilder,
            ProjectIntelligenceEngine projectIntelligenceEngine,
            Path projectPath,
            Set<ArtifactId> staleArtifacts
    ) {
        List<EntityInfo> allEntities = cache.getEntityCache().values().stream()
                .flatMap(List::stream)
                .toList();

        // 1. Build Namespaces
        Map<String, NamespaceInfo> namespaces;
        if (staleArtifacts.contains(ArtifactId.NAMESPACE_INDEX) || previous == null) {
            namespaces = namespaceBuilder.build(allEntities);
            cache.getNamespaceCache().clear();
            namespaces.forEach(cache::putNamespace);
        } else {
            namespaces = cache.getNamespaceCache();
        }

        // 2. Build ProjectInfo
        ProjectInfo projectInfo;
        if (staleArtifacts.contains(ArtifactId.PROJECT_INFO) || previous == null) {
            projectInfo = projectInfoBuilder.buildAll(projectPath, allEntities);
            cache.setProjectInfo(projectInfo);
        } else {
            projectInfo = cache.getProjectInfo();
        }

        // 3. Build ProjectSnapshot
        ProjectSnapshot snapshot;
        if (staleArtifacts.contains(ArtifactId.PROJECT_SNAPSHOT) || previous == null) {
            snapshot = projectSnapshotBuilder.build(projectInfo, allEntities, namespaces);
            cache.setProjectSnapshot(snapshot);
        } else {
            snapshot = cache.getProjectSnapshot();
        }

        // 4. Build ProjectModel
        com.example.bodhak.model.project.ProjectModel projectModel = 
                com.example.bodhak.model.project.ProjectModelParser.parse(projectPath, projectInfo.knownFiles());

        // 5. Build ClassificationResult
        ProjectClassificationResult classificationResult;
        if (staleArtifacts.contains(ArtifactId.CLASSIFICATION_RESULT) || previous == null) {
            AnalysisContext tempContext = new AnalysisContext(
                    snapshot,
                    dependencyGraph,
                    null,
                    null,
                    List.of(),
                    new ArrayList<>(cache.getCuCache().values()),
                    cache.getReferenceDatabase(),
                    cache.getSymbolTable(),
                    projectModel
            );
            classificationResult = projectIntelligenceEngine.analyze(tempContext, projectModel);
            cache.setClassificationResult(classificationResult);
        } else {
            classificationResult = cache.getClassificationResult();
        }

        // Assemble final AnalysisContext
        return factory.create(
                snapshot,
                dependencyGraph,
                classificationResult,
                new ArrayList<>(cache.getCuCache().values()),
                cache.getReferenceDatabase(),
                cache.getSymbolTable(),
                projectModel
        );
    }
}
