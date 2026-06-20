package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.ai.evidence.model.HighFanOutEvidence;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;

import java.util.Comparator;
import java.util.List;

public class HighFanOutEvidenceBuilder {
    public List<HighFanOutEvidence> buildHighFanOuts(
            AnalysisContext context
    ) {
        return context.getProjectInfo().getEntities()
                .stream()
                .map(entity ->
                        new HighFanOutEvidence(

                                entity.getEntityName(),

                                entity.getDependsOn().size()
                        )
                )
                .sorted(
                        Comparator.comparingInt(
                                HighFanOutEvidence::fanOut
                        ).reversed()
                )
                .limit(10)
                .toList();
    }


}
