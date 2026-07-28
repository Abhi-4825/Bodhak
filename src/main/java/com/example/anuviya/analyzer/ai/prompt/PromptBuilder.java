package com.example.anuviya.analyzer.ai.prompt;

public interface PromptBuilder<T> {

    record PromptPair(String systemPrompt, String userPrompt, String responsePrefix) {
        public PromptPair(String systemPrompt, String userPrompt) {
            this(systemPrompt, userPrompt, "");
        }
    }

    PromptPair build(
            T evidence
    );

}
