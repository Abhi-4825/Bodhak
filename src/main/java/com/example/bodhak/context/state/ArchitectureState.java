package com.example.bodhak.context.state;

import com.example.bodhak.context.AnalysisContext;

@FunctionalInterface
public interface ArchitectureState {
    void update(AnalysisContext context);
}
