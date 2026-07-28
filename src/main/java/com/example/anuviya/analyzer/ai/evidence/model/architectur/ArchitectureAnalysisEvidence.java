package com.example.anuviya.analyzer.ai.evidence.model.architectur;

import com.example.anuviya.analyzer.ai.evidence.model.*;

import java.util.List;

public record ArchitectureAnalysisEvidence(

        ArchitectureSummaryEvidence summary,

        DependencyGraphEvidence graph,

        List<CircularDependencyEvidence> circularDependencies,

        List<HighCouplingEvidence> highCouplings,

        List<GodClassEvidence> godClasses,

        List<HighFanInEvidence> highFanIns,

        List<HighFanOutEvidence> highFanOuts,

        List<GrowthRiskEvidence> growthRisks,
        List<BetweennessCentralityEvidence> betweennessCentralities,
        List<PackageCycleEvidence> packageCycles,
        List<PackageStabilityEvidence> packageStabilities

) {}
