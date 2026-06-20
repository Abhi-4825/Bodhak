package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.ai.evidence.model.CircularDependencyEvidence;
import com.example.bodhakfrontend.ai.evidence.model.scalability.ScalabilityAnalysisEvidence;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.engine.GraphSnapshot;

import java.util.List;

import com.example.bodhakfrontend.ai.event.AnalysisEvent;
import com.example.bodhakfrontend.ai.event.AnalysisEventType;
import com.example.bodhakfrontend.ai.event.AnalysisProgressListener;

public class ScalabilityEvidenceBuilder implements EvidenceBuilder<ScalabilityAnalysisEvidence> {
     private final DependencyGraphEvidenceBuilder dependencyGraphEvidenceBuilder=new DependencyGraphEvidenceBuilder();
     private final HighFanOutEvidenceBuilder highFanOutEvidenceBuilder=new HighFanOutEvidenceBuilder();
     private final HighFanInEvidenceBuilder highFanInEvidenceBuilder=new HighFanInEvidenceBuilder();
     private final GrowthRiskEvidenceBuilder growthRiskEvidenceBuilder=new GrowthRiskEvidenceBuilder();
     private final BetweennessCentralityEvidenceBuilder betweennessCentralityEvidenceBuilder=new BetweennessCentralityEvidenceBuilder();


     public ScalabilityAnalysisEvidence build(AnalysisContext context, AnalysisProgressListener listener){
         GraphSnapshot graph=context.getDependencyGraph().snapshot();
         if (listener != null) {
             long edgeCount = graph.globalDependencies().values().stream().mapToInt(java.util.Set::size).sum();
             listener.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.METRIC, "Scalability analysis initiated", 
                 "Graph size: " + edgeCount + " edges"));
         }
         
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
             listener.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.FINDING, "Detected circular dependencies", 
                 "Found " + cycles.size() + " clusters"));
         }
         
         if (listener != null) {
             listener.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.METRIC, "Scalability bottlenecks evaluated", 
                 "Computed:\n✓ High Fan-Out Metrics\n✓ High Fan-In Metrics\n✓ Growth Risk\n✓ Centrality Evidence"));
         }

         return new ScalabilityAnalysisEvidence(
                 dependencyGraphEvidenceBuilder.buildGraphEvidence(context),
                 highFanOutEvidenceBuilder.buildHighFanOuts(context),
                 highFanInEvidenceBuilder.buildHighFanIns(context),
                 growthRiskEvidenceBuilder.buildGrowthRisks(context),
                 betweennessCentralityEvidenceBuilder.buildCentralityEvidence(context),
                 cycles
         );

     }




}
