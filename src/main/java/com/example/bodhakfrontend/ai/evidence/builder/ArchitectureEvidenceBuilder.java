package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.ai.evidence.model.*;
import com.example.bodhakfrontend.ai.evidence.model.architectur.ArchitectureAnalysisEvidence;
import com.example.bodhakfrontend.ai.evidence.model.architectur.ArchitectureSummaryEvidence;
import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.analysis.entityflag.EntityCharacteristics;
import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.GraphSnapshot;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.example.bodhakfrontend.ai.event.AnalysisEvent;
import com.example.bodhakfrontend.ai.event.AnalysisEventType;
import com.example.bodhakfrontend.ai.event.AnalysisProgressListener;

public class ArchitectureEvidenceBuilder implements EvidenceBuilder<ArchitectureAnalysisEvidence> {

    public ArchitectureAnalysisEvidence build(
          AnalysisContext analysisContext, AnalysisProgressListener listener
    ) {
        ProjectInfo projectInfo=analysisContext.getProjectInfo();
        GraphSnapshot graph=analysisContext.getDependencyGraph().snapshot();
        
        if (listener != null) {
            long edgeCount = graph.globalDependencies().values().stream().mapToInt(java.util.Set::size).sum();
            listener.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.METRIC, "Dependency graph loaded", 
                analysisContext.getEntities().size() + " entities\n" + edgeCount + " dependency edges"));
        }

        Collection<EntityCharacteristics> characteristics = analysisContext.getCharacteristics();
        List<EntityInfo> entities = analysisContext.getEntities();
        int totalEntities        = entities.size();
        int withWarnings         = (int) characteristics.stream().filter(c -> !c.flags().isEmpty()).count();
        int healthy              = totalEntities - withWarnings;
        int godCount             = (int) characteristics.stream().filter(c -> c.flags().contains(EntityFlag.OVERSIZED_ENTITY)).count();
        int highlyCoupled        = (int) characteristics.stream().filter(c -> c.flags().contains(EntityFlag.HIGH_COUPLING)).count();
        int circular             = (int) entities.stream().filter(e -> !e.getCircularGroups().isEmpty()).count();

        ArchitectureSummaryEvidence summaryEvidence=new ArchitectureSummaryEvidence(
                totalEntities,
                healthy,
                withWarnings,
                godCount,
                highlyCoupled,
                circular,
                graph.circularGroups().size()
        );
        DependencyGraphEvidenceBuilder dependencyGraphEvidenceBuilder=new DependencyGraphEvidenceBuilder();
        DependencyGraphEvidence dependencyGraphEvidence=dependencyGraphEvidenceBuilder.buildGraphEvidence(analysisContext);

        List<CircularDependencyEvidence> cycles =
                graph.circularGroups()
                        .stream()
                        .map(group ->
                                new CircularDependencyEvidence(
                                        group,
                                        group.size()
                                ))
                        .toList();
                        
        if (listener != null && !cycles.isEmpty()) {
            int maxCycle = cycles.stream().mapToInt(CircularDependencyEvidence::cycleSize).max().orElse(0);
            listener.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.FINDING, "Detected circular dependency clusters", 
                "Found " + cycles.size() + " clusters\nLargest cycle size: " + maxCycle + " entities"));
        }

        List<HighCouplingEvidence> couplingEvidence =
                characteristics.stream()
                        .filter(c -> c.flags().contains(EntityFlag.HIGH_COUPLING))
                        .map(c -> {
                            EntityInfo entity = c.entityInfo();
                            int fanIn = entity.getUsedBy().size();
                            int fanOut = entity.getDependsOn().size();
                            return new HighCouplingEvidence(
                                    entity.getEntityName(),
                                    fanIn,
                                    fanOut,
                                    fanIn + fanOut
                            );
                        })
                        .sorted(
                                Comparator.comparingInt(
                                        HighCouplingEvidence::couplingScore
                                ).reversed()
                        )
                        .toList();
        List<GodClassEvidence> godClasses =
                characteristics.stream()
                        .filter(c -> c.flags().contains(EntityFlag.OVERSIZED_ENTITY))
                        .map(c -> {
                            EntityInfo entity = c.entityInfo();
                            return new GodClassEvidence(
                                    entity.getEntityName(),
                                    entity.getLinesOfCode(),
                                    (int) entity.getMethodCount(),
                                    entity.getFields().size(),
                                    entity.getDependsOn().size()
                            );
                        })
                        .toList();
                        
        HighFanInEvidenceBuilder highFanInEvidenceBuilder=new HighFanInEvidenceBuilder();
        List<HighFanInEvidence> highFanInEvidences=highFanInEvidenceBuilder.buildHighFanIns(analysisContext);
        HighFanOutEvidenceBuilder highFanOutEvidenceBuilder=new HighFanOutEvidenceBuilder();
        List<HighFanOutEvidence> highFanOutEvidences=highFanOutEvidenceBuilder.buildHighFanOuts(analysisContext);
        GrowthRiskEvidenceBuilder growthRiskEvidenceBuilder=new GrowthRiskEvidenceBuilder();
        List<GrowthRiskEvidence> growthRiskEvidences=growthRiskEvidenceBuilder.buildGrowthRisks(analysisContext);

        BetweennessCentralityEvidenceBuilder betweennessCentralityEvidenceBuilder=new BetweennessCentralityEvidenceBuilder();
        List<BetweennessCentralityEvidence> betweennessCentralityEvidences=betweennessCentralityEvidenceBuilder.buildCentralityEvidence(analysisContext).stream().limit(10).toList();

        if (listener != null) {
            listener.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.METRIC, "Graph bottlenecks evaluated", 
                "Computed:\n✓ Fan-In Metrics\n✓ Fan-Out Metrics\n✓ Betweenness Centrality"));
        }

        return new ArchitectureAnalysisEvidence(

                summaryEvidence,
                dependencyGraphEvidence,
                cycles,
                couplingEvidence,
                godClasses,
                highFanInEvidences,
                highFanOutEvidences,
                growthRiskEvidences,
                betweennessCentralityEvidences

        );
    }

    private List<EntityInfo> filterByFlag(
            AnalysisContext analysisContext,
            EntityFlag flag
    ) {
        return analysisContext.getCharacteristics()
                .stream()
                .filter(c -> c.flags().contains(flag))
                .map(EntityCharacteristics::entityInfo)
                .toList();
    }
}