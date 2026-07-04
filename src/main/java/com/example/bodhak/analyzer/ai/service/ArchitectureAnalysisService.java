package com.example.bodhak.analyzer.ai.service;

import com.example.bodhak.analyzer.ai.evidence.builder.ArchitectureEvidenceBuilder;
import com.example.bodhak.analyzer.ai.evidence.model.architectur.ArchitectureAnalysisEvidence;
import com.example.bodhak.analyzer.ai.client.OllamaClient;
import com.example.bodhak.analyzer.ai.prompt.ArchitecturePromptBuilder;
import com.example.bodhak.context.AnalysisContext;

import com.example.bodhak.analyzer.ai.event.AnalysisEvent;
import com.example.bodhak.analyzer.ai.event.AnalysisEventType;
import com.example.bodhak.analyzer.ai.event.AnalysisProgressListener;

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
