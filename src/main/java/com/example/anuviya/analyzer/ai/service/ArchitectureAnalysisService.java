package com.example.anuviya.analyzer.ai.service;

import com.example.anuviya.analyzer.ai.evidence.builder.ArchitectureEvidenceBuilder;
import com.example.anuviya.analyzer.ai.evidence.model.architectur.ArchitectureAnalysisEvidence;
import com.example.anuviya.analyzer.ai.prompt.ArchitecturePromptBuilder;
import com.example.anuviya.context.AnalysisContext;

import com.example.anuviya.analyzer.ai.event.AnalysisEvent;
import com.example.anuviya.analyzer.ai.event.AnalysisEventType;
import com.example.anuviya.analyzer.ai.event.AnalysisProgressListener;
import com.example.anuviya.analyzer.ai.platform.AIPlatform;
import com.example.anuviya.analyzer.ai.platform.session.AISession;
import com.example.anuviya.analyzer.ai.platform.session.SessionManager;
import com.example.anuviya.analyzer.ai.platform.prompt.PromptPackage;

public class ArchitectureAnalysisService implements AiAnalysisService {
    private final ArchitectureEvidenceBuilder evidenceBuilder = new ArchitectureEvidenceBuilder();
    private final ArchitecturePromptBuilder promptBuilder = new ArchitecturePromptBuilder();

    @Override
    public String analyze(
           AnalysisContext context, String model, AnalysisProgressListener eventLogger
    ) throws Exception {

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Analyzing architecture...", null));
        }
        
        ArchitectureAnalysisEvidence evidence = evidenceBuilder.build(context, eventLogger);

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Preparing evidence for model...", null));
        }
        
        com.example.anuviya.analyzer.ai.prompt.PromptBuilder.PromptPair pair = promptBuilder.build(evidence);

        // Extract responsePrefix — the priming tokens that the user prompt ends with.
        // The model will continue from these tokens, so we prepend them to the final response.
        String responsePrefix = pair.responsePrefix() != null ? pair.responsePrefix() : "";

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.INFO, "Resolving AI session...", null));
        }

        // Open AI session
        AISession session = AIPlatform.getInstance().openSession("architecture", model);
        PromptPackage promptPackage = new PromptPackage(pair.systemPrompt(), pair.userPrompt(), null, null);

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.MODEL, "Connecting to " + session.getModel().displayName(), "Evidence package sent\nReady for reasoning"));
        }
        
        final StringBuilder responseBuilder = new StringBuilder();
        session.getProvider().stream(
            session,
            promptPackage,
            chunk -> {
                responseBuilder.append(chunk);
                if (eventLogger != null) {
                    eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.MODEL_STREAM_CHUNK, "chunk", chunk));
                }
            }
        );
        
        SessionManager.getInstance().close(session);

        if (eventLogger != null) {
            eventLogger.publish(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.SUCCESS, "Reasoning complete", "Response parsed successfully"));
        }
        
        // Prepend the response prefix to reconstruct the full JSON
        String rawResponse = responseBuilder.toString().trim();
        if (!responsePrefix.isEmpty() && !rawResponse.startsWith(responsePrefix)) {
            return responsePrefix + rawResponse;
        }
        return rawResponse;
    }
}
