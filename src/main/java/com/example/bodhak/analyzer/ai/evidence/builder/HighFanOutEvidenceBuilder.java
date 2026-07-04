package com.example.bodhak.analyzer.ai.evidence.builder;

import com.example.bodhak.analyzer.ai.evidence.model.HighFanOutEvidence;
import com.example.bodhak.context.AnalysisContext;

import java.util.Comparator;
import java.util.List;

public class HighFanOutEvidenceBuilder {
    public List<HighFanOutEvidence> buildHighFanOuts(
            AnalysisContext context
    ) {
        return context.getEntities()
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
