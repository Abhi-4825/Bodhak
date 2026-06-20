package com.example.bodhakfrontend.ai.evidence.model.scalability;



import com.example.bodhakfrontend.ai.evidence.model.*;

import java.util.List;

public record ScalabilityAnalysisEvidence(

        DependencyGraphEvidence graph,

        List<HighFanOutEvidence> highFanOuts,

        List<HighFanInEvidence> highFanIns,

        List<GrowthRiskEvidence> growthMetrics,

        List<BetweennessCentralityEvidence> centralities,

        List<CircularDependencyEvidence> circularDependencies

) {}
