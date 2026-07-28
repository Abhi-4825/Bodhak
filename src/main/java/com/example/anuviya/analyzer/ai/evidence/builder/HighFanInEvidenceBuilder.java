package com.example.anuviya.analyzer.ai.evidence.builder;

import com.example.anuviya.analyzer.ai.evidence.model.HighFanInEvidence;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.project.ProjectInfo;

import java.util.Comparator;
import java.util.List;

public class HighFanInEvidenceBuilder {
    public List<HighFanInEvidence> buildHighFanIns(
           AnalysisContext context
    ) {
        ProjectInfo projectInfo=context.getProjectInfo();
        return context.getEntities()
                .stream()
                .map(entity ->
                        new HighFanInEvidence(

                                entity.getEntityName(),

                                entity.getUsedBy().size()
                        )
                )
                .sorted(
                        Comparator.comparingInt(
                                HighFanInEvidence::fanIn
                        ).reversed()
                )
                .limit(10)
                .toList();
    }
}
