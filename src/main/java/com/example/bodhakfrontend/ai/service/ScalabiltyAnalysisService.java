package com.example.bodhakfrontend.ai.service;

import com.example.bodhakfrontend.ai.evidence.builder.ScalabilityEvidenceBuilder;
import com.example.bodhakfrontend.ai.evidence.model.scalability.ScalabilityAnalysisEvidence;
import com.example.bodhakfrontend.ai.ollama.OllamaClient;
import com.example.bodhakfrontend.ai.prompt.ScalabilityPromptBuilder;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

import com.example.bodhakfrontend.ai.event.AnalysisEvent;
import com.example.bodhakfrontend.ai.event.AnalysisEventType;
import com.example.bodhakfrontend.ai.event.AnalysisProgressListener;

public class ScalabiltyAnalysisService implements  AiAnalysisService {
private final ScalabilityEvidenceBuilder builder=new ScalabilityEvidenceBuilder();
private final ScalabilityPromptBuilder promptBuilder=new ScalabilityPromptBuilder();

private final OllamaClient ollamaClient=new OllamaClient();

public String analyze(AnalysisContext context, String model, AnalysisProgressListener eventLogger) throws Exception{
    if (eventLogger != null) {
        eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Analyzing scalability...", null));
    }
    
    ScalabilityAnalysisEvidence evidence=builder.build(context, eventLogger);

    if (eventLogger != null) {
        eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Preparing evidence for model...", null));
    }
    
    String prompt=promptBuilder.build(evidence);
    
    if (eventLogger != null) {
        eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.MODEL, "Connecting to " + model, "Evidence package sent\nReady for reasoning"));
    }
    
    String response = ollamaClient.generate(
            model,
            prompt,
            chunk -> {
                if (eventLogger != null) {
                    eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.MODEL_STREAM_CHUNK, "chunk", chunk));
                }
            }
    );
    
    if (eventLogger != null) {
        eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.SUCCESS, "Reasoning complete", "Response parsed successfully"));
    }
    
    return response;
}

}
