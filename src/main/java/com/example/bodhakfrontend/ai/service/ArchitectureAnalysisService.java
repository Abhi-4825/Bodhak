package com.example.bodhakfrontend.ai.service;

import com.example.bodhakfrontend.ai.evidence.builder.ArchitectureEvidenceBuilder;
import com.example.bodhakfrontend.ai.evidence.model.architectur.ArchitectureAnalysisEvidence;
import com.example.bodhakfrontend.ai.ollama.OllamaClient;
import com.example.bodhakfrontend.ai.prompt.ArchitecturePromptBuilder;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

import com.example.bodhakfrontend.ai.event.AnalysisEvent;
import com.example.bodhakfrontend.ai.event.AnalysisEventType;
import com.example.bodhakfrontend.ai.event.AnalysisProgressListener;

public class ArchitectureAnalysisService implements AiAnalysisService{
    private final ArchitectureEvidenceBuilder
            evidenceBuilder =
            new ArchitectureEvidenceBuilder();

    private final ArchitecturePromptBuilder
            promptBuilder =
            new ArchitecturePromptBuilder();

    private final OllamaClient
            ollamaClient =
            new OllamaClient();

    public String analyze(
           AnalysisContext context, String model, AnalysisProgressListener eventLogger
    ) throws Exception {

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Analyzing architecture...", null));
        }
        
        ArchitectureAnalysisEvidence evidence =
                evidenceBuilder.build(
                  context, eventLogger
                );

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Preparing evidence for model...", null));
        }
        
        String prompt =
                promptBuilder.build(
                        evidence
                );

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
