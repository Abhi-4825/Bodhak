package com.example.bodhakfrontend.ai.service;

import com.example.bodhakfrontend.ai.evidence.builder.ArchitectureEvidenceBuilder;
import com.example.bodhakfrontend.ai.evidence.model.ArchitectureAnalysisEvidence;
import com.example.bodhakfrontend.ai.ollama.OllamaClient;
import com.example.bodhakfrontend.ai.prompt.ArchitecturePromptBuilder;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

public class ArchitectureAnalysisService {
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
           AnalysisContext context
    ) throws Exception {

        ArchitectureAnalysisEvidence evidence =
                evidenceBuilder.build(
                  context
                );

        String prompt =
                promptBuilder.build(
                        evidence
                );

        return ollamaClient.generate(
                "qwen2.5:3b",
                prompt
        );
    }

}
