package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.ai.evidence.model.ArchitectureAnalysisEvidence;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.IssueType;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;

import java.util.List;

public class ArchitectureEvidenceBuilder {

    public ArchitectureAnalysisEvidence build(
          AnalysisContext analysisContext
    ) {
        ProjectInfo projectInfo=analysisContext.getProjectInfo();

        List<EntityInfo> godClasses =
                filterByIssue(projectInfo, IssueType.GOD_CLASS);

        List<EntityInfo> highlyCoupled =
                filterByIssue(projectInfo, IssueType.HIGH_COUPLING);

        List<EntityInfo> circular =
                filterByIssue(projectInfo, IssueType.CIRCULAR_DEPENDENCY);

        List<EntityInfo> anemic =
                filterByIssue(projectInfo, IssueType.ANEMIC_DOMAIN);

        return new ArchitectureAnalysisEvidence(
                projectInfo.getTotalEntities(),
                projectInfo.getHealthyEntities(),
                projectInfo.getEntitiesWithWarnings(),
                godClasses,
                highlyCoupled,
                circular,
                anemic,
                analysisContext.getDependencyGraph().snapshot().circularGroups()
        );
    }

    private List<EntityInfo> filterByIssue(
            ProjectInfo projectInfo,
            IssueType issueType
    ) {

        return projectInfo.getEntities()
                .stream()
                .filter(entity ->
                        entity.getIssueType().contains(issueType))
                .toList();
    }
}