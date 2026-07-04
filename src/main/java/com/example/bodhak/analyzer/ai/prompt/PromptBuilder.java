package com.example.bodhak.analyzer.ai.prompt;

import com.example.bodhak.analyzer.ai.prompt.AiPromptContext;

public interface PromptBuilder<T> {

    String build(
            T evidence
    );

}
