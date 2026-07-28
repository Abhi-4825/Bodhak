package com.example.anuviya.analyzer.ai.evidence.model.scalability;



import com.example.anuviya.analyzer.ai.evidence.model.*;

import java.util.List;

public record ScalabilityAnalysisEvidence(

        DependencyGraphEvidence graph,

        List<HighFanOutEvidence> highFanOuts,

        List<HighFanInEvidence> highFanIns,

        List<GrowthRiskEvidence> growthMetrics,

        List<BetweennessCentralityEvidence> centralities,

        List<CircularDependencyEvidence> circularDependencies

) {}
