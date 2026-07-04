package com.example.bodhak.context.state;

import com.example.bodhak.context.AnalysisContext;

@FunctionalInterface
public interface AiAssistantState {
    void update(AnalysisContext context);
}
