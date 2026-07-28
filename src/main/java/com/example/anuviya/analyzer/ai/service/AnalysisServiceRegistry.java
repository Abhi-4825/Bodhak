package com.example.anuviya.analyzer.ai.service;


import com.example.anuviya.analyzer.ai.analysis.AnalysisType;

import java.util.EnumMap;
import java.util.Map;

public class AnalysisServiceRegistry {

    private final Map<
            AnalysisType,
            AiAnalysisService
            > services =
            new EnumMap<>(AnalysisType.class);

    public AnalysisServiceRegistry() {

        services.put(
                AnalysisType.ARCHITECTURE,
                new ArchitectureAnalysisService()
        );

        services.put(
                AnalysisType.SCALABILITY,
                new ScalabiltyAnalysisService()
        );
    }

    public AiAnalysisService get(
            AnalysisType type
    ) {

        return services.get(type);
    }
}
