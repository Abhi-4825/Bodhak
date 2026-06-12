package com.example.bodhakfrontend.ai.prompt;

import com.example.bodhakfrontend.ai.context.AiPromptContext;

public interface PromptBuilder<T> {

    String build(
            T evidence
    );

}
